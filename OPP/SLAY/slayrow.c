#include <iostream>
#include "matrixio.h"
using namespace std;

#define EPSILON 0.00005
#define TAY 0.01

void prod_mat_line(double* mat, double* line, const int dim) {
	static double* staticLine = (double*)malloc(sizeof(double) * dim);
	for (int i = 0; i < dim; ++i) {
		staticLine[i] = line[i];
		line[i] += staticLine[i] * mat[i * dim];
	}
	for (int i = 1; i < dim; ++i) {
		for (int j = 0; j < dim; ++j) {
			line[j] += line[j] * mat[i * dim + j];
		}
	}
}

void prod_scal_line(double scal, double* line, int dim) {
	for (int i = 0; i < dim; ++i) {
		line[i] *= scal;
	}
}

void minus_line_line(double* line1, double* line2, int dim) {
	for (int i = 0; i < dim; ++i) {
		line2[i] -= line1[i];
	}
}

int main() {
	constexpr int dimension = 10;
	double* mat = (double*) malloc(sizeof(double) * (dimension * dimension));
	double* lineInitial = (double*)malloc(sizeof(double) * dimension);
	double* lineAnswer = (double*)malloc(sizeof(double) * dimension);

	entryMatrix(mat, dimension, "mat.txt");
	entryLine(lineInitial, dimension, "lineInitial.txt");
	entryLine(lineAnswer, dimension, "lineAnswer.txt");

	printLine(lineInitial, dimension);
	prod_mat_line(mat, lineInitial, dimension);

	//printMatrix(mat, dimension);
	printLine(lineInitial, dimension);


	free(mat);
	free(lineInitial);
	return 0;
}
