#include <stdlib.h>
#include <stdio.h>
#include <time.h>

int main(){
	FILE* coefMatrixF;
	FILE* xLineF;

	if((coefMatrixF = fopen("coefMatrix.txt", "w")) == NULL || (xLineF = fopen("xLineF.txt", "w")) == NULL){
		printf("Error while creating the empty output files.\n");
		exit(1);
	}

	int dimension;
	scanf("%d", &dimension);

	srand(time(NULL));
	for(int i = 0; i < dimension; ++i){
		for(int j = 0; j < dimension; ++j){
			if(i != j){
				fprintf(coefMatrixF, "%f ", (double)rand()/rand());
			}
			else{
				fprintf(coefMatrixF, "%f ", (double)rand()/rand()+dimension);
			}
		}
		fputs("\n", coefMatrixF);
		fprintf(xLineF, "%f ", (double)rand()/rand());
	}


}
