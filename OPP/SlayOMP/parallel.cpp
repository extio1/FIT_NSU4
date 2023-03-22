#include <stdlib.h>
#include <stdio.h>
#include <omp.h>
#include "matrixio.h"
#include <iostream>
#include <string>
#include <unistd.h>

#define MAXITERATION 100000
#define TAU -0.01
#define EPSILON 0.00005
#define DIMENSION 1500
#define PRECISION 5

typedef struct SlayData {
	double* lineCurr;
	double* lineBuffer;
	double* lineB;
	double* matrix;
} SlayData;

double timeAll = 0;
int precisionCounter = 0;
double exitSum = 0;
double buffer = 0;
double* matrix = (double*) malloc(sizeof(double)*(DIMENSION*DIMENSION));
double* lineCurr = (double*) malloc(sizeof(double)*(DIMENSION));
double* lineBuff = (double*) malloc(sizeof(double)*(DIMENSION));
double* b = (double*) malloc(sizeof(double)*(DIMENSION));
SlayData data = {lineCurr, lineBuff, b, matrix};

double bMeasureSquaredCalc();
void initZeroArr(double* arr);
void multScalLine(double* arr, const double scal);
void multScalMatrix(double* arr, const double scal);
bool exitFunction(const double);

int main(int argc, char** argv){
	double timeStart = 0, timeEnd = 0;

	bool convergeFlag = true;
	int iterationCounter = 0;
	double bMeasureSquared = 0;
	double exitConstant = 0;

	entryMatrix(data.matrix, DIMENSION, "coefMatrix.txt");
	entryLine(data.lineB, DIMENSION, "vecB.txt");

	#pragma omp parallel shared(data, exitConstant, exitSum, convergeFlag, iterationCounter, timeEnd, timeStart, bMeasureSquared, buffer)
	{
	#pragma omp atomic
	timeStart += omp_get_wtime();

	initZeroArr(data.lineCurr);

	double bMeasureSquared = bMeasureSquaredCalc();

	#pragma omp single
	exitConstant = TAU*TAU*EPSILON*EPSILON*bMeasureSquared;

	multScalLine(data.lineB, TAU);
	multScalMatrix(data.matrix, TAU);

	while(exitFunction(exitConstant)){
		#pragma omp for schedule(guided, 200)
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
	#pragma omp atomic
	timeEnd += omp_get_wtime();
	#pragma omp barrier
	#pragma omp single
	timeAll = (timeEnd - timeStart) / (double) omp_get_num_threads();
	}

	
	if(convergeFlag){
		printf("%d iterations to convergence.\n", iterationCounter);
		printf("Execution %f seconds\n", timeAll);
		printf("--------------\n");
		FILE* f = fopen("filetime.txt", "w");
		fprintf(f, "%f\n", timeAll);
		fclose(f);
	}



	free(matrix);
	free(b);

	return 0;
}

double bMeasureSquaredCalc(){
	#pragma omp for reduction(+:buffer) schedule(guided, 10)
	for(int i = 0; i < DIMENSION; i++){
		double temp = data.lineB[i];
		buffer += temp*temp;
	}
	return buffer;
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

bool exitFunction(const double exitConstant){
	#pragma omp single
	exitSum = 0;

	#pragma omp for reduction(+ : exitSum) schedule(guided, 200)
	for(int i = 0; i < DIMENSION; ++i){
		data.lineBuffer[i] = 0;
		for(int j = 0; j < DIMENSION; ++j){
			data.lineBuffer[i] += data.lineCurr[j] * data.matrix[i * DIMENSION + j]; 
		}
		
		double exitSumt = data.lineBuffer[i] - data.lineB[i];
		exitSum += exitSumt * exitSumt;
	}

	//std::cout << exitSum << ' ' << exitConstant << '\n';
	#pragma omp single
	if(exitSum < exitConstant){
		++precisionCounter;
	} else {
		precisionCounter = 0;
	}
	#pragma omp barrier

	return precisionCounter < PRECISION;
}
