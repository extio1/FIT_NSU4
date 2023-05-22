#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>

#define LEN_LIST 5
#define N_TASK_FOR_EACH 100

#define SENDER_SENSITIVITY 3

#define L 10

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

int* taskList;

int globIterCounter;
pthread_mutex_t taskListMutex;
pthread_cond_t taskLeftLessThanSens; 

int rank, size;

void print_task_list(){
	printf("rank: %d\n", rank);
	for(int i = 0; i < size*N_TASK_FOR_EACH; ++i){
		printf("%d ", taskList[i]);
	}
	printf("\n");
}

void generate_task_list(){
	for(int i = 0; i < N_TASK_FOR_EACH*size; ++i){
		taskList[i] = abs(N_TASK_FOR_EACH/2-i%N_TASK_FOR_EACH) * abs(rank-(globIterCounter%size)) * L;
	}
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

int A = rank*N_TASK_FOR_EACH;
int B = (rank+1)*N_TASK_FOR_EACH;

int load_counter = N_TASK_FOR_EACH;
int counter_task = A;
void get_new_task(const int currIt, size_t* weight){
	pthread_mutex_lock(&taskListMutex);
	*weight = taskList[counter_task++];
	--load_counter;

	if(counter_task == B)
		pthread_cond_signal(&taskLeftLessThanSens);

	pthread_mutex_unlock(&taskListMutex);
}

void* asker_routine(void* d){
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
				MPI_Isend(&rank, 1, MPI_INT, i, MPI_ANY_TAG, MPI_COMM_WORLD, &req[reqcounter++]);
			}
		}

		MPI_Start(&allgatherReq);

		int recvFromRank;
		int new_weight;
		get_rank_by_max_load(&recvFromRank, &recvFromRank, &new_weight);

		MPI_Recv(taskList[globIterCounter], new_weight, MPI_INT, 
			recvFromRank, MPI_ANY_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		pthread_mutex_unlock(&taskListMutex);
		load_counter += new_weight;

		MPI_Waitall(size-1, req, MPI_STATUSES_IGNORE);
	}
}

void* contributer_routine(void* b){
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
			MPI_Isend(&taskList[globIterCounter]+counter_task, weight, 
				MPI_INT, askingRank, MPI_ANY_TAG, MPI_COMM_WORLD, &sendReq)
			counter_task += weight;
		}

		pthread_mutex_unlock(&taskListMutex);

		if(busyRank == rank)
			MPI_Wait(&sendReq, MPI_STATUS_IGNORE);
	}
}

pthread_attr_t attrAsker, attrContributer;
void init_asker_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrAsker));
	POSIX_SUCCEED(pthread_attr_setdetachstate(&attrAsker, PTHREAD_CREATE_DETACHED));
	POSIX_SUCCEED(pthread_create(tid, &attrAsker, asker_routine, NULL));
}

void init_contributer_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrContributer));
	POSIX_SUCCEED(pthread_attr_setdetachstate(&attrContributer, PTHREAD_CREATE_DETACHED));
	POSIX_SUCCEED(pthread_create(tid, &attrContributer, contributer_routine, NULL));
}

int refresh_counter(){
	counter_task = A;
}

int main(int argc, char** argv){
	int threadImpMPI;
	MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &threadImpMPI);
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if(rank == 0) printf("MPI implementation OK: %d\n", threadImpMPI == MPI_THREAD_MULTIPLE);


	pthread_t contributer, asker;
	init_contributer_thread(contributer);
	init_asker_thread(asker);
	POSIX_SUCCEED(pthread_mutex_init(&taskListMutex, NULL));

	int jobResilt = 0;

	size_t taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);
	for(globIterCounter = 0; globIterCounter < LEN_LIST; ++globIterCounter){
		generate_task_list();
		while(1){
			get_new_task(globIterCounter, &taskWeight);
			if(taskWeight == -1)
				break;

			for(int i = 0; i < taskWeight; ++i){
				jobResilt += sin(i);
			}

		}
		MPI_Barrier(MPI_COMM_WORLD);
	}

	pthread_kill(contributer);
	pthread_kill(asker);

	MPI_Finalize();

}
