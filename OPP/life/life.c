#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <C:\Program Files (x86)\Microsoft SDKs\MPI\Include\mpi.h>

#define ASSERT_SECCEED(com) if(com != MPI_SUCCESS) { perror("com error."); exit(-1); }
#define N_DIMS 1
#define ASCII_TO_INT(ascii_code) ascii_code - 48

typedef struct ScattervParam {
	int* size;
	int* offset;
} ScattervParam;

typedef struct Matrix {
	int size;
	double* data;
} Matrix;

void define_scatterv_matrixs(int* sizes, int* offsets, const int size, const int nproc) {
	double piece = (double)size / nproc;

	double sum = piece;
	sizes[0] = ceil(piece);
	double sum_of_previouses = sizes[0];
	offsets[0] = 0;

	for (int i = 1; i < nproc; ++i) {
		sum += piece;
		sizes[i] = ceil(sum) - sum_of_previouses;
		offsets[i] = offsets[i - 1] + sizes[i - 1];
		sum_of_previouses += sizes[i];
	}
}

// За-mmap-ить?
void read_field(char* path, char* field, const size_t size_fld) {
	FILE* in;
	fopen_s(&in, path, "r");
	char* buff = (char*)malloc(sizeof(char) * size_fld * 2);
	
	for (int i = 0; i < size_fld; ++i) {
		fgets(buff, size_fld*2+2, in);
		//printf("%s|||||||||||||\n", buff);
		for (int j = 0; j < size_fld; ++j) {
			field[i * size_fld + j] = ASCII_TO_INT(buff[j * 2]);
		}
	}
	fclose(in);
}

void print_mat(const char* mat, const int size) {
	for (int i = 0; i < size; ++i) {
		for (int j = 0; j < size; ++j) {
			printf("%d ", mat[i * size + j]);
		}
		printf("\n");
	}
}

void print_line(const int* line, const int size) {
	for (int j = 0; j < size; ++j) {
		printf("%d ", line[j]);
	}
	printf("\n");
}

void create_topology(MPI_Comm* comm, const int size) {
	int dims[N_DIMS] = { size };
	int periodic[N_DIMS] = { 1 };
	ASSERT_SECCEED(MPI_Cart_create(MPI_COMM_WORLD, 1, dims, periodic, 1, comm));
}

int main(int argc, char** argv) {

	ASSERT_SECCEED(MPI_Init(&argc, &argv));

	MPI_Comm cartComm;
	int size, rank;
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	create_topology(&cartComm, size);
	MPI_Comm_rank(cartComm, &rank);

	const int n_iteration = atoi(argv[4]);
	size_t size_fld = atol(argv[1]);

	char* field;
	if (rank == 0) {
		field = (char*)malloc(sizeof(char) * size_fld * size_fld);
		read_field(argv[2], field, size_fld);
		//print_mat(field, size_fld);
	}

	int* sizes   = (int*)(malloc(sizeof(int) * size));
	int* offsets = (int*)(malloc(sizeof(int) * size));

	define_scatterv_matrixs(sizes, offsets, size_fld, size);
	print_line(sizes, size);
	print_line(offsets, size);






	ASSERT_SECCEED(MPI_Finalize());
}
