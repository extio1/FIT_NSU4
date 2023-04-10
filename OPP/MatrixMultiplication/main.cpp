#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include "matio.h"
#include <iostream>
#include <math.h>
#include <stdbool.h>

#define N1 5
#define N2 10
#define N3 4
#define N_DIMENSIONS 2
#define MAKE_DIFFERENT_FROM_COLS(row_coord) row_coord+size
#define MAKE_DIFFERENT_FROM_ROWS(col_coord) col_coord+size*10

typedef struct ScattervParam {
	int* size;
	int* offset;
} ScattervParam;

typedef struct Matrix {
	int xSize;
	int ySize;
	double* data;
} Matrix;

void devide_job_between_processes(const int nProc, const int jobSize, int* job) {
	int extraCells = jobSize % nProc;
	const int standartSize = jobSize / nProc;
	for (int i = 0; i < nProc; ++i) {
		if (extraCells == 0) {
			job[i] = standartSize;
		}
		else {
			job[i] = standartSize + 1;
			--extraCells;
		}
	}
}

void define_card_proc_size(size_t* xs, size_t* ys, const int nProc) {
	for (int i = (size_t)sqrt(nProc); i > 0; --i) {
		if ((nProc / i) * i == nProc) {
			*xs = i;
			*ys = nProc / i;
			break;
		}
	}
}

void init_cart(const int xSizeCart, const int ySizeCart, const int size, MPI_Comm* cartComm) {
	int dimensionsSizeArr[2] = { xSizeCart, ySizeCart };
	int* periodicOrNot = (int*)calloc(size, size * sizeof(int));
	bool reorderBit = true;
	if (MPI_Cart_create(MPI_COMM_WORLD, N_DIMENSIONS, dimensionsSizeArr,
		periodicOrNot, reorderBit, cartComm) != MPI_SUCCESS) printf("Error while MPI_Cart_create().\n");

	free(periodicOrNot);
}
/*
void mult_mat(const double* a, const double* b, double* c, const int n1, const int n2, const int n3){
	for (int i = 0; i < n; ++i)
		for (int k = 0; k < n; ++k)
			for (int j = 0; j < n; ++j)
				temp.mat[i * n + j] += a.mat[i * n + k] * b.mat[k * n + j];
}
*/

void make_col_row_types(MPI_Datatype* RowAType, MPI_Datatype* ColBType){ 
	//Создание и регистрация производных типов данных: столбец и строка матрицы

	MPI_Datatype ColBtypeUnaligned;
	if (MPI_Type_contiguous(N2, MPI_DOUBLE, RowAType) != MPI_SUCCESS) printf("Error while MPI_Type_contiguous().\n");
	if (MPI_Type_vector(N2, 1, N3 - 1, MPI_DOUBLE, &ColBtypeUnaligned) != MPI_SUCCESS) printf("Error while MPI_Type_contiguous().\n");
	MPI_Type_commit(RowAType);
	MPI_Type_commit(&ColBtypeUnaligned);

	MPI_Type_create_resized(ColBtypeUnaligned, 0, sizeof(double), ColBType);
	MPI_Type_commit(ColBType);

	MPI_Type_free(&ColBtypeUnaligned);
}

void make_submat_type(MPI_Datatype* SubMatType, const int* aMatDisctrib, const int* bMatDisctrib, const int* myCardCoords){ 
	//Создание и регистрация производных типов: результирующие подматрицы

	MPI_Datatype SubMatUnaligned;
	if (MPI_Type_vector(aMatDisctrib[myCardCoords[0]], bMatDisctrib[myCardCoords[1]],
		N2 - 1, MPI_DOUBLE, &SubMatUnaligned) != MPI_SUCCESS)printf("Error while MPI_Type_contiguous().\n");

	MPI_Type_commit(&SubMatUnaligned);

	int offsetBeginCMat = 0;
	for (int i = myCardCoords[1] - 1; i >= 0; --i)
		offsetBeginCMat += aMatDisctrib[i] * N3;
	for (int i = myCardCoords[0] - 1; i >= 0; --i)
		offsetBeginCMat += bMatDisctrib[i];

	MPI_Type_create_resized(SubMatUnaligned, offsetBeginCMat, bMatDisctrib[myCardCoords[0]], SubMatType);
	MPI_Type_commit(SubMatType);


	MPI_Type_free(&SubMatUnaligned);
}

void define_scatterv_matrixs(ScattervParam* a, ScattervParam* b, const int xSizeCart, const int ySizeCart){
	//Разделение строк и столбцов между процессами

	int* aMatDisctrib = (int*) malloc(sizeof(int)*ySizeCart); // в этих массивах записана количество строк или столбцов для каждого процесса
	int* bMatDisctrib = (int*) malloc(sizeof(int)*xSizeCart); // для рассылки их с помощью scatterv
	int* aOffsets = (int*) malloc(sizeof(int)*ySizeCart);
	int* bOffsets = (int*) malloc(sizeof(int)*xSizeCart);

	devide_job_between_processes(ySizeCart, N1, aMatDisctrib);
	devide_job_between_processes(xSizeCart, N3, bMatDisctrib);

	aOffsets[0] = 0;
	bOffsets[0] = 0;
	for(int i = 1; i < ySizeCart; ++i){
		aOffsets[i] = aOffsets[i-1] + aMatDisctrib[i-1]*N2;
	}
	for(int i = 1; i < xSizeCart; ++i){
		bOffsets[i] = bOffsets[i-1] + bMatDisctrib[i-1]*N2;
	}

	a->size = aMatDisctrib;
	b->size = bMatDisctrib;
	a->offset = aOffsets;
	b->offset = bOffsets;
}

//AMat * BMat = CMat
int main(int argc, char** argv) {
	int size, rankWorld;
	double timeStart, timeEnd;

	Matrix AMat, BMat, CMat;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rankWorld);

	double* AFull;
	double* BFull;
	double* CFull;
	//Чтение матриц из файла 0-м процессом
	if(rankWorld == 0){ // argv[1] = n1, argv[2] = n2, argv[3] = n3 размерности матриц А и В n1 x n2, n2 x n3 соотв.
		AFull = (double*) malloc(sizeof(double) * N1*N2);
		BFull = (double*) malloc(sizeof(double) * N2*N3);
		CFull = (double*) malloc(sizeof(double) * N1*N3);
		enter_matrix(AFull, N2, "AMat.txt");
		enter_matrix(BFull, N3, "BMat.txt");
	}
	
	//Создание топологии процессов - декартова решетка
	size_t xSizeCart, ySizeCart;
	if (argc == 1) {
		define_card_proc_size(&xSizeCart, &ySizeCart, size);
	}
	else if (argc == 3) {
		xSizeCart = atoi(argv[1]);
		ySizeCart = atoi(argv[2]);
	}
	else {
		if (rankWorld == 0) printf("Incorrect launching params.\n");
		return 0;
	}

	int rankCart;
	MPI_Comm cartComm;
	init_cart(xSizeCart, ySizeCart, size, &cartComm);
	MPI_Comm_rank(cartComm, &rankCart);

	//Создание коммуникатора объединяющего процессы, находящиеся в одной строке и в одном столбце
	int myCardCoords[2];
	int ignore[2];
	if (MPI_Cart_get(cartComm, N_DIMENSIONS, ignore, ignore, myCardCoords) != MPI_SUCCESS) { printf("Error while MPI_Cart_get().\n"); }

	// В новом коммутаторе процессы будут иметь нумерацию в соответсвии с их координатой
	// в каждом столбце/строке координата по y/x уникальна в нем/ней
	MPI_Comm myRowComm, myColComm;
	if (MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_COLS(myCardCoords[0]), myCardCoords[0], &myRowComm) != MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");
	if (MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_ROWS(myCardCoords[1]), myCardCoords[1], &myColComm) != MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");

	//  Данная структура по сути будет сопоставлять значению x в сетке процессов количество столбцов в подматрице В,
	// а координате y количество строк в подматрице А, распределенных по декартовой топологии процессов.
	// Стоит заметить, что другая размерность матрицы в обоих случаях будет равна N2
	// из-за чего умножение такие горизонтальных и вертикальных полос возможно и корректно.
	ScattervParam aMatScatterv, bMatScatterv;
	define_scatterv_matrixs(&aMatScatterv, &bMatScatterv, xSizeCart, ySizeCart);

if(rankWorld == 0){
	printf("aMatDisctrib\n");
	for(int i = 0; i < ySizeCart; ++i){
		printf("%d ", aMatScatterv.size[i]);
	}
	printf("\n");
	for(int i = 0; i < ySizeCart; ++i){
		printf("%d ", aMatScatterv.offset[i]);
	}
	printf("\nbMatDisctrib\n");
	for(int i = 0; i < xSizeCart; ++i){
		printf("%d ", bMatScatterv.size[i]);
	}
	printf("\n");
	for(int i = 0; i < xSizeCart; ++i){
		printf("%d ", bMatScatterv.offset[i]);
	}
	printf("\n");
}

	//Создание типов данных
	MPI_Datatype RowAType, ColBType, SubMatType;
	make_col_row_types(&RowAType, &ColBType);
	make_submat_type(&SubMatType, aMatScatterv.size, bMatScatterv.size, myCardCoords);


	//Определяем подматрицы A и В, с которыми будет работать каждый процесс в декартовой топологии
	AMat.xSize = N2; AMat.ySize = aMatScatterv.size[myCardCoords[1]];
	BMat.xSize = bMatScatterv.size[myCardCoords[0]]; BMat.ySize = N2;
	CMat.xSize = bMatScatterv.size[myCardCoords[0]]; CMat.ySize = aMatScatterv.size[myCardCoords[1]];
	AMat.data = (double*)malloc(sizeof(double)*AMat.ySize*AMat.xSize);
	BMat.data = (double*)malloc(sizeof(double)*BMat.xSize*BMat.ySize);
	CMat.data = (double*)сalloc(sizeof(double)*CMat.xSize*CMat.ySize);

	// 1Шаг: рассылаем из (0, 0) на крайнюю верхнюю строку и левый столбец
	// 	горизонтальные и вертикальные полосы определенного для каждого
	// 	процесса размера
	if(myCardCoords[0] == 0){
		MPI_Scatterv(AFull, aMatScatterv.size, aMatScatterv.offset, RowAType,
					 AMat.data, AMat.ySize*N2, MPI_DOUBLE, 0, myRowComm);
	}
	if(myCardCoords[1] == 0){
		MPI_Scatterv(BFull, bMatScatterv.size, bMatScatterv.offset, ColBType,
					 BMat.data, BMat.xSize*N2, MPI_DOUBLE, 0, myColComm);
	}

	// 2Шаг: каждый процесс на верхней строке и левом столбце делает
	// broadcast в пределах коммуникатора своего столбца и строки соответсвенно
	MPI_Bcast(BMat.data, bMatScatterv.size[myCardCoords[0]], ColBType, 0, myColComm);
	MPI_Bcast(AMat.data, aMatScatterv.size[myCardCoords[1]], RowAType, 0, myRowComm);

	// 3Шаг: непосредственно умножение
	for (int i = 0; i < AMat.ySize; ++i) {
        for (int k = 0; k < AMat.xSize; ++k) {
            for (int j = 0; j < BMat.xSize; ++j)
                CMat.data[i * BMat.xSize + j] += AMat.data[i*BMat.ySize + k] * BMat.data[k * BMat.xSize + j];
        }
    }
 


//Освобождние ресурсов
	MPI_Type_free(&RowAType);
	MPI_Type_free(&ColBType);
	MPI_Type_free(&SubMatType);

	free(AMat.data);
	free(BMat.data);
	free(CMat.data);
	free(aMatScatterv.size);
	free(bMatScatterv.size);
	free(aMatScatterv.offset);
	free(bMatScatterv.offset);

	if(rankWorld == 0){
		free(AFull);
		free(BFull);
		free(CFull);
	}

	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rankWorld);
	}

	return 0;
}
