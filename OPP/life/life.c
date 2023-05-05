#include <math.h>
#include <C:\Program Files (x86)\Microsoft SDKs\MPI\Include\mpi.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

#define DOWN_STRING_TAG 100
#define UP_STRING_TAG 101

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
		fgets(buff, size_fld * 2 + 2, in);
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

void init_matString_type(MPI_Datatype* type, const int line_length) {
	ASSERT_SUCCEED(MPI_Type_contiguous(line_length, MPI_CHAR, type));
	ASSERT_SUCCEED(MPI_Type_commit(type));
}

//кандидат на векторизацию
void calc_stop_flag(LocaData* ld, const size_t n_flags) {
	int size_x = ld->size_x;
	int size_y = ld->size_y;
	for (int i = 0; i < n_flags; ++i) {
		ld->stopper[i] = 1;
		for (int j = 0; j < size_y; ++j) {
			for (int k = 0; k < size_x; ++k) {
				if (ld->data[j * size_x + k] != ld->previous_data[i][j * size_x + k]) {
					ld->stopper[i] = 0;
					break;
				}
			}
		}
	}
}

void init_by_num(char* arr, const char num, const size_t size) {
	for (int i = 0; i < size; ++i) {
		arr[i] = num;
	}
}

int calc_cell(const char* field, const size_t pos_x, const size_t pos_y,
	const size_t size_x, const size_t size_y) {
	int n_alive = 0;
	size_t prevx = (pos_x - 1) % size_x;
	size_t nextx = (pos_x + 1) % size_x;
	size_t prevy = (pos_y - 1) % size_y;
	size_t nexty = (pos_y + 1) % size_y;

	n_alive += field[pos_y * size_x + prevx];
	n_alive += field[pos_y * size_x + nextx];

	n_alive += field[prevy * size_x + prevx];
	n_alive += field[prevy * size_x + pos_x];
	n_alive += field[prevy * size_x + nexty];
	n_alive += field[nexty * size_x + prevx];
	n_alive += field[nexty * size_x + pos_x];
	n_alive += field[nexty * size_x + nexty];

	return n_alive;
}

void calc_new_state_of_field_except_for_last_first(LocalData* ld, char* new_field) {
	int size_x = ld->size_x;
	int size_y = ld->size_y;
	for (int i = 2; i < size_y - 2; i++) {
		for (int j = 0; j < size_x; ++j) {
			int n_alive = calc_cell(ld->data, j, i, size_x, size_y);

			if (ld->data[i * size_x + j] == 1) {
				if (n_alive < 2 || n_alive > 3) {
					ld->data[i * size_x + j] = 0;
				}
			}
			else {
				if (n_alive == 3) {
					ld->data[i * size_x + j] = 1;
				}
			}
		}
	}
}

void write_matrix(const char* arr, const size_t size, const char* path) {
	FILE* out = fopen(path, "w");
	printf("===============%d\n", size);
	for (int i = 0; i < size; ++i) {
		for (int j = 0; j < size; ++j) {
			printf("%d ", arr[i * size + j]);
			fprintf(out, "%d ", arr[i * size + j]);
		}
		fputc('\n', out);
	}

	fclose(out);
}


void calc_string_condition(LocalData* ld, char* new_field, const size_t posY) {
	size_t size_x = ld->size_x, size_y = ld->size_y;
	for (int j = 0; j < size_x; ++j) {
		int n_alive = calc_cell(ld->data, j, posY, size_x, size_y);

		if (ld->data[posY * size_x + j] == 1) {
			if (n_alive < 2 || n_alive > 3) {
				ld->data[posY * size_x + j] = 0;
			}
		}
		else {
			if (n_alive == 3) {
				ld->data[posY * size_x + j] = 1;
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

int compare_stop_vect() {
	return 0;
}

int main(int argc, char** argv) {

	ASSERT_SUCCEED(MPI_Init(&argc, &argv));

	MPI_Comm cartComm;
	int size, rank;

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	create_topology(&cartComm, size);
	MPI_Comm_rank(cartComm, &rank);
	
	const int n_iteration = atoi(argv[4]);
	size_t size_fld = atol(argv[1]);

	char* field_full = NULL;
	if (rank == 0) {
		field_full = (char*)malloc(sizeof(char) * size_fld * size_fld);
		read_field(argv[2], field_full, size_fld);
		//print_mat(field_full, size_fld, size_fld);
	}
	
	int* sizes = (int*)(malloc(sizeof(int) * size));
	int* offsets = (int*)(malloc(sizeof(int) * size));
	define_scatterv_matrixs(sizes, offsets, size_fld, size);
	//print_line(sizes, size);
	//print_line(offsets, size);
	
	MPI_Datatype MatString;
	init_matString_type(&MatString, size_fld);

	int previous_rank, next_rank; // previous = upper; next = lower 
	ASSERT_SUCCEED(MPI_Cart_shift(cartComm, 0, 1, &previous_rank, &next_rank));

	//printf("my %d; upper %d, lower %d\n", rank, previous_rank, next_rank);
	
	LocalData local_data;
	local_data.size_x = size_fld;
	local_data.size_y = sizes[rank] + 2;
	int n_elements_in_distributed = local_data.size_x * local_data.size_y;
	int n_elements_in_localdata = n_elements_in_distributed + 2 * local_data.size_x;
	printf("%d - %d %d\n", rank, local_data.size_x, local_data.size_y);
	local_data.data = (char*)malloc(sizeof(char) * n_elements_in_localdata);
	//init_by_num(local_data.data, rank, n_elements_in_distributed);
	local_data.previous_data = (char**)malloc(sizeof(char*) * n_iteration);
	local_data.stopper = (char*)malloc(sizeof(char) * n_iteration);
	
	ASSERT_SUCCEED(
		MPI_Scatterv(field_full,
			sizes, offsets, MatString,
			local_data.data + local_data.size_x,
			n_elements_in_distributed,
			MPI_CHAR, 0, cartComm)
	);

	
	if(rank == 1){
		print_mat(local_data.data, local_data.size_x, local_data.size_y);
	}

	
	unsigned int iteration_passed = 0;
	char* first_str_ptr = local_data.data;
	char* second_str_ptr = first_str_ptr + local_data.size_x;
	char* last_str_ptr = local_data.data + n_elements_in_localdata;
	char* prelast_str_ptr = last_str_ptr - local_data.size_x;
	MPI_Request second_send, prelast_send;
	MPI_Request first_recv, last_recv;
	MPI_Request stop_flag_alltoall;

	print_mat(local_data.data, local_data.size_x, local_data.size_y);
	
	for (iteration_passed = 0; iteration_passed < n_iteration; ++iteration_passed) {
		printf("0\n");
		ASSERT_SUCCEED(
			MPI_Isend(second_str_ptr, 1, MatString,
				previous_rank, UP_STRING_TAG, cartComm, &second_send)
		);
		ASSERT_SUCCEED(
			MPI_Isend(prelast_str_ptr, 1, MatString,
				next_rank, DOWN_STRING_TAG, cartComm, &prelast_send)
		);

		ASSERT_SUCCEED(
			MPI_Irecv(first_str_ptr, 1, MatString,
				previous_rank, DOWN_STRING_TAG, cartComm, &first_recv)
		);
		ASSERT_SUCCEED(
			MPI_Irecv(last_str_ptr, 1, MatString,
				next_rank, UP_STRING_TAG, cartComm, &last_recv)
		);
		
		printf("1\n");
		//calc_stop_flag(&local_data, iteration_passed);
		//Был получен флаг останова длины iteration_passed в local_data.stopper
		
		/*
		ASSERT_SUCCEED(
			MPI_Ialltoall(last_str_ptr, 1, MatString,
				next_rank, LAST_STRING_TAG, cartComm, &stop_flag_alltoall)
		);
		*/

		//char* new_field = (char*)malloc(sizeof(char) * n_elements_in_localdata);
		//calc_new_state_of_field_except_for_last_first(&local_data, new_field);
		printf("2\n");
		
		ASSERT_SUCCEED(
			MPI_Wait(&second_send, MPI_STATUS_IGNORE)
		);

		ASSERT_SUCCEED(
			MPI_Wait(&first_recv, MPI_STATUS_IGNORE)
		);
		printf("3\n");
		//calc_string_condition(&local_data, new_field, 1);

		ASSERT_SUCCEED(
			MPI_Wait(&prelast_send, MPI_STATUS_IGNORE)
		);

		ASSERT_SUCCEED(
			MPI_Wait(&last_recv, MPI_STATUS_IGNORE)
		);
		printf("4\n");
		//calc_string_condition(&local_data, new_field, local_data.size_y - 1);
		
		/*
		ASSERT_SUCCEED(
			MPI_Wait(&stop_flag_alltoall, MPI_STATUS_IGNORE)
		);
		*/
		printf("5\n");
		if (compare_stop_vect()) {
			break;
		} else {
			//local_data.previous_data[iteration_passed] = local_data.data;
			//local_data.data = new_field;
		}
		printf("6\n");
	}

	ASSERT_SUCCEED(
		MPI_Gatherv(local_data.data, sizes[rank], MatString,
			field_full, sizes, offsets, MatString, 0, cartComm)
	);

	if (rank == 0)
		write_matrix(field_full, size_fld, argv[3]);

	if (rank == 0)
		free(field_full);

	free(local_data.data);

	for (int i = 0; i < iteration_passed; ++i) {
		free(local_data.previous_data[i]);
	}
	free(local_data.previous_data);

	free(sizes);
	free(offsets);
	MPI_Type_free(&MatString);

	ASSERT_SUCCEED(MPI_Finalize());

}
