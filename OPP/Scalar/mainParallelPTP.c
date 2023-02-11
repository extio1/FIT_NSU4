#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

#define LENGTH 1000

#define LEFTARR 123
#define RIGHTARR 456

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

int main(int argc, char** argv) {
	int rank;
	int size;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if (rank == 0) {	
		double begin, end;
		double arr1[LENGTH];
		double arr2[LENGTH];
		initArr(arr1, LENGTH);
		initArr(arr2, LENGTH);

		begin = MPI_Wtime();
		int currenRecipNum = 1;
		int leftArrPos = 0;
		for (int i = 1; i < size; i++) {
			int proccesJobSize = defineSegmentSize(LENGTH, size-1);

			if(MPI_Send(arr2, LENGTH, MPI_DOUBLE, i, RIGHTARR, MPI_COMM_WORLD) != MPI_SUCCESS){
				printf("Error while sending a messange from %d to %d\n", 0, i);
			}
			if(MPI_Send(&arr1[leftArrPos], proccesJobSize, MPI_DOUBLE, i, LEFTARR, MPI_COMM_WORLD) != MPI_SUCCESS){
				printf("Error while sending a messange from %d to %d\n", 0, i);
			}
			leftArrPos += proccesJobSize;
		}
		
		MPI_Status stat;
		double answerSum = 0;
		double incomeVal;
		for (int i = 0; i < size-1; ++i) {
			if (MPI_Recv(&incomeVal, 1, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &stat) != MPI_SUCCESS) {
				printf("Error while recieving a messange from %d to 0 process\n", stat.MPI_SOURCE);
			} 
			else {
				answerSum += incomeVal;
			}
		}
		end = MPI_Wtime();

		printf("-------------------------------------------------------\n");
		printf("Total calculating and communication time is %f sec.\n", end - begin);
		printf("Answer is %f\n", answerSum);
	
	} else {

		MPI_Status stat;
		int leftArrLen = stat.count_lo;
		int rightArrLen = stat.count_lo;
		double* arr1;
		double* arr2;
		double start, end;

		start = MPI_Wtime();
		if( MPI_Probe(0, RIGHTARR, MPI_COMM_WORLD, &stat) == MPI_SUCCESS){

			rightArrLen = stat.count_lo / sizeof(double);
			arr2 = (double*) malloc(stat.count_lo);

			if( MPI_Recv(arr2, stat.count_lo, MPI_DOUBLE, 0, RIGHTARR, MPI_COMM_WORLD, &stat) != MPI_SUCCESS )
				printf("%d process, %d tag: The error occurred while getting the messange from 0 process", rank, stat.MPI_TAG);

			if( MPI_Probe(0, LEFTARR, MPI_COMM_WORLD, &stat) == MPI_SUCCESS ){

				leftArrLen = stat.count_lo / sizeof(double);
				arr1 = (double*) malloc(stat.count_lo);
				if( MPI_Recv(arr1, stat.count_lo, MPI_DOUBLE, 0, LEFTARR, MPI_COMM_WORLD, &stat) != MPI_SUCCESS )
					printf("%d process, %d tag: The error occurred while getting the messange from 0 process", rank, stat.MPI_TAG);

			} else {
				printf("Error while getting the left array by %d process\n", rank);
			}

		} else {
			printf("Error while getting the right array by %d process\n", rank);
		}

		double chunkOfAnswer = 0;
		for(int i = 0; i < leftArrLen; i++){
			for(int j = 0; j < rightArrLen; j++){
				chunkOfAnswer += arr1[i] * arr2[j];
			}
		}

		MPI_Send(&chunkOfAnswer, 1, MPI_DOUBLE, 0, 321, MPI_COMM_WORLD); // отсылает 0му, что насчитала
		end = MPI_Wtime();

		printf("The working time of #%d process is %f sec.\n", rank, end - start);
		free(arr1);
		free(arr2);
	}

	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rank);
	}
}
