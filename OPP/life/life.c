#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>

#define FIRST_STRING_TAG 100
#define LAST_STRING_TAG 101

#define N_DIMS 1
#define ASSERT_SUCCEED(com) if(com != MPI_SUCCESS) { perror("com error."); exit(-1); }
#define ASCII_TO_INT(ascii_code) ascii_code - 48

typedef struct ScattervParam {
	int* size;
	int* offset;
} ScattervParam;

typedef struct LocalData {
	int size_x;
	int size_y;
	char* data;
	char** previous_data;
	char* stopper;
} LocaData;

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
	FILE* in = fopen(path, "r");
	char* buff = (char*)malloc(sizeof(char) * size_fld * 3);
	for (int i = 0; i < size_fld; ++i) {
		fgets(buff, size_fld*2+2, in);
		//printf("%s|||||||||||||\n", buff);
		for (int j = 0; j < size_fld; ++j) {
			field[i * size_fld + j] = ASCII_TO_INT(buff[j * 2]);
		}
	}

	free(buff);
	fclose(in);
}

void print_mat(const char* mat, const int sizeX, const int sizeY) {
	for (int i = 0; i < sizeY; ++i) {
		for (int j = 0; j < sizeX; ++j) {
			printf("%d ", mat[i * sizeX + j]);
		}
		printf("\n");
	}
	printf("\n");
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
	ASSERT_SUCCEED(MPI_Cart_create(MPI_COMM_WORLD, 1, dims, periodic, 1, comm));
}

void init_matString_type(MPI_Datatype* type, const int line_length){
	ASSERT_SUCCEED(MPI_Type_contiguous(line_length, MPI_CHAR, type));
	ASSERT_SUCCEED(MPI_Type_commit(type));
}

//кандидат на векторизацию
void calc_stop_flag(LocaData* ld, const size_t n_flags){
	int size_x = ld->size_x;
	int size_y = ld->size_y;
	for(int i = 0; i < n_flags; ++i){
		ld->stopper[i] = 1;
		for(int j = 0; j < size_y, ++j){
			for(int k = 0; k < size_x; ++k){
				if(ld->data[j * size_x + k] != ld->previous_data[j * size_x + k]){
					ld->stopper[i] = 0;
					break;
				}
			}
		}
	}
}

/*
	Чтение параллельно?
	Каждый процесс читает свою часть?
	Запись параллельно?
	MPI_File_iread_all
*/

int main(int argc, char** argv) {

	ASSERT_SUCCEED(MPI_Init(&argc, &argv));

	MPI_Comm cartComm;
	int size, rank;
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	create_topology(&cartComm, size);
	MPI_Comm_rank(cartComm, &rank);


	const int n_iteration = atoi(argv[4]);
	size_t size_fld = atol(argv[1]);

	char* field_full;
	if (rank == 0) {
		field_full = (char*)malloc(sizeof(char) * size_fld * size_fld);
		read_field(argv[2], field_full, size_fld);
		//print_mat(field_full, size_fld);
	}

	int* sizes   = (int*)(malloc(sizeof(int) * size));
	int* offsets = (int*)(malloc(sizeof(int) * size));
	define_scatterv_matrixs(sizes, offsets, size_fld, size);
	//print_line(sizes, size);
	//print_line(offsets, size);

	MPI_Datatype MatString;
	init_matString_type(&MatString, size_fld);

	int previous_rank, next_rank; // previous = upper; next = lower 
	ASSERT_SUCCEED(MPI_Cart_shift(cartComm, 0, 1, &previous_rank, &next_rank));

	printf("my %d; upper %d, lower %d\n", rank, previous_rank, next_rank);
	
	LocalData local_field;
	local_field.size_x = size_fld;
	local_field.size_y = sizes[rank]+2;
	int n_elements_in_distributed = local_field.size_x*local_field.size_y;
	int n_elements_in_localdata = n_elements_in_distributed+2*local_field.size_x;
	MPI_Barrier(MPI_COMM_WORLD);
	printf("%d - %d %d\n",rank, local_field.size_x, local_field.size_y);
	local_field.data[0] = (char*) malloc(sizeof(char)*n_elements_in_localdata);
	local_field.previous_data = (char**) malloc(sizeof(char*) * n_iteration);
	local_field.stopper = (char*) malloc(sizeof(char) * n_iteration);

	ASSERT_SUCCEED(
		MPI_Scatterv(field_full, 
					sizes, offsets, MatString, 
					local_field.data[0]+local_field.size_x, 
					n_elements_in_distributed, 
					MPI_CHAR, 0, cartComm)
	);

	/*
	if(rank == 5){
		print_mat(local_field.data[0], local_field.size_x, local_field.size_y);
	}
	*/

	size_t first_str_pos = 0;
	size_t last_str_pos  = n_elements_in_localdata-local_field.size_x-1;
	MPI_Request first_send, last_send;
	MPI_Request first_recv, last_recv;
	for(int iteration = 0; i < n_iteration; ++iteration){
		ASSERT_SUCCEED(
			MPI_Isent(local_field.data[i%2][first_str_pos], 1, MatString, 
					  previous_rank, FIRST_STRING_TAG, cartComm, &first_send);
		);
		ASSERT_SUCCEED(
			MPI_Isent(local_field.data[i%2][last_str_pos], 1, MatString, 
					  next_rank, LAST_STRING_TAG, cartComm, &last_send);
		);

		ASSERT_SUCCEED(
			MPI_Irecv(local_field.data[i%2][first_str_pos], 1, MatString, 
					  previous_rank, FIRST_STRING_TAG, cartComm, &first_recv);
		);
		ASSERT_SUCCEED(
			MPI_Irecv(local_field.data[i%2][last_str_pos], 1, MatString, 
					  next_rank, LAST_STRING_TAG, cartComm, &last_recv);
		);

		calc_stop_flag(local_data, iteration);
	}


	

	if(rank == 0){
		free(field_full);
	}

	free(local_field.data);
	for(int i = 0; i < n_iteration; ++i){
		free(local_field.previous_data[i]);
	}
	free(local_field.previous_data);

	free(sizes);
	free(offsets);
	MPI_Type_free(&MatString);

	ASSERT_SUCCEED(MPI_Finalize());

}
