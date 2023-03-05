#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <time.h>
#include "matrixio.h"

#define MAXITERATION 10000
#define DIMENSION 600
#define TAU -0.01
#define EPSILON 0.00005

typedef struct SlayData {
	double* lineCurr;
	double* lineNext;
	double* lineAnswer;
	double* matrix;
} SlayData;

void swapPointers(double** p1, double** p2) {
	double* temp = *p1;
	*p1 = *p2;
	*p2 = temp;
}

void prodMatLine(const double* mat, const double* oldLine, double* newLine) {
	for (int i = 0; i < DIMENSION; ++i) {
		newLine[i] = 0;
		for (int j = 0; j < DIMENSION; ++j) {
			newLine[i] += oldLine[j] * mat[i * DIMENSION + j];
		}
	}
}

void prodScalLine(const double scal, double* line) {
	for (int i = 0; i < DIMENSION; ++i) {
		line[i] *= scal;
	}
}

//minusLineLine(x2, x1) : (x2 - x1) --result--> x2
void minusLineLine1(double* line1, const double* line2) {
	for (int i = 0; i < DIMENSION; ++i) {
		line1[i] = line1[i] - line2[i];
	}
}

//minusLineLine(x2, x1) : (x1 - x2) --result--> x1
void minusLineLine2(double* line1, const double* line2) {
	for (int i = 0; i < DIMENSION; ++i) {
		line1[i] = line2[i] - line1[i];
	}
}

//lineNext = x(n+1), lineCurr = x(n)
void step(SlayData* data) {
	prodMatLine(data->matrix, data->lineCurr, data->lineNext);
	minusLineLine1(data->lineNext, data->lineAnswer);
	prodScalLine(TAU, data->lineNext);
    minusLineLine1(data->lineCurr, data->lineNext);
	//swapPointers(&(data->lineCurr), &(data->lineNext));
}

double measure(const double* line) {
	double measure = 0;
	for (int i = 0; i < DIMENSION; ++i) {
		double xi = line[i];
		measure += xi * xi;
	}
	return sqrt(measure);
}

bool conditionExit(const SlayData* data) {
	prodMatLine(data->matrix, data->lineCurr, data->lineNext);
	minusLineLine1(data->lineNext, data->lineAnswer);
	printf("%f\n", (measure(data->lineNext) / measure(data->lineAnswer)));
	return (measure(data->lineNext) / measure(data->lineAnswer)) >= EPSILON;
}


int main() {

	double* mat = (double*)malloc(sizeof(double) * (DIMENSION * DIMENSION));
	double* lineCurr = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineNext = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineAnswer = (double*)malloc(sizeof(double) * DIMENSION);

	entryMatrix(mat, DIMENSION, "coefMatrix.txt");
	entryLine(lineAnswer, DIMENSION, "lineAnswer.txt");

	SlayData data = { lineCurr, lineNext, lineAnswer, mat };
	
	struct timespec start, end;
	if( clock_gettime(CLOCK_MONOTONIC_RAW, &start) != 0 ) printf("Error of getting time.\n");

	unsigned int iterationCounter = 0;
	while (conditionExit(&data)) {
		if (iterationCounter < MAXITERATION) {
			step(&data);
			++iterationCounter;
		}
		else {
			break;
		}
	}

	if( clock_gettime(CLOCK_MONOTONIC_RAW, &end) != 0 ) printf("Error of getting time.\n");

	printLine(data.lineCurr, DIMENSION);
	printf("%d iterations to convergence.\n", iterationCounter);
	printf("Time spent: %f\n", end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec));

	free(mat);
	free(lineAnswer);
	free(lineCurr);
	free(lineNext);
	return 0;
}
