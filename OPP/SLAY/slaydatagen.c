#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <iostream>
#include <random>

int main(){
	FILE* coefMatrixF;
	FILE* xLineF;

	if((coefMatrixF = fopen("coefMatrix.txt", "w")) == NULL || (xLineF = fopen("lineAnswer.txt", "w")) == NULL){
		printf("Error while creating the empty output files.\n");
		exit(1);
	}

	int dimension;
	printf("Enter the dimension to generate:\n");
	scanf("%d", &dimension);

	srand(time(NULL));
	double* matrix = (double*) malloc(sizeof(double) * (dimension*dimension));
	for(int i = 0; i < dimension; ++i){
		for(int j = i; j < dimension; ++j){
			double randomValue = (double)(rand())/RAND_MAX*2 - 1;
			if(i==j){
				matrix[i * dimension + j] = randomValue + dimension + dimension;
			} else {
				matrix[i * dimension + j] = randomValue;
				matrix[j * dimension + i] = randomValue;
			}
		}
	}

	for(int i = 0; i < dimension; ++i){
		for(int j = 0; j < dimension; ++j){
			fprintf(coefMatrixF, "%f ", matrix[i * dimension + j]);
		}
		fputs("\n", coefMatrixF);
		fprintf(xLineF, "%f ", 1);
	}

	free(matrix);
	printf("==========================>DONE!\n");
	return 0;
}
