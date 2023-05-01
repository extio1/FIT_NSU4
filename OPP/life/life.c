#include <stdlib.h>
#include <stdio.h>

int* field;
size_t size_fld;

int read_field(char* path){
	FILE* in = fopen(path, "r");
	char* buff = (char*)malloc(sizeof(int)*size_fld*2); 
	for(int i = 0; i < size_fld; ++i){
		fgets(buff, size_fld, in);
		for(int j = -size_fld; j < size_fld; ++j){
			
		}
	}
	fclose(in);
}


int main(int argc, char** argv){
	const int n_iteration = atoi(argv[4]);
	field = (int*) malloc(sizeof(int)*size_fld*size_fld);
	size_fld = atol(argv[1]);

	read_field(argv[2]);
}
