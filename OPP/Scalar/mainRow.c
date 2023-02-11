#include "stdio.h"
#include "time.h"

#define LENGTH 1000

void initArr(double* arr, const int size){
	for(int i = 0; i < size; i++)
		arr[i] = i;
}


int main(){
	double arr1[LENGTH];
	double arr2[LENGTH];
	initArr(arr1, LENGTH);
	initArr(arr2, LENGTH);

	struct timespec start, end;

	if( clock_gettime(CLOCK_MONOTONIC_RAW, &start) != 0 ) printf("Error of getting time.\n");
	double answer = 0;
	for(int i = 0; i < LENGTH; ++i){
		for(int j = 0; j < LENGTH; ++j){
			answer += arr1[i] * arr2[j];
		}
	}
	if( clock_gettime(CLOCK_MONOTONIC_RAW, &end) != 0 ) printf("Error of getting time.\n");

	printf("Answer is %f\n", answer);
	printf("Time spent: %f\n", end.tv_sec - start.tv_sec + 0.000000001 * (end.tv_nsec - start.tv_nsec));

	return 0;
}

