#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include "matrixio.h"
#include <iostream>

#define MAXITERATION 100000
#define TAU -0.01
#define EPSILON 0.00005
#define DIMENSION 800
#define PRECISION 5

typedef struct SlayData {
	double* lineCurr;
	double* lineBuffer;
	double* lineAnswer;
	double* matrix;
} SlayData;

typedef struct ScattervParam{
	int* size;
	int* offset;
} ScattervParam;

void multScalArr(double* arr, const double scal, const int length){
	for(int i = 0; i < length; ++i){
		arr[i] *= scal;
	}
}

//определяет сколько строк матрицы, элементов вектора отдать процессу
int defineSegmentSize(const int nProc){
	static int extraCells = DIMENSION % nProc;
	const static int standartSize = DIMENSION / nProc;
	if (extraCells == 0) {
		return standartSize;
	}
	else {
		--extraCells;
		return standartSize + 1;
	}
}

//опеределяет параметры для MPI_Scatterv
void defineScatter(ScattervParam* paramVec, ScattervParam* paramMat, const int nproc){
	int prevSegmentSizeVec = defineSegmentSize(nproc);
	int prevSegmentSizeMat = prevSegmentSizeVec * DIMENSION;
	paramVec->size[0] = prevSegmentSizeVec;
	paramMat->size[0] = prevSegmentSizeMat;
	paramVec->offset[0] = 0;
	paramMat->offset[0] = 0;
	for(int i = 1; i < nproc; i++){
		paramVec->offset[i] = paramVec->offset[i-1] + prevSegmentSizeVec;
		paramMat->offset[i] = paramMat->offset[i-1] + prevSegmentSizeMat;
		prevSegmentSizeVec = defineSegmentSize(nproc);
		prevSegmentSizeMat = prevSegmentSizeVec * DIMENSION;
		paramVec->size[i] = prevSegmentSizeVec;
		paramMat->size[i] = prevSegmentSizeMat;
	}
}

void initZeroArr(double* arr, const int length){
	for(int i = 0; i < length; ++i){
		arr[i] = 0;
	}
}

double answerMeasureSquaredCalc(const double* localAnswer, const int lenght){
	double localMeasureAnswer=0;
	for(int i = 0; i < lenght; i++){
		double temp = localAnswer[i];
		localMeasureAnswer += temp*temp;
	}

	double answerMeasure;
	MPI_Allreduce(&localMeasureAnswer, &answerMeasure, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
	return answerMeasure;
}

bool exitFunction(SlayData* data, ScattervParam* vecParam, const double exitConstant, const int rank){
	static int precisionCounter = 0;

	double localExitSum = 0;
	for(int i = 0; i < vecParam->size[rank]; ++i){
		data->lineBuffer[i] = 0;
		for(int j = 0; j < DIMENSION; ++j){
			data->lineBuffer[i] += data->lineCurr[j] * data->matrix[i * DIMENSION + j]; 
		}
		
		double localExitSumt = data->lineBuffer[i] - data->lineAnswer[i];
		localExitSum += localExitSumt * localExitSumt;
	}

	double exitSum = 0;
	MPI_Allreduce(&localExitSum, &exitSum, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
	
	if(exitSum < exitConstant){
		++precisionCounter;
	} else {
		precisionCounter = 0;
	}
	
	return precisionCounter < PRECISION;
}

void printTimeProcesses(double timeStart, double timeEnd, int rank){
	double localTime = timeEnd - timeStart;
	double globalTime;

	MPI_Reduce(&localTime, &globalTime, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);
	
	//printf("Process #%d works for %f sec.\n", rank, localTime);
	if(rank == 0){
		printf("---------------------------\nTotal program calculating time: %f\n---------------------------\n", 
			globalTime);
	}
}

int main(int argc, char** argv){
	int size, rank;
	double timeStart, timeEnd;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	int size1[size];
	int size2[size];
	int offset1[size];
	int offset2[size];
	ScattervParam matParam = {size1, offset1};
	ScattervParam vecParam = {size2, offset2};

	double* matrixBuff;
	double* answerBuff;
	
	if(rank == 0){
		matrixBuff = (double*) malloc(sizeof(double)*(DIMENSION*DIMENSION));
		answerBuff = (double*) malloc(sizeof(double)*(DIMENSION));
		entryMatrix(matrixBuff, DIMENSION, "coefMatrix.txt");
		entryLine(answerBuff, DIMENSION, "lineAnswer.txt");
	}

	timeStart = MPI_Wtime();

	defineScatter(&vecParam, &matParam, size);

	SlayData localData;
	localData.matrix = (double*) malloc(sizeof(double)*matParam.size[rank]);
	localData.lineCurr = (double*) malloc(sizeof(double)*DIMENSION);
	localData.lineBuffer = (double*) malloc(sizeof(double)*DIMENSION);
	localData.lineAnswer = (double*) malloc(sizeof(double)*vecParam.size[rank]);

	initZeroArr(localData.lineCurr, DIMENSION);

	MPI_Scatterv(matrixBuff, matParam.size, matParam.offset, MPI_DOUBLE, 
				localData.matrix, matParam.size[rank], MPI_DOUBLE, 
				0, MPI_COMM_WORLD);
	MPI_Scatterv(answerBuff, vecParam.size, vecParam.offset, MPI_DOUBLE,
				localData.lineAnswer, vecParam.size[rank], MPI_DOUBLE,
				0, MPI_COMM_WORLD);

	double answerMeasureSquared = answerMeasureSquaredCalc(localData.lineAnswer, vecParam.size[rank]);
	double exitConstant = TAU*TAU*EPSILON*EPSILON*answerMeasureSquared;
	multScalArr(localData.matrix, TAU, matParam.size[rank]);
	multScalArr(localData.lineAnswer, TAU, vecParam.size[rank]);

	int iterationCounter = 0;

	//помимо определения момента выхода из цикла exitFunction 
	//подсчитывает локальный A*Xn и складывает результать в data.lineBuffer
	while(exitFunction(&localData, &vecParam, exitConstant, rank)){
		for(int i = 0; i < vecParam.size[rank]; ++i){ // этот цикл определяет локальный Xn - T*A*Xn + T*B
			localData.lineBuffer[i] = localData.lineCurr[i+vecParam.offset[rank]] - localData.lineBuffer[i] + localData.lineAnswer[i]; 
		}

		MPI_Allgatherv(localData.lineBuffer, vecParam.size[rank], MPI_DOUBLE,
				localData.lineCurr, vecParam.size, vecParam.offset, 
				MPI_DOUBLE, MPI_COMM_WORLD);

		++iterationCounter;
		if(iterationCounter >= MAXITERATION){
			printf("No converge for %d iteration!\n", iterationCounter);
			return 0;
		}
	}

	timeEnd = MPI_Wtime();
	printTimeProcesses(timeStart, timeEnd, rank);
	MPI_Barrier(MPI_COMM_WORLD);
	if(rank == 0){
		free(matrixBuff);
		free(answerBuff);
		//printLine(localData.lineCurr, DIMENSION);
		printf("%d iterations to convergence.\n", iterationCounter);
		//writeBinary(localData.lineCurr, DIMENSION);
	}

	free(localData.matrix);
	free(localData.lineAnswer);
	free(localData.lineCurr);
	free(localData.lineBuffer);

	if (MPI_Finalize() == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()\n", rank);
	}
	
	return 0;
}
