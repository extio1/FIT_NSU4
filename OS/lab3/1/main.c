#include <stdio.h>
#include "dirrev.h"

int main(int argc, char** argv){
	if(argc != 2){
		printf("Error: Uncorrect num of args.\n");
		return -1;
	}

	mkdirrev(argv[1]);

	return 0;
}
