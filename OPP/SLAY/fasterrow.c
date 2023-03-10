#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <time.h>
#include "matrixio.h"

#define MAXITERATION 100000
#define DIMENSION 1500
#define TAU -0.01
#define EPSILON 0.00005

typedef struct SlayData {
	double* lineCurr;
	double* lineNext;
	double* lineB;
	double* matrix;
} SlayData;


void prodMatLine(const double* mat, const double* oldLine, double* newLine) {
	for (int i = 0; i < DIMENSION; ++i) {
		newLine[i] = 0;
		for (int j = 0; j < DIMENSION; ++j) {
			newLine[i] += oldLine[j] * mat[i * DIMENSION + j];
		}
	}
}

void prodScalMat(const double scal, double* mat) {
	for (int i = 0; i < DIMENSION; ++i) {
		for(int j = 0; j < DIMENSION; ++j){
			mat[i * DIMENSION + j] *= scal;
		}
	}
}

void prodScalLine(const double scal, double* line) {
	for (int i = 0; i < DIMENSION; ++i) {
		line[i] *= scal;
	}
}

double measureSquared(const double* line) {
	double measure = 0;
	for (int i = 0; i < DIMENSION; ++i) {
		measure += line[i] * line[i];
	}
	return measure;
}

bool conditionExit(const SlayData* data, const double exitConstant) {
	double exitSum = 0;

	prodMatLine(data->matrix, data->lineCurr, data->lineNext);

	for(int i = 0; i < DIMENSION; ++i){
		double tempSquare = data->lineNext[i] - data->lineB[i];
		exitSum += tempSquare * tempSquare; 
	}

	return exitSum >= exitConstant;
}

void initZeroArr(double* arr, const int length){
	for(int i = 0; i < length; ++i){
		arr[i] = 0;
	}
}

int main() {

	double* mat = (double*)malloc(sizeof(double) * (DIMENSION * DIMENSION));
	double* lineCurr = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineNext = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineB = (double*)malloc(sizeof(double) * DIMENSION);

	entryMatrix(mat, DIMENSION, "coefMatrix.txt");
	entryLine(lineB, DIMENSION, "lineAnswer.txt");

	SlayData data = { lineCurr, lineNext, lineB, mat };
	struct timespec start, end;

	if( clock_gettime(CLOCK_MONOTONIC_RAW, &start) != 0 ) printf("Error of getting time.\n");

	initZeroArr(data.lineCurr, DIMENSION);

	double exitConstant = EPSILON * EPSILON * TAU * TAU * measureSquared(data.lineB);
	prodScalMat(TAU, data.matrix);
	prodScalLine(TAU, data.lineB);

	unsigned int iterationCounter = 0;
	while (conditionExit(&data, exitConstant)) {
		if (iterationCounter < MAXITERATION) {
			//printLine(data.lineNext, DIMENSION);
			for(int i = 0; i < DIMENSION; ++i){
				data.lineCurr[i] = data.lineCurr[i] - data.lineNext[i] + data.lineB[i];
			}
			
			++iterationCounter;
		}
		else {
			break;
		}
	}

	if( clock_gettime(CLOCK_MONOTONIC_RAW, &end) != 0 ) printf("Error of getting time.\n");

	printf("%d iterations to convergence.\n", iterationCounter);
	printf("Time spent: %f\n", end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec));

	free(data.matrix);
	free(data.lineB);
	free(data.lineCurr);
	free(data.lineNext);
	return 0;
}
