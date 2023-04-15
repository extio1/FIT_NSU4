#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include "matio.h"
#include <iostream>
#include <math.h>
#include <stdbool.h>
#include <string.h>

#define N1 14
#define N2 3
#define N3 7
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

void make_col_row_types(MPI_Datatype* RowAType, MPI_Datatype* ColBTypeFull, MPI_Datatype* ColBTypePart, const int partBsizeX){ 
	//Создание и регистрация производных типов данных: столбец и строка матрицы

	MPI_Datatype ColBtypeUnalignedFull, ColBtypeUnalignedPart;
	if (MPI_Type_contiguous(N2, MPI_DOUBLE, RowAType) != MPI_SUCCESS) printf("Error while MPI_Type_contiguous().\n");
	MPI_Type_commit(RowAType);

	if (MPI_Type_vector(N2, 1, N3, MPI_DOUBLE, &ColBtypeUnalignedFull) != MPI_SUCCESS) printf("Error while MPI_Type_vector().\n");
	if (MPI_Type_vector(N2, 1, partBsizeX, MPI_DOUBLE, &ColBtypeUnalignedPart) != MPI_SUCCESS) printf("Error while MPI_Type_vector().\n");
	MPI_Type_commit(&ColBtypeUnalignedPart);
	MPI_Type_commit(&ColBtypeUnalignedFull);

	MPI_Type_create_resized(ColBtypeUnalignedFull, 0, sizeof(double), ColBTypeFull);
	MPI_Type_create_resized(ColBtypeUnalignedPart, 0, sizeof(double), ColBTypePart);
	MPI_Type_commit(ColBTypeFull);
	MPI_Type_commit(ColBTypePart);

	MPI_Type_free(&ColBtypeUnalignedFull);
	MPI_Type_free(&ColBtypeUnalignedPart);
}

void make_submat_types(MPI_Datatype* SubMatType, const int* aMatDisctrib, const int* bMatDisctrib, const int xSizeCart, const int ySizeCart, const int size){ 
	// В массиве на j * xSizeCart * i позиции хранится тип подматрицы для i, j процесса из решетки процессов

	MPI_Datatype* SubMatUnaligned = (MPI_Datatype*) malloc(sizeof(MPI_Datatype)*size);
	for(int i = 0; i < xSizeCart; ++i){
		for(int j = 0; j < ySizeCart; ++j){
			if (MPI_Type_vector(aMatDisctrib[j], bMatDisctrib[i],
				N3 - 1, MPI_DOUBLE, &SubMatUnaligned[j*xSizeCart+i]) != MPI_SUCCESS){
				printf("Error while MPI_Type_vector().\n");
			}
				MPI_Type_commit(&SubMatUnaligned[j*xSizeCart+i]);
		}
	}

	//MPI_Type_create_resized(SubMatUnaligned[0], 0, bMatDisctrib[myCardCoords[0]], SubMatType[0]);
	//MPI_Type_commit(&SubMatType[0]);
	for(int i = 0; i < xSizeCart; ++i){
		for(int j = 0; j < ySizeCart; ++j){
			int offsetBeginCMat = 0;
			for (int n = j - 1; n >= 0; --n)
				offsetBeginCMat += aMatDisctrib[n] * N3;
			for (int m = i - 1; m >= 0; --m)
				offsetBeginCMat += bMatDisctrib[m];

			MPI_Type_create_resized(SubMatUnaligned[j * xSizeCart + i], offsetBeginCMat, bMatDisctrib[i], &SubMatType[j * xSizeCart + i]);
			MPI_Type_commit(&SubMatType[j * xSizeCart + i]);
		}
	}

/*
	int offsetBeginCMat = 0;
	for (int i = myCardCoords[1] - 1; i >= 0; --i)
		offsetBeginCMat += aMatDisctrib[i] * N3;
	for (int i = myCardCoords[0] - 1; i >= 0; --i)
		offsetBeginCMat += bMatDisctrib[i];

	MPI_Type_create_resized(SubMatUnaligned, offsetBeginCMat, bMatDisctrib[myCardCoords[0]], SubMatType);
	MPI_Type_commit(SubMatType);


	MPI_Type_free(&SubMatUnaligned);
	*/
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
		aOffsets[i] = aOffsets[i-1] + aMatDisctrib[i-1];
	}
	for(int i = 1; i < xSizeCart; ++i){
		bOffsets[i] = bOffsets[i-1] + bMatDisctrib[i-1];
	}

	a->size = aMatDisctrib;
	b->size = bMatDisctrib;
	a->offset = aOffsets;
	b->offset = bOffsets;
}

void make_submat_type(MPI_Datatype* types, const ScattervParam* aMatDisctrib, const ScattervParam* bMatDisctrib, const int xSizeCart, const int ySizeCart){
	int sizeOfArray[N_DIMENSIONS] = {N1, N3};
	int sizeOfSubarray[N_DIMENSIONS];
	int startCoords[N_DIMENSIONS];

	for(int i = 0; i < xSizeCart; ++i){
		for(int j = 0; j < ySizeCart; ++j){
			MPI_Datatype type;
			sizeOfSubarray[1] = bMatDisctrib->size[i];
			sizeOfSubarray[0] = aMatDisctrib->size[j];
			startCoords[1] = bMatDisctrib->offset[i];
			startCoords[0] = aMatDisctrib->offset[j];
			printf("=====(%d,%d) %d %d; %d %d\n", j, i, sizeOfSubarray[0], sizeOfSubarray[1], startCoords[0], startCoords[1]);

			if( MPI_Type_create_subarray(N_DIMENSIONS, sizeOfArray, sizeOfSubarray, startCoords, MPI_ORDER_C, MPI_DOUBLE, &type) != MPI_SUCCESS){
				printf("Error while MPI_Type_create_subarray().\n");
			}
			MPI_Type_commit(&type);

			long int lb, extent;
			MPI_Type_get_extent(type, &lb, &extent);
			printf("%d ---- %ld %ld\n", i, lb, extent);

			types[j*xSizeCart+i] = type;
		}
	}
}

void assignRootSubmat(double* CFull, const Matrix* CMat){
	for(int j = 0; j < CMat->ySize; ++j){
		for(int i = 0; i < CMat->xSize; ++i){
			CFull[j*N3+i] = CMat->data[j*CMat->xSize+i];
		}
	}
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

	// Чтение матриц из файла 0-м процессом
	// Размерности матриц зафиксированы макросами N1, N2, N3
	if(rankWorld == 0){
		AFull = (double*) malloc(sizeof(double) * N1*N2);
		BFull = (double*) malloc(sizeof(double) * N2*N3);
		CFull = (double*) malloc(sizeof(double) * N1*N3);
		enter_matrix(AFull, N2, "AMat.txt");
		enter_matrix(BFull, N3, "BMat.txt");
	}
	
	// Создание топологии процессов - декартова решетка
	// Если не были введены размеры решетки, то подбираются два наименее отличных
	// числа, в произведении дающие число процессов
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

	int rankCart; //ранг каждого процесса в коммуникаторе декартовой решетки
	MPI_Comm cartComm; 
	init_cart(xSizeCart, ySizeCart, size, &cartComm); //функция, непосредственно создающая топологию
	MPI_Comm_rank(cartComm, &rankCart);

	//Создание коммуникатора объединяющего процессы, находящиеся в одной строке и в одном столбце
	int myCardCoords[2]; //тут хранятся координаты в декартовой решетки для каждого процесса
	int ignore[2];
	if (MPI_Cart_get(cartComm, N_DIMENSIONS, ignore, ignore, myCardCoords) != MPI_SUCCESS) { printf("Error while MPI_Cart_get().\n"); }

	//  В новом коммутаторе процессы будут иметь нумерацию в соответсвии с их координатой,
	// в каждом столбце/строке координата по y/x уникальна в нем/ней.
	//  К тому же процессы, осуществляющие broadcast на 3 и 4 шаге будут иметь ранг 0 внутри коммуникатора их
	// строки и столбца.
	MPI_Comm myRowComm, myColComm;
	if (MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_COLS(myCardCoords[1]), myCardCoords[0], &myRowComm) != MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");
	if (MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_ROWS(myCardCoords[0]), myCardCoords[1], &myColComm) != MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");

	//  Данная структура по сути будет сопоставлять значению x в сетке процессов количество столбцов в подматрице В,
	// а координате y количество строк в подматрице А, распределенных по декартовой топологии процессов.
	// Стоит заметить, что другая размерность матрицы в обоих случаях будет равна N2
	// из-за чего умножение такие горизонтальных и вертикальных полос корректно.
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

	// Создание типов данных.
	// !Важно, что каждый процесс еще имеет свой тип данных для столбца матрицы, потому что 
 	//  offset до начала следующего блока у каждого процесса свой, в том числе они отличны
	//  от offset для столбца изначальной матрицы.
	MPI_Datatype RowAType, ColBTypeFull, ColBTypePart;
	//MPI_Datatype* SubmatType = (MPI_Datatype*) malloc(sizeof(MPI_Datatype)*(size));
	make_col_row_types(&RowAType, &ColBTypeFull, &ColBTypePart, bMatScatterv.size[myCardCoords[0]]);
	//make_submat_types(SubmatType, aMatScatterv.size, bMatScatterv.size, xSizeCart, ySizeCart, size);

	MPI_Datatype* SubmatTypes;
	if(rankWorld == 0){
		SubmatTypes = (MPI_Datatype*) malloc(sizeof(MPI_Datatype)*(size));
		make_submat_type(SubmatTypes, &aMatScatterv, &bMatScatterv, xSizeCart, ySizeCart);
	}

	//Определяем подматрицы A и В, с которыми будет работать каждый процесс в декартовой топологии
	AMat.xSize = N2; 								 AMat.ySize = aMatScatterv.size[myCardCoords[1]];
	BMat.xSize = bMatScatterv.size[myCardCoords[0]]; BMat.ySize = N2;
	CMat.xSize = bMatScatterv.size[myCardCoords[0]]; CMat.ySize = aMatScatterv.size[myCardCoords[1]];
	AMat.data = (double*)malloc(sizeof(double) * AMat.ySize * AMat.xSize);
	BMat.data = (double*)malloc(sizeof(double) * BMat.xSize * BMat.ySize);
	CMat.data = (double*)calloc(CMat.xSize*CMat.ySize, sizeof(double));

	// 1Шаг: рассылаем из (0, 0) на крайнюю верхнюю строку и левый столбец
	// 	горизонтальные и вертикальные полосы определенного для каждого
	// 	процесса размера
	if(myCardCoords[0] == 0){
		MPI_Scatterv(AFull, aMatScatterv.size, aMatScatterv.offset, RowAType,
					 AMat.data, AMat.ySize*N2, MPI_DOUBLE, 0, myColComm);
	}

	if(myCardCoords[1] == 0){
		MPI_Scatterv(BFull, bMatScatterv.size, bMatScatterv.offset, ColBTypeFull,
					 BMat.data, BMat.xSize, ColBTypePart, 0, myRowComm);
	}

	// 2Шаг: каждый процесс на верхней строке и левом столбце делает
	//  broadcast в пределах коммуникатора своего столбца и строки соответсвенно
	MPI_Bcast(BMat.data, bMatScatterv.size[myCardCoords[0]], ColBTypePart, 0, myColComm);
	MPI_Bcast(AMat.data, aMatScatterv.size[myCardCoords[1]], RowAType, 0, myRowComm);

/*
	if(myCardCoords[0] == 0 && myCardCoords[1] == 3){
		printf("x: %d, y: %d\n", BMat.xSize, AMat.ySize);
		printf("A:\n");
		print_matrix(AMat.data, AMat.xSize, AMat.ySize);
		printf("B:\n");
		print_matrix(BMat.data, BMat.xSize, BMat.ySize);
	}
*/

	// 3Шаг: непосредственно умножение
	for (int i = 0; i < AMat.ySize; ++i) {
        for (int j = 0; j < BMat.xSize; ++j) {
            for (int k = 0; k < BMat.ySize; ++k)
                CMat.data[i * BMat.xSize + j] += AMat.data[i * AMat.xSize + k] * BMat.data[k * BMat.xSize + j];
        }
    }

/*
    if(myCardCoords[0] == 0 && myCardCoords[1] == 3){
		printf("Cx: %d, Cy: %d\n", CMat.xSize, CMat.ySize);
		printf("C:\n");
		print_matrix(CMat.data, CMat.xSize, CMat.ySize);
	}
*/

	/*
    for (int i = 0; i < n; ++i){
		for (int k = 0; k < n; ++k){
			for (int j = 0; j < n; ++j){
				CMat.data[i * n + j] += AMat.data[i * n + k] * BMat.data[k * n + j];
			}
		}
    }
    */

    // 4Шаг: собираем подматрицы в процессе 0
    if(myCardCoords[0] == 0 && myCardCoords[1] == 0){
    	assignRootSubmat(CFull, &CMat);
    	MPI_Status status;
    	for(int i = 1; i < size; ++i){
	    	if(MPI_Probe(MPI_ANY_SOURCE, MPI_ANY_TAG, cartComm, &status) == MPI_SUCCESS){
	    		int senderCoord[2];
	    		MPI_Cart_coords(cartComm, status.MPI_SOURCE, N_DIMENSIONS, senderCoord);
	    		printf("RECEIVING FROM %d,%d\n", senderCoord[0], senderCoord[1]);
	    		if(MPI_Recv(CFull, 1, SubmatTypes[senderCoord[1]*xSizeCart+senderCoord[0]], 
	    				    status.MPI_SOURCE, status.MPI_TAG, cartComm, &status) != MPI_SUCCESS){
	    			printf("Error while MPI_Recv() from %d to left upper process.\n", status.MPI_SOURCE);
	    		}
	    	}
	    }
	    /*
	    for(int i = 0; i < xSizeCart; ++i){
	    	for(int j = 0; j < ySizeCart; ++j){
	    		if(MPI_Recv(CFull, 1, SubmatTypes[j*xSizeCart+i], j*xSizeCart+i, MPI_ANY, MPI_COMM_WORLD, &status) != MPI_SUCCESS){
	    			printf("Error while MPI_Recv() from %d to left upper process.\n", status.MPI_SOURCE);
	    		}
	    	}
	    }*/
    } else {
    	int cartRankRoot;
    	int rootCoords[2] = {0,0};
		MPI_Cart_rank(cartComm, rootCoords, &cartRankRoot);
		printf("I SEND: %d,%d to %d\n", myCardCoords[0], myCardCoords[1], cartRankRoot);
		if(MPI_Send(CMat.data, CMat.xSize*CMat.ySize, MPI_DOUBLE, cartRankRoot, 0, cartComm) != MPI_SUCCESS){
			printf("Error while MPI_Send() from %d to left upper process.\n", rankWorld);
		}
    }


    if(rankWorld == 0){
    	print_matrix(CFull, N3, N1);
    }
/*

//Освобождение ресурсов
	MPI_Type_free(&RowAType);
	MPI_Type_free(&ColBType);
	//MPI_Type_free(&SubMatType);

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
*/
	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rankWorld);
	}

	return 0;
}
