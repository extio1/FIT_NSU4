#include <stdlib.h>
#include <stdio.h>
#include <omp.h>
#include "matrixio.h"
#include <iostream>

#define MAXITERATION 100000
#define TAU -0.01
#define EPSILON 0.00005
#define DIMENSION 1200
#define PRECISION 5

typedef struct SlayData {
	double* lineCurr;
	double* lineBuffer;
	double* lineB;
	double* matrix;
} SlayData;

bool convergeFlag = true;
int iterationCounter = 0;
double bMeasureSquared = 0;
double exitConstant = 0;
double exitSum = 0;
int precisionCounter = 0;
double* matrix = (double*) malloc(sizeof(double)*(DIMENSION*DIMENSION));
double* lineCurr = (double*) malloc(sizeof(double)*(DIMENSION));
double* lineBuff = (double*) malloc(sizeof(double)*(DIMENSION));
double* b = (double*) malloc(sizeof(double)*(DIMENSION));
SlayData data = {lineCurr, lineBuff, b, matrix};

void bMeasureSquaredCalc();
void initZeroArr(double* arr);
void multScalLine(double* arr, const double scal);
void multScalMatrix(double* arr, const double scal);
bool exitFunction();

int main(int argc, char** argv){
	double timeStart, timeEnd;

	entryMatrix(data.matrix, DIMENSION, "coefMatrix.txt");
	entryLine(data.lineB, DIMENSION, "vecB.txt");

	#pragma omp parallel shared(data, bMeasureSquared, exitConstant, convergeFlag, iterationCounter, timeEnd, timeStart)
	{
	#pragma omp reduction(min:timeEnd)
	timeStart = omp_get_wtime();

	initZeroArr(data.lineCurr);

	bMeasureSquaredCalc();

	#pragma omp single
	exitConstant = TAU*TAU*EPSILON*EPSILON*bMeasureSquared;

	multScalLine(data.lineB, TAU);
	multScalMatrix(data.matrix, TAU);

	while(exitFunction()){
		#pragma omp for schedule(guided, 100)
		for(int i = 0; i < DIMENSION; ++i){
			data.lineCurr[i] = data.lineCurr[i] - data.lineBuffer[i] + data.lineB[i]; 
		}

		//есть неявный barrier после single
		#pragma omp single 
		++iterationCounter;

		if(iterationCounter >= MAXITERATION){
			#pragma omp single
			{
			convergeFlag = false;
			printf("No converge for %d iteration!\n", iterationCounter);
			}
			break;
		}
	}
	#pragma omp reduction(max:timeEnd)
	timeEnd = omp_get_wtime();
	}

	
	if(convergeFlag){
		printf("%d iterations to convergence.\n", iterationCounter);
	}

	printf("Execution %f seconds\n", timeEnd - timeStart);

	free(matrix);
	free(b);

	return 0;
}

void bMeasureSquaredCalc(){
	#pragma omp for reduction(+:bMeasureSquared) schedule(guided, 10)
	for(int i = 0; i < DIMENSION; i++){
		double temp = data.lineB[i];
		bMeasureSquared += temp*temp;
	}
}

void initZeroArr(double* arr){
	#pragma omp for
	for(int i = 0; i < DIMENSION; ++i){
		arr[i] = 0;
	}
}

void multScalLine(double* arr, const double scal){
	#pragma omp for
	for(int i = 0; i < DIMENSION; ++i){
		arr[i] *= scal;
	}
}

void multScalMatrix(double* arr, const double scal){
	#pragma omp for
	for(int i = 0; i < DIMENSION; ++i){
		for(int j = 0; j < DIMENSION; ++j){
			arr[i * DIMENSION + j] *= scal;
		}
	}

}

bool exitFunction(){
	exitSum = 0;

	#pragma omp for reduction(+ : exitSum)
	for(int i = 0; i < DIMENSION; ++i){
		data.lineBuffer[i] = 0;
		for(int j = 0; j < DIMENSION; ++j){
			data.lineBuffer[i] += data.lineCurr[j] * data.matrix[i * DIMENSION + j]; 
		}
		
		double exitSumt = data.lineBuffer[i] - data.lineB[i];
		exitSum += exitSumt * exitSumt;
	}

	#pragma omp single
	if(exitSum < exitConstant){
		++precisionCounter;
	} else {
		precisionCounter = 0;
	}

	return precisionCounter < PRECISION;
}
