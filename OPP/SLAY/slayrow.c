#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "matrixio.h"

#define DIMENSION 4
#define TAU 0.01
#define EPSILON 0.00005


typedef struct SlayData{
	double* lineCurr;
	double* lineNext;
	double* lineAnswer;
	double* matrix;
} SlayData;

void swapPointers(double** p1, double** p2){
	double* temp = *p1;
	*p1 = *p2;
	*p2 = temp;
}

void prodMatLine(const double* mat, const double* oldLine, double* newLine){
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
void minusLineLine(double* line2, const double* line1) {
	for (int i = 0; i < DIMENSION; ++i) {
		line2[i] -= line1[i];
	}
}

//lineNext = x(n+1), lineCurr = x(n)
void step(SlayData* data){
	prodMatLine(data->matrix, data->lineCurr, data->lineNext);
	minusLineLine(data->lineNext, data->lineAnswer);
	prodScalLine(TAU, data->lineNext);
	minusLineLine(data->lineNext, data->lineCurr);
	swapPointers(&(data->lineCurr), &(data->lineNext));
}

double measure(const double* line){
	double measure = 0;
	for(int i = 0; i < DIMENSION; ++i){
		double xi = line[i];
		measure += xi*xi;
	}
	return sqrt(measure);
}

bool conditionExit(const SlayData* data){
	prodMatLine(data->matrix, data->lineCurr, data->lineNext);
	minusLineLine(data->lineNext, data->lineAnswer);
	//printf("%f", (measure(data->lineNext) / measure(data->lineAnswer)));
	return (measure(data->lineNext) / measure(data->lineAnswer)) >= EPSILON;
}


int main() {

	double* mat = (double*) malloc(sizeof(double) * (DIMENSION * DIMENSION));
	double* lineCurr = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineNext = (double*)malloc(sizeof(double) * DIMENSION);
	double* lineAnswer = (double*)malloc(sizeof(double) * DIMENSION);

	entryMatrix(mat, DIMENSION, "coefMatrix.txt");
	entryLine(lineCurr, DIMENSION, "lineInitial.txt");
	entryLine(lineAnswer, DIMENSION, "lineAnswer.txt");

	SlayData data = {lineCurr, lineNext, lineAnswer, mat};

	while(conditionExit(&data)){
		step(&data);
	}
	

	printLine(data.lineCurr, DIMENSION);
	prodMatLine(data.matrix, data.lineCurr, data.lineNext);
	printLine(data.lineNext, DIMENSION);


	free(mat);
	free(lineAnswer);
	free(lineCurr);
	free(lineNext);
	return 0;
}
