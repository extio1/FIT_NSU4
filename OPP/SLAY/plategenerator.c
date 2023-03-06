#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <iostream>

int main(){
	FILE* coefMatrixF;
	FILE* xLineF;

	if((coefMatrixF = fopen("coefMatrix.txt", "w")) == NULL || (xLineF = fopen("lineAnswer.txt", "w")) == NULL){
		printf("Error while creating the empty output files.\n");
		exit(1);
	}

	int nx, ny;
	printf("Enter sizes of plate, x and y:\n");
	scanf("%d %d", &nx, &ny);

	srand(time(NULL));
	// i - строка
	// j - столбец
	for(int i = 0; i < nx*ny; ++i){
		for(int j = 0; j < nx*ny; ++j){
			if(i == j){
				fprintf(coefMatrixF, "%f ", -4.0);
			}else if ( ((i-j==1) || (j-i==1)) && !((i%nx==nx-1&&j%nx==0)||(j%nx==nx-1&&i%nx==0)) ) {
				fprintf(coefMatrixF, "%f ", 1.0);
			}else if((i-j == nx) || (j-i == nx)){
				fprintf(coefMatrixF, "%f ", 1.0);
			}
			else{
				fprintf(coefMatrixF, "%f ", 0.0);
			}
		}
		fputs("\n", coefMatrixF);
		if(rand()%20 == 0){
			printf("NOT ZERO\n");
			fprintf(xLineF, "%f ", (double)(rand())/RAND_MAX*100 - 50 );
		}
		else{
			fprintf(xLineF, "%f ", 0.0);
		}
		
	}
	printf("==========================>DONE!\n");

}
