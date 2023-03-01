#include <stdio.h>
#include <stdlib.h>

#define EPSILON 0.00005
#define TAY 0.01

void prod_mat_line(double* mat, double* line, int dim) {
	double* staticLine = (double*)malloc(sizeof(double) * dim);
	for (int i = 0; i < dim; ++i) {
		staticLine[i] = line[i];
	}

	for (int i = 0; i < dim; ++i) {
		for (int j = 0; j < dim; ++j) {
			line[j] += staticLine[j] * mat[i * dim + j];
		}
	}
	free(staticLine);
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
	return 0;
}
