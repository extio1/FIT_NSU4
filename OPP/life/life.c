#include <math.h>
#include <mpi.h>
#include <stdlib.h>
#include <stdio.h>

#define DOWN_STRING_TAG 100
#define UP_STRING_TAG 101

#define N_DIMS 1
#define ASSERT_SUCCEED(com) if(com != MPI_SUCCESS) { perror("com error."); exit(-1); }
#define ASCII_TO_INT(ascii_code) ascii_code - 48

typedef struct LocalData {
	int size_x;
	int size_y;
	char* data;
	char** previous_data;
	char* stopper;
} LocaData;

int size; 		   // num of procceses
MPI_Comm cartComm; // cartesian communicator

void print_line(const char* line, const int size) {
	for (int j = 0; j < size; ++j) {
		printf("%d ", line[j]);
	}
	printf("\n");
}

void define_scatterv_matrixs(int* sizes, int* offsets, const int size_mat) {
	int extraCells = size_mat % size;
	const int standartSize = size_mat / size;
	offsets[0] = 0;
	for (int i = 0; i < size; ++i) {
		if (extraCells == 0) {
			sizes[i] = standartSize;
		}
		else {
			sizes[i] = standartSize + 1;
			--extraCells;
		}
		if(i > 0)
			offsets[i] = offsets[i - 1] + sizes[i - 1];
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


void write_matrix(const char* arr, const size_t size, const char* path) {
	FILE* out = fopen(path, "w");
	printf("===============%d\n", size);
	for (int i = 0; i < size; ++i) {
		for (int j = 0; j < size; ++j) {
			printf("%d ", arr[i * size + j]);
			fprintf(out, "%d ", arr[i * size + j]);
		}
		printf("\n");
		fputc('\n', out);
	}

	fclose(out);
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
		for (int j = 1; j < size_y-1; ++j) {
			for (int k = 0; k < size_x; ++k) {
				size_t cur_pos = j * size_x + k ;
				if (ld->data[cur_pos] != ld->previous_data[i][cur_pos]) {
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
	size_t prevx = (size_x + pos_x - 1) % size_x;
	size_t nextx = (size_x + pos_x + 1) % size_x;
	size_t prevy = (size_y + pos_y - 1) % size_y;
	size_t nexty = (size_y + pos_y + 1) % size_y;

	n_alive += field[pos_y * size_x + prevx];
	n_alive += field[pos_y * size_x + nextx];

	n_alive += field[prevy * size_x + prevx];
	n_alive += field[prevy * size_x + pos_x];
	n_alive += field[prevy * size_x + nextx];
	n_alive += field[nexty * size_x + prevx];
	n_alive += field[nexty * size_x + pos_x];
	n_alive += field[nexty * size_x + nextx];

/*	printf("%d %d %d %d %d %d %d %d\n", field[pos_y * size_x + prevx], field[pos_y * size_x + nextx],
		field[prevy * size_x + prevx], field[prevy * size_x + pos_x], field[prevy * size_x + nextx],
		field[nexty * size_x + prevx], field[nexty * size_x + pos_x], field[nexty * size_x + nextx]);*/
/*	printf("%ld %ld %ld %ld curr(%ld, %ld) = %d:  ", prevx, nextx, prevy, nexty, pos_x, pos_y, n_alive);
	printf("\n%d - %d \n UP: %d %d %d \n DOWN: %d %d %d\n", 
		field[pos_y * size_x + prevx], field[pos_y * size_x + nextx],
		field[prevy * size_x + prevx], field[prevy * size_x + pos_x], field[prevy * size_x + nextx],
		field[nexty * size_x + prevx], field[nexty * size_x + pos_x], field[nexty * size_x + nextx]);*/
	return n_alive;
}

void calc_new_state_of_field_except_for_last_first(LocalData* ld, char* new_field) {
	int size_x = ld->size_x;
	int size_y = ld->size_y;
	//printf("--------+%d \n\n", size_y);
	for (int i = 2; i < size_y-2; i++) {
			//print_mat(ld->data, size_x, size_y);
		for (int j = 0; j < size_x; ++j) {
			int n_alive = calc_cell(ld->data, j, i, size_x, size_y);
			int curr_pos_arr_linear = i * size_x + j;

			//printf("(%ld, %ld): init-%d% d\n", j, i, ld->data[curr_pos_arr_linear], n_alive);
			if (ld->data[curr_pos_arr_linear] == 1) {
				if (n_alive < 2 || n_alive > 3) {
					new_field[curr_pos_arr_linear] = 0;
				} else {
					new_field[curr_pos_arr_linear] = 1;
				}
			} else {
				if (n_alive == 3) {
					new_field[curr_pos_arr_linear] = 1;
				} else {
					new_field[curr_pos_arr_linear] = 0;
				}
			}
		}
	}
}

void calc_string_condition(LocalData* ld, char* new_field, const size_t posY) {
	size_t size_x = ld->size_x, size_y = ld->size_y;

	for (int j = 0; j < size_x; ++j) {
		int n_alive = calc_cell(ld->data, j, posY, size_x, size_y);
		int curr_pos_arr_linear = posY * size_x + j;

		if (ld->data[curr_pos_arr_linear] == 1) {
			if (n_alive < 2 || n_alive > 3) {
				new_field[curr_pos_arr_linear] = 0;
			} else {
				new_field[curr_pos_arr_linear] = 1;
			}
		}
		else {
			if (n_alive == 3) {
				new_field[curr_pos_arr_linear] = 1;
			} else {
				new_field[curr_pos_arr_linear] = 0;
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

int how_many_was_one_in_stop_vect(const char* stopper, const size_t block_size) {
	//printf(">>>>>>>>>%d\n", block_size);
	int was_one = 0;
	for(int i = 0; i < block_size; ++i){
		int num_of_ones = 0;
		for(int proc = 0; proc < size; ++proc){
			if(stopper[i+block_size*proc] == 1)
				++num_of_ones;
		}
		if(num_of_ones == size){
			was_one = 1;
			break;
		}
	}
	
	int how_many_was_one = 0;
	ASSERT_SUCCEED(
		MPI_Allreduce(&was_one, &how_many_was_one, 1, MPI_INT, MPI_SUM, cartComm)
	);
	//printf("!!!!>>>>>>>>>%d %d\n", how_many_was_one, size);
	return how_many_was_one;
}

int main(int argc, char** argv) {

	ASSERT_SUCCEED(MPI_Init(&argc, &argv));

	int rank;

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	create_topology(&cartComm, size);
	MPI_Comm_rank(cartComm, &rank);
	
	const int n_iteration = atoi(argv[4]);
	size_t size_fld = atol(argv[1]);

	char* field_full = NULL;
	if (rank == 0) {
		field_full = (char*)malloc(sizeof(char) * size_fld * size_fld);
		read_field(argv[2], field_full, size_fld);
	}
	
	int* sizes = (int*)(malloc(sizeof(int) * size));
	int* offsets = (int*)(malloc(sizeof(int) * size));
	define_scatterv_matrixs(sizes, offsets, size_fld);
	
	MPI_Datatype MatString;
	init_matString_type(&MatString, size_fld);

	int previous_rank, next_rank; // previous = upper; next = lower 
	ASSERT_SUCCEED(MPI_Cart_shift(cartComm, 0, 1, &previous_rank, &next_rank));
	
	LocalData local_data;
	local_data.size_x = size_fld;
	local_data.size_y = sizes[rank] + 2;
	int n_elements_in_localdata = local_data.size_x * local_data.size_y;
	local_data.data = (char*)malloc(sizeof(char) * n_elements_in_localdata);
	local_data.previous_data = (char**)malloc(sizeof(char*) * n_iteration);
	local_data.stopper = (char*)malloc(sizeof(char) * n_iteration);
	init_by_num(local_data.stopper, 120, n_iteration);
	char* stopper_distributed_alltoall = (char*)malloc(sizeof(char) * n_iteration);
	init_by_num(stopper_distributed_alltoall, 127, n_iteration);

	MPI_Barrier(MPI_COMM_WORLD);
	printf("%d SIZES: %d %d\n", rank, local_data.size_x, local_data.size_y);

	ASSERT_SUCCEED(
		MPI_Scatterv(field_full,
			sizes, offsets, MatString,
			local_data.data + local_data.size_x,
			sizes[rank]*size_fld,
			MPI_CHAR, 0, cartComm)
	);

	unsigned int iteration_passed = 0;
	MPI_Request second_send, prelast_send;
	MPI_Request first_recv, last_recv;
	MPI_Request stop_flag_alltoall;

	for (iteration_passed = 0; iteration_passed < n_iteration; ++iteration_passed) {
		char* first_str_ptr = local_data.data;
		char* second_str_ptr = first_str_ptr + local_data.size_x;
		char* last_str_ptr = first_str_ptr + n_elements_in_localdata - local_data.size_x;
		char* prelast_str_ptr = last_str_ptr - local_data.size_x;

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
		
		calc_stop_flag(&local_data, iteration_passed);
/*
		if(rank == 0)
			print_line(local_data.stopper, iteration_passed);
		MPI_Barrier(MPI_COMM_WORLD);
		if(rank == 1)
			print_line(local_data.stopper, iteration_passed);
		MPI_Barrier(MPI_COMM_WORLD);
*/
		//Был получен флаг останова длины iteration_passed в local_data.stopper

		int block_stopper_size = ceil((double)iteration_passed/size);
		ASSERT_SUCCEED(
			MPI_Ialltoall(local_data.stopper, block_stopper_size, MPI_CHAR,
						  stopper_distributed_alltoall, block_stopper_size, MPI_CHAR,
						  cartComm, &stop_flag_alltoall)
		);

		char* new_field = (char*)malloc(sizeof(char)*n_elements_in_localdata);
		calc_new_state_of_field_except_for_last_first(&local_data, new_field);

		ASSERT_SUCCEED(
			MPI_Wait(&second_send, MPI_STATUS_IGNORE)
		);
		ASSERT_SUCCEED(
			MPI_Wait(&first_recv, MPI_STATUS_IGNORE)
		);

		calc_string_condition(&local_data, new_field, 1);

		ASSERT_SUCCEED(
			MPI_Wait(&prelast_send, MPI_STATUS_IGNORE)
		);

		ASSERT_SUCCEED(
			MPI_Wait(&last_recv, MPI_STATUS_IGNORE)
		);

		calc_string_condition(&local_data, new_field, local_data.size_y - 2);

		ASSERT_SUCCEED(
			MPI_Wait(&stop_flag_alltoall, MPI_STATUS_IGNORE)
		);

		int a = how_many_was_one_in_stop_vect(stopper_distributed_alltoall, block_stopper_size);
		//printf("IIII: %d\n", iteration_passed);
		if (a >= 1) {

					if(rank == 0){
					printf("\nV---------%d-----%d-----\n", a, iteration_passed);
					printf("%d\n", block_stopper_size);
					print_line(local_data.stopper, iteration_passed+1);
					print_line(stopper_distributed_alltoall, iteration_passed+1);
				}
				MPI_Barrier(MPI_COMM_WORLD);
				if(rank == 1){
					printf("\n");
					print_line(local_data.stopper, iteration_passed+1);
					print_line(stopper_distributed_alltoall, iteration_passed+1);
					printf("^--------------------\n\n");
				}
				MPI_Barrier(MPI_COMM_WORLD);

			--iteration_passed;
			break;
		} else {
			local_data.previous_data[iteration_passed] = local_data.data;
			local_data.data = new_field;
		}
	}

	ASSERT_SUCCEED(
		MPI_Gatherv(local_data.data+local_data.size_x, sizes[rank], MatString,
			field_full, sizes, offsets, MatString, 0, cartComm)
	);

	if (rank == 0)
		write_matrix(field_full, size_fld, argv[3]);

	if (rank == 0)
		free(field_full);

	free(local_data.data);
	free(local_data.stopper);

	for (int i = 0; i < iteration_passed; ++i) {
		free(local_data.previous_data[i]);
	}

	free(local_data.previous_data);

	free(sizes);
	free(offsets);
	free(stopper_distributed_alltoall);

	ASSERT_SUCCEED(MPI_Type_free(&MatString));

	ASSERT_SUCCEED(MPI_Finalize());

}
