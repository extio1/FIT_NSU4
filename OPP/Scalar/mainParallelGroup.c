#include "stdio.h"
#include "stdlib.h"
#include "mpi.h"

#define LENGTH 100000

void initArr(double* arr, const int size){
	for(int i = 0; i < size; i++)
		arr[i] = 1;
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

	double timeStart, timeEnd;

	MPI_Init(&argc, &argv);

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

   	double* arr1Full;
	double* arr2 = (double*) malloc(sizeof(double) * LENGTH);
	int sendCounts[size];
	int sendOffts[size];
	if(rank == 0){
		arr1Full = (double*) malloc(sizeof(double) * LENGTH);
		initArr(arr1Full, LENGTH);
		initArr(arr2, LENGTH);
	}

	timeStart = MPI_Wtime();

	defineScatter(sendCounts, sendOffts, size, LENGTH);
	double* arr1Part = (double*) malloc(sizeof(double) * sendCounts[rank]);

	MPI_Bcast(arr2, LENGTH, MPI_DOUBLE, 0, MPI_COMM_WORLD);

	MPI_Scatterv(arr1Full, sendCounts, sendOffts, MPI_DOUBLE, 
				 arr1Part, sendCounts[rank], MPI_DOUBLE, 
				 0, MPI_COMM_WORLD);


	double partAnswer = 0;
	for(int i = 0; i < sendCounts[rank]; ++i){
		for(int j = 0; j < LENGTH; j++){
			partAnswer += arr1Part[i] * arr2[j];
		}
	}

	double answer;
	MPI_Reduce(&partAnswer, &answer, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

	timeEnd = MPI_Wtime();
	double localTime = timeEnd - timeStart;
	double globalTime;

	MPI_Reduce(&localTime, &globalTime, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
	
	printf("Process #%d works for %f sec.\n", rank, localTime);
	MPI_Barrier(MPI_COMM_WORLD);
	if(rank == 0){
		printf("-------------------------------------------------------------------------\n");
		printf("The answer is: %f\nTotal calculating and communication time is %f sec.\n", answer, globalTime);
		printf("-------------------------------------------------------------------------\n");
	}



	free(arr1Part);
	free(arr2);
	if(rank == 0){
		free(arr1Full);
	}

	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rank);
	}
}
