#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>
#include <signal.h>

#define LEN_LIST 5
#define N_TASK_FOR_EACH 10

#define ASK_SENSITIVITY 5

#define L 10

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

#define ASK_TASKS_TAG 100
#define SEND_TASKS_TAG 200

int* taskList;
int rank, size;

int glob_iter_counter;
pthread_mutex_t taskListMutex;

int pos_begin_task;
int load_counter = N_TASK_FOR_EACH;
int new_load_counter;
int counter_task;

void print_task_list(){
	printf("rank: %d\n", rank);
	for(int i = 0; i < size*N_TASK_FOR_EACH; ++i){
		printf("%d ", taskList[i]);
	}
	printf("\n");
}

void print_array(const int* arr, int size){
	for(int i = 0; i < size; ++i){
		printf("%d ", arr[i]);
	}
	printf("\n");
}

void print_array_from_size(const int* arr, const size_t a, const size_t size){
	printf("%d rank :", rank);
	for(int i = a; i < a+size; ++i){
		printf("%d ", arr[i]);
	}
	printf("\n");
}

void generate_task_list(){
	pthread_mutex_lock(&taskListMutex);
	load_counter = N_TASK_FOR_EACH;
	counter_task = pos_begin_task;
	for(int i = 0; i < N_TASK_FOR_EACH*size; ++i){
		//taskList[i] = abs(N_TASK_FOR_EACH/2-i%N_TASK_FOR_EACH) * abs(rank-(glob_iter_counter%size)) * L;
		taskList[i] = ((i%N_TASK_FOR_EACH+1));
	}
	pthread_mutex_unlock(&taskListMutex);
}

void get_rank_by_max_load(const int* load, int* maxRank, int* weight){
	int max = 0;

	for(int i = 0; i < size; ++i){
		if(load[i] > max){
			max = load[i];
			*maxRank = i;
		}
	}

	*weight = max / 2;
}

void ask_new_tasks(){
	int loadStat[size];
	MPI_Request reqs[size-1];

	int reqCounter = 0;
	for(int i = 0; i < size; ++i){
		if(i != rank){
			MPI_Isend(&rank, 1, MPI_INT, i, ASK_TASKS_TAG, MPI_COMM_WORLD, &reqs[reqCounter++]);
		}
	}

	int recvFromRank = -1, newNTasks = 0, zeroLoad = 0;
	MPI_Allgather(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
	get_rank_by_max_load(loadStat, &recvFromRank, &newNTasks);

	if(newNTasks > 0){
		printf("%d going to recv %d from %d\n", rank, newNTasks, recvFromRank);
		MPI_Recv(&taskList[pos_begin_task], newNTasks, MPI_INT,
				recvFromRank, SEND_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		load_counter = newNTasks;
	} else {
		taskList[pos_begin_task] = -1;
		load_counter = 1;
	}
	counter_task = pos_begin_task;

	MPI_Waitall(size-1, reqs, MPI_STATUSES_IGNORE);
}

void get_new_task(int* weight){
	pthread_mutex_lock(&taskListMutex);

	if(load_counter <= 0){
		ask_new_tasks();
	}

	*weight = taskList[counter_task++];
	--load_counter;

	pthread_mutex_unlock(&taskListMutex);
}


void sigusr_contibuter_handler(int signal){
	exit(0);
}

void* contributer_routine(void* b){
	signal(SIGUSR1, sigusr_contibuter_handler);
	int loadStat[size];

	int askingRank;
	MPI_Request send_req;

	int local_iter_counter = 0;
	while(glob_iter_counter < LEN_LIST){
		MPI_Recv(&askingRank, 1, MPI_INT, MPI_ANY_SOURCE, ASK_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		pthread_mutex_lock(&taskListMutex);

		int sendingRank = -1, nSendingTasks = 0;
		MPI_Allgather(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &sendingRank, &nSendingTasks);

		if(sendingRank == rank && nSendingTasks > 0){
			printf("%d going to send %d to %d\n", rank, nSendingTasks, askingRank);
			MPI_Isend(&taskList[counter_task+load_counter-nSendingTasks], nSendingTasks,
					MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD, &send_req);
			print_array_from_size(taskList, counter_task+load_counter-nSendingTasks+1, nSendingTasks);
			load_counter -= nSendingTasks;
			printf("%d send: ", rank);
		}

		pthread_mutex_unlock(&taskListMutex);
		
		if(sendingRank == rank && nSendingTasks > 0){
			MPI_Wait(&send_req, MPI_STATUS_IGNORE);
		}
	}

	return NULL;
}


int main(int argc, char** argv){
	int threadImpMPI;
	MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &threadImpMPI);

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if(rank == 0) printf("MPI implementation OK: %d\n", threadImpMPI == MPI_THREAD_MULTIPLE);

	pthread_t contributer;
	POSIX_SUCCEED(pthread_create(&contributer, NULL, contributer_routine, NULL));
	POSIX_SUCCEED(pthread_detach(contributer));
	POSIX_SUCCEED(pthread_mutex_init(&taskListMutex, NULL));

	pos_begin_task = rank*N_TASK_FOR_EACH;
	counter_task = pos_begin_task;

	int c = 0;

	unsigned long long jobResult = 0;

	int taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);

	for(glob_iter_counter = 0; glob_iter_counter < LEN_LIST; ++glob_iter_counter){
		generate_task_list();
		while(1){
			get_new_task(&taskWeight);
			if(taskWeight < 0){
				break;
			}

			++c;
			//printf("(%d, %d) ", rank, taskWeight);
			//for(int i = 0; i < taskWeight; ++i){
			sleep((long)rank);
				jobResult += taskWeight*1LL;
			//}
		}

		MPI_Barrier(MPI_COMM_WORLD);
	}

	printf("%d DONE!! %lld %d %d\n", rank, jobResult, c, N_TASK_FOR_EACH);

	unsigned long long result;
	MPI_Reduce(&jobResult, &result, 1, MPI_LONG_LONG_INT, MPI_SUM, 0, MPI_COMM_WORLD);
	
	if(rank == 0)
		printf("!!!!!!!!!!!!!!!!! result: %lld\n", result);


	POSIX_SUCCEED(pthread_kill(contributer, SIGUSR1));
	POSIX_SUCCEED(pthread_mutex_destroy(&taskListMutex));

	MPI_Finalize();

}
