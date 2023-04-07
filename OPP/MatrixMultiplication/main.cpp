#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
//#include "matrixio.h"
#include <iostream>
#include <definescatterv.h>
#include <math.h>
#include <stdbool.h>

#define N_DIMENSIONS 2
#define MAKE_DIFFERENT_FROM_COLS(row_coord) row_coord+size
#define MAKE_DIFFERENT_FROM_ROWS(col_coord) col_coord+size*10

typedef struct ScattervParam{
	int* size;
	int* offset;
} ScattervParam;

typedef struct Matrix{
	int xSize;
	int ySize;
	double* data;
} Matrix;

int devideJobsBetweenProcesses(const int nProc, const int jobSize, int* job){
	int extraCells = jobSize % nProc;
	const int standartSize = jobSize / nProc;
	for(int i = 0; i < nProc; ++i){
		if (extraCells == 0){
			job[i] = standartSize;
		} else {
			job[i] = standartSize + 1;
			--extraCells;
		}
	}
	
}

void defineCardProcSize(size_t* xs, size_t* ys, const int nProc){
	for(int i = (size_t) sqrt(nProc); i > 0; --i){
		if((nProc / i) * i == nProc){
			*xs = i;
			*ys = nProc / i;
			break;
		}
	}
}

void init_cart(const int xSizeCart, const int ySizeCart, const int size, MPI_Comm* cartComm){
	int dimensionsSizeArr[2] = {xSizeCart, ySizeCart};
	int* periodicOrNot = (int*) calloc(size,size*sizeof(int));
	bool reorderBit = true;
	if( MPI_Cart_create(MPI_COMM_WORLD, N_DIMENSIONS, dimensionsSizeArr, 
					  	periodicOrNot, reorderBit, cartComm)!=MPI_SUCCESS ) printf("Error while MPI_Cart_create().\n");

	free(periodicOrNot);
}

//AMat * BMat = CMat
int main(int argc, char** argv){
	int size, rank;
	double timeStart, timeEnd;

	Matrix AMAt,BMat,CMat;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

//Создание топологии процессов - декартова решетка
	size_t xSizeCart, ySizeCart;
	if(argc == 1){
		defineCardProcSize(&xSizeCart, &ySizeCart, size);
	} else if(argc == 3) {
		xSizeCart = atoi(argv[1]);
		ySizeCart = atoi(argv[2]);
	}
	else {
		if(rank == 0) printf("Incorrect launching params.\n");
		return 0;
	}

	int rankCart;
	MPI_Comm cartComm;
	init_cart(xSizeCart, ySizeCart, size, &cartComm);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

//Создание коммуникатора объединяющего процессы, находящиеся в одной строке и в одном столбце
	int myCardCoords[2];
	int ignore[2];
	if(MPI_Cart_get(cartComm, N_DIMENSIONS, ignore, ignore, myCardCoords) != MPI_SUCCESS){printf("Error while MPI_Cart_get().\n");}

	//printf("%d - %d, %d\n", rank, MAKE_DIFFERENT_FROM_COLS(myCardCoords[0]), MAKE_DIFFERENT_FROM_ROWS(myCardCoords[1]));
	//MPI_Comm_rank(cartComm, &rank);

	MPI_Comm myRowComm, myColComm;
	if(MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_COLS(myCardCoords[0]), 0, &myRowComm)!= MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");
	if(MPI_Comm_split(cartComm, MAKE_DIFFERENT_FROM_ROWS(myCardCoords[1]), 0, &myColComm)!= MPI_SUCCESS)
		printf("Error while MPI_Comm_split().\n");

		/*
	int sum;
	MPI_Allreduce(&rank, &sum, 1, MPI_INT, MPI_SUM, myColComm);
	printf("%d; %d,%d -- %d\n", rank, myCardCoords[0], myCardCoords[1], sum);
*/

//Разделение строк и столбцов между процессами
	int aMatDisctrib[ySizeCart];
	int bMatDisctrib[xSizeCart];
	devideJobsBetweenProcesses(ySizeCart, AMat.ySize, aMatDisctrib);
	devideJobsBetweenProcesses(xSizeCart, BMat.xSize, bMatDisctrib);

//Создание и регистрация производных типов данных: столбец и строка матрицы
	MPI_Datatype RowAtype, ColBtypeUnaligned, ColBtype, SubMat, SubMatUnaligned;
	if( MPI_Type_contiguous(AMat.xSize, MPI_DOUBLE, &RowAType) != MPI_SUCCESS )printf("Error while MPI_Type_contiguous().\n");
	if( MPI_Type_vector(BMat.ySize, 1, BMat.ySize-1, MPI_DOUBLE, &ColBtypeUnaligned) != MPI_SUCCESS )printf("Error while MPI_Type_contiguous().\n");
	MPI_Type_commit(&RowAtype);
	MPI_Type_commit(&ColBtypeUnaligned);
	MPI_Type_create_resized(ColBtypeUnaligned, 0, sizeof(double), ColBtype);
	MPI_Type_commit(&ColBtype);
//Создание и регистрация производных типов: результирующие подматрицы
	if( MPI_Type_vector(aMatDisctrib[myCardCoords[0]], bMatDisctrib[myCardCoords[1]], 
		                BMat.ySize-1, MPI_DOUBLE, &SubMatUnaligned) != MPI_SUCCESS )printf("Error while MPI_Type_contiguous().\n");
	MPI_Type_commit(&SubMatUnaligned);
	int offsetBeginCMat = 0;
	for(int i = myCardCoords[1]-1; i >= 0; --i)
		offsetBeginCMat += aMatDisctrib[i]*BMAt.xSize;
	for(int i = myCardCoords[0]-1; i >= 0; --i)
		offsetBeginCMat += bMatDisctrib[i];
	
	MPI_Type_create_resized(SubMatUnaligned, offsetBeginCMat, bMatDisctrib[myCardCoords[0]], SubMat);
	MPI_Type_commit(&SubMat);

//

	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rank);
	}
	
	return 0;
}
