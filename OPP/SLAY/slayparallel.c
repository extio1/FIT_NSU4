#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>
#include "matrixio.h"
#include <iostream>

#define MAXITERATION 100000
#define TAU 0.01
#define EPSILON 0.00005
#define DIMENSION 5
#define PRECISION 500

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
	std::cout << localExitSum << std::endl;
	MPI_Allreduce(&localExitSum, &exitSum, 1, MPI_DOUBLE, MPI_SUM, MPI_COMM_WORLD);
	std::cout << "EXIT:" << exitSum << std::endl;
	//printf("%f\n", exitSum);

/*
	//собираем во всех процессах TAU*MATRIX*Xn
	MPI_Allgatherv(data.lineBuffer, vecParam.size[rank], MPI_DOUBLE,
				data.lineBuffer, vecParam.size, vecParam.offset, 
				MPI_DOUBLE, MPI_COMM_WORLD);
*/
	
	if(exitSum < exitConstant){
		++precisionCounter;
	} else {
		++precisionCounter;
		//precisionCounter = 0;
	}

	//printf("%d\n", precisionCounter);
	return precisionCounter < PRECISION;
	//return false;
}

int main(int argc, char** argv){
	int size, rank;
	double end, start;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	int size1[size];
	int size2[size];
	int offset1[size];
	int offset2[size];
	ScattervParam matParam = {size1, offset1};
	ScattervParam vecParam = {size2, offset2};

	defineScatter(&vecParam, &matParam, size);

	double* matrixBuff;
	double* answerBuff;

	SlayData localData;
	localData.matrix = (double*) malloc(sizeof(double)*matParam.size[rank]);
	localData.lineCurr = (double*) malloc(sizeof(double)*DIMENSION);
	localData.lineBuffer = (double*) malloc(sizeof(double)*DIMENSION);
	localData.lineAnswer = (double*) malloc(sizeof(double)*vecParam.size[rank]);
	//printf("%p\n%p\n%p\n%p\n", mat, lineCurr, lineBuffer, lineAnswer);

	initZeroArr(localData.lineCurr, DIMENSION);
	if(rank == 0){
		matrixBuff = (double*) malloc(sizeof(double)*(DIMENSION*DIMENSION));
		answerBuff = (double*) malloc(sizeof(double)*(DIMENSION));
		entryMatrix(matrixBuff, DIMENSION, "coefMatrix.txt");
		entryLine(answerBuff, DIMENSION, "lineAnswer.txt");
	}

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

	int interationCounter = 0;

	//помимо определения момента выхода из цикла exitFunction 
	//подсчитывает локальный A*Xn и складывает результать в data.lineBuffer
	while(exitFunction(&localData, &vecParam, exitConstant, rank)){
		for(int i = 0; i < vecParam.size[rank]; ++i){ // этот цикл определяет локальный Xn - T*A*Xn + T*B
			localData.lineBuffer[i] = localData.lineCurr[i] - localData.lineBuffer[i] + localData.lineAnswer[i]; 
		}
		MPI_Barrier(MPI_COMM_WORLD);
		
		//printf("%d ", rank);
		
		//printLine(localData.lineCurr, vecParam.size[rank]);
		MPI_Allgatherv(localData.lineBuffer, vecParam.size[rank], MPI_DOUBLE,
				localData.lineCurr, vecParam.size, vecParam.offset, 
				MPI_DOUBLE, MPI_COMM_WORLD);

		//printLine(localData.lineCurr, DIMENSION);

		++interationCounter;
		if(interationCounter >= MAXITERATION){
			printf("No converge for %d iteration!\n", interationCounter);
			return 0;
		}
	}

	if(rank == 0){
		free(matrixBuff);
		free(answerBuff);
		printLine(localData.lineCurr,DIMENSION);
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
