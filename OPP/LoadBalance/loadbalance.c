#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
#include <mpi.h>

#define LEN_LIST 5
#define N_TASK 100

#define SENDER_SENSITIVITY 1

#define L 10

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

int** listTaskList;

int globIterCounter;
pthread_mutex_t taskListMutex;
pthread_cond_t taskLeftLessThanSens; 

int rank, size;


void init_task_list(){
	listTaskList = (int**)malloc(sizeof(int*)*LEN_LIST);
	for(int i = 0; i < LEN_LIST; ++i){
		listTaskList[i] = (int*)malloc(sizeof(int)*N_TASK*size);

		for(int j = 0; j < N_TASK*size; ++j){
			listTaskList[i][j] = abs(N_TASK/2-j%N_TASK) * abs(rank-(i%size)) * L;
		}
	}
}

void print_task_list(){
	for(int i = 0; i < LEN_LIST; ++i){
		printf("\n%d list:\n", i);
		for(int j = 0; j < N_TASK*size; ++j){
			printf("%d ", listTaskList[i][j]);
		}
	}
}

void free_task_list(){
	for(int i = 0; i < LEN_LIST; ++i){
		free(listTaskList[i]);
	}
	free(listTaskList);
}

void get_rank_by_max_load(const int* load, int* rank, int* weight){
	int max = 0;
	for(int i = 0; i < size; ++i){
		if(load[i] > max){
			max = load[i];
			*rank = i;
		}
	}

	*weight = max / 2;
}

void* reciever_routine(void* d){

	MPI_Request req[size-1];
	int loadStat[size];
	int zeroLoad = 0;

	MPI_Request allgatherReq;
	MPI_Allgather_init(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD,	MPI_INFO_NULL, &allgatherReq);

	while(1){
		pthread_mutex_lock(&taskListMutex);
		
		if(counter_task != B){
				pthread_cond_wait(&taskLeftLessThanSens, &taskListMutex);
		}

		int reqcounter;
		for(int i = 0; i < size; ++i){
			if(i != rank){
				MPI_Isend(&rank, 1, MPI_INT, i, MPI_ANY_TAG, MPI_COMM_WORLD, req[reqcounter++]);
			}
		}

		MPI_Start(&allgatherReq);

		int recvFromRank;
		int new_weight;
		get_rank_by_max_load(&recvFromRank, &recvFromRank, &new_weight);

		MPI_Recv(taskListMutex[globIterCounter], new_weight, MPI_INT, 
			recvFromRank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		pthread_mutex_unlock(&taskListMutex);
		load_counter += new_weight;

		MPI_Waitall(size-1, req, MPI_STATUSES_IGNORE);
	}
}

void* sender_routine(void* b){
	int loadStat[size];

	int askingRank;
	MPI_Request allgatherReq, recvReq, sendReq;
	MPI_Allgather_init(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD,	MPI_INFO_NULL, &allgatherReq);
	MPI_Recv_init(&askingRank, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &recvReq);

	while(1){
		MPI_Start(&recvReq);

		pthread_mutex_lock(&taskListMutex);

		int busyRank, weight;
		get_rank_by_max_load(loadStat, &busyRank, &weight);
		if(busyRank == rank){
			MPI_ISend(listTaskList[globIterCounter]+counter_task, weight, 
				MPI_INT, askingRank, MPI_ANY_TAG, MPI_COMM_WORLD, &sendReq)
			counter_task += weight;
		}

		pthread_mutex_unlock(&taskListMutex);

		if(busyRank == rank)
			MPI_Wait(&sendReq, MPI_STATUS_IGNORE);
	}
}

pthread_attr_t attrReciever, attrSender;
void init_reciever_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrReciever));
	POSIX_SUCCEED(pthread_attr_setdetachstate(&attrReciever, PTHREAD_CREATE_DETACHED));
	POSIX_SUCCEED(pthread_create(tid, &attrReciever, reciever_routine, NULL));
}

void init_sender_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrSender));
	POSIX_SUCCEED(pthread_attr_setdetachstate(&attrSender, PTHREAD_CREATE_DETACHED));
	POSIX_SUCCEED(pthread_create(tid, &attrSender, sender_routine, NULL));
}

int A = rank*N_TASK;
int B = (rank+1)*N_TASK;

int load_counter = N_TASK;
int counter_task = A;
void get_new_task(const int currIt, size_t* weight){
	pthread_mutex_lock(&taskListMutex);
	*weight = listTaskList[currIt][counter_task++];
	--load_counter;

	if(counter_task == B)
		pthread_cond_signal(&taskLeftLessThanSens);

	pthread_mutex_unlock(&taskListMutex);
}

int refresh_counter(){
	counter_task = A;
}

int main(int argc, char** argv){
	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	pthread_t sender, reciever;
	init_task_list();
	init_sender_thread(sender);
	init_reciever_thread(reciever);
	POSIX_SUCCEED(pthread_mutex_init(&taskListMutex, NULL));

	int jobResilt=0;

	size_t taskWeight;
	for(globIterCounter = 0; globIterCounter < LEN_LIST; ++globIterCounter){
		while(1){
			get_new_task(globIterCounter, &taskWeight);
			if(taskWeight == -1)
				break;

			for(int i = 0; i < taskWeight; ++i){
				jobResilt += sin(i);
			}

		}
	}

	free_task_list();
	MPI_Finalize();

}
