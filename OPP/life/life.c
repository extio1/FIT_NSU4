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

void create_topology(MPI_Comm* comm, const int size) {
	int dims[N_DIMS] = { size };
	int periodic[N_DIMS] = { 1 };
	ASSERT_SUCCEED(MPI_Cart_create(MPI_COMM_WORLD, 1, dims, periodic, 1, comm));
}

void init_matString_type(MPI_Datatype* type, const int line_length) {
	ASSERT_SUCCEED(MPI_Type_contiguous(line_length, MPI_CHAR, type));
	ASSERT_SUCCEED(MPI_Type_commit(type));
}

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

void kill_or_revive_cell(const char* cell, const int n_alive_around, char* new_cell){
	if (*cell == 1) {
		if (n_alive_around < 2 || n_alive_around > 3) {
			*new_cell = 0;
		} else {
			*new_cell = 1;
		}
	} else {
		if (n_alive_around == 3) {
			*new_cell = 1;
		} else {
			*new_cell = 0;
		}
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

	return n_alive;
}

void calc_new_state_of_field_except_for_last_first(LocalData* ld, char* new_field) {
	int size_x = ld->size_x;
	int size_y = ld->size_y;
	for (int i = 2; i < size_y-2; i++) {
		for (int j = 0; j < size_x; ++j) {
			int n_alive = calc_cell(ld->data, j, i, size_x, size_y);
			int curr_pos_arr_linear = i * size_x + j;
			kill_or_revive_cell(&ld->data[curr_pos_arr_linear], n_alive, &new_field[curr_pos_arr_linear]);
		}
	}
}

void calc_string_condition(LocalData* ld, char* new_field, const size_t posY) {
	size_t size_x = ld->size_x, size_y = ld->size_y;
	for (int j = 0; j < size_x; ++j) {
		int n_alive = calc_cell(ld->data, j, posY, size_x, size_y);
		int curr_pos_arr_linear = posY * size_x + j;
		kill_or_revive_cell(&ld->data[curr_pos_arr_linear], n_alive, &new_field[curr_pos_arr_linear]);
	}
}

int how_many_was_one_in_stop_vect(const char* stopper, const size_t block_size) {
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
	return how_many_was_one;
}

void init_file_string_type(MPI_Datatype* type, const int line_length){
	MPI_Type_vector(line_length, line_length, line_length+1, MPI_CHAR, type);
	MPI_Type_commit(type);
}

void ascii_to_integer(char* arr, const size_t arr_size){
	for(int i = 0; i < arr_size; ++i){
		arr[i] -= 48;
	}
}

void integer_to_ascii(char* arr, const size_t arr_size){
	for(int i = 0; i < arr_size; ++i){
		arr[i] += 48;
	}
}


int main(int argc, char** argv) {
	int rank;
	const int n_iteration = atoi(argv[4]);
	const size_t size_fld = atol(argv[1]);

	ASSERT_SUCCEED(MPI_Init(&argc, &argv));

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	create_topology(&cartComm, size);
	MPI_Comm_rank(cartComm, &rank);
	
	int* sizes = (int*)(malloc(sizeof(int) * size));
	int* offsets = (int*)(malloc(sizeof(int) * size));
	define_scatterv_matrixs(sizes, offsets, size_fld);

	MPI_Datatype MatString;
	init_matString_type(&MatString, size_fld);

	MPI_File infile, outfile;
	MPI_Datatype fileType;
	init_file_string_type(&fileType, size_fld);

	// (size_fld+1) - to take into account line breaks
	ASSERT_SUCCEED(MPI_File_open(cartComm, argv[2], MPI_MODE_RDONLY, MPI_INFO_NULL, &infile));
	ASSERT_SUCCEED(MPI_File_set_view(infile, offsets[rank]*(size_fld+1), MPI_CHAR, fileType, "native", MPI_INFO_NULL));

	ASSERT_SUCCEED(MPI_File_open(cartComm, argv[3], MPI_MODE_WRONLY | MPI_MODE_CREATE, MPI_INFO_NULL, &outfile));
	ASSERT_SUCCEED(MPI_File_set_view(outfile, offsets[rank]*(size_fld+1), MPI_CHAR, MPI_CHAR, "native", MPI_INFO_NULL));

	int previous_rank, next_rank; // previous = upper; next = lower 
	ASSERT_SUCCEED(MPI_Cart_shift(cartComm, 0, 1, &previous_rank, &next_rank));

	LocalData local_data;
	local_data.size_x = size_fld;
	local_data.size_y = sizes[rank] + 2;
	int n_elements_in_localdata = local_data.size_x * local_data.size_y;
	int n_elements_in_localdata_without_extra_str = size_fld * sizes[rank];
	local_data.data = (char*)malloc(sizeof(char) * n_elements_in_localdata);
	local_data.previous_data = (char**)malloc(sizeof(char*) * n_iteration);
	local_data.stopper = (char*)calloc((n_iteration+1), sizeof(char));
	char* local_field_begin = local_data.data+local_data.size_x;
	char* stopper_distributed_alltoall = (char*)calloc((n_iteration+1), sizeof(char));

	ASSERT_SUCCEED(MPI_File_read(infile, local_field_begin,
								n_elements_in_localdata_without_extra_str, MPI_CHAR, MPI_STATUS_IGNORE));
	ascii_to_integer(local_field_begin, n_elements_in_localdata_without_extra_str);

	int iteration_passed = 0;
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

		//Был получен флаг останова длины iteration_passed в local_data.stopper

		int block_stopper_size;
		if(((double)iteration_passed/size) != (block_stopper_size = iteration_passed/size)){
			++block_stopper_size;
		}

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

		if (how_many_was_one_in_stop_vect(stopper_distributed_alltoall, block_stopper_size) >= 1) {
			--iteration_passed;
			break;
		} else {
			local_data.previous_data[iteration_passed] = local_data.data;
			local_data.data = new_field;
		}
	}

	local_field_begin = local_data.data+local_data.size_x;
	integer_to_ascii(local_field_begin, n_elements_in_localdata_without_extra_str);

	char caret = '\n';
	for(int i = 1; i <= sizes[rank]; ++i){
		ASSERT_SUCCEED(MPI_File_write(outfile, local_data.data+local_data.size_x*i, 
									local_data.size_x, MPI_CHAR, MPI_STATUS_IGNORE));
		ASSERT_SUCCEED(MPI_File_write(outfile, &caret, 1, MPI_CHAR, MPI_STATUS_IGNORE));
	}

	free(local_data.data);
	free(local_data.stopper);

	for (int i = 0; i < iteration_passed; ++i) {
		free(local_data.previous_data[i]);
	}

	free(local_data.previous_data);

	free(sizes);
	free(offsets);
	free(stopper_distributed_alltoall);

	MPI_Type_free(&fileType);

	ASSERT_SUCCEED(MPI_Type_free(&MatString));


	ASSERT_SUCCEED(MPI_File_close(&infile));
	ASSERT_SUCCEED(MPI_File_close(&outfile));

	ASSERT_SUCCEED(MPI_Finalize());

}
