#include "stdio.h"
#include "stdlib.h"
#include "mpi.h"

#define LENGTH 15

void initArr(double* arr, const int size){
	for(int i = 0; i < size; i++)
		arr[i] = i;
}

int defineSegmentSize(const int sizeArr, const int amProc){
	static int extraCells = sizeArr % amProc;
	const static int standartSize = sizeArr / amProc;
	if (extraCells == 0) {
		return standartSize;
	}
	else {
		--extraCells;
		return standartSize + 1;
	}
}

void defineScatter(int* size, int* offset, const int nproc, const int sizeInputArr){
	int prevSegmentSize = defineSegmentSize(sizeInputArr, nproc);
	size[0] = prevSegmentSize;
	offset[0] = 0;
	for(int i = 1; i < nproc; i++){
		offset[i] = offset[i-1] + prevSegmentSize;
		prevSegmentSize = defineSegmentSize(sizeInputArr, nproc);
		size[i] = prevSegmentSize;
	}
}

int main(int argc, char** argv){
	int size;
	int rank;

	int size1 = 0, rank1 = 0;

	MPI_Init(&argc, &argv);

	MPI_Group universeGr;
	MPI_Comm_group(MPI_COMM_WORLD, &universeGr);

	int explNum[] = {0};
	MPI_Group calcGr;
	MPI_Comm calcComm;
	if( MPI_Group_excl(universeGr, 1, explNum, &calcGr) != MPI_SUCCESS ) return 1;
	if( MPI_Comm_create(MPI_COMM_WORLD, calcGr, &calcComm) ) return 2;

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);
	if(rank > 0){
		MPI_Comm_size(calcComm, &size1);
		MPI_Comm_rank(calcComm, &rank1);
	}

	printf("I'm %d of %d and %d of %d\n", rank, size, rank1, size1);

	double arr[LENGTH];
   	double arr1[LENGTH];
	double arr2[LENGTH];
	
	if(rank == 0){
		int sendCounts[size];
		int sendOffts[size];
		defineScatter(sendCounts, sendOffts, size, LENGTH);
		initArr(arr1, LENGTH);
		initArr(arr2, LENGTH);
	}

	MPI_Bcast(arr2, LENGTH, MPI_DOUBLE, 0, MPI_COMM_WORLD);

	
/*
	MPI_Scatter(arr1, LENGTH / size, MPI_DOUBLE, 
				arr, LENGTH / size, MPI_DOUBLE, 
				0, MPI_COMM_WORLD);

*/
	if(rank == 0){
		for(int i = 0; i < size; i++){
			printf("%d %d \n", sendCounts[i], sendOffts[i]);
		}
		printf("\n");
	}
}
