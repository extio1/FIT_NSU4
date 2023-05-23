#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>

#define LEN_LIST 5
#define N_TASK_FOR_EACH 100

#define ASK_SENSITIVITY 30

#define L 10

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

#define ASK_TASKS_TAG 100
#define SEND_TASKS_TAG 200

int* taskList;
int rank, size;

int new_tasks_required = 0;
int new_tasks_ready = 0;

int glob_iter_counter;
pthread_mutex_t taskListMutex;
pthread_mutex_t initAskMutex;
pthread_mutex_t newTasksReadyMutex;

pthread_cond_t task_left_eq_sens_cond; 
pthread_cond_t new_tasks_ready_cond; 

int pos_begin_task = rank*N_TASK_FOR_EACH;
int load_counter = N_TASK_FOR_EACH;
int new_load_counter;
int counter_task = pos_begin_task;

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

void generate_task_list(){
	load_counter = N_TASK_FOR_EACH;
	counter_task = 0;
	for(int i = 0; i < N_TASK_FOR_EACH*size; ++i){
		taskList[i] = abs(N_TASK_FOR_EACH/2-i%N_TASK_FOR_EACH) * abs(rank-(glob_iter_counter%size)) * L;
	}
}

void get_rank_by_max_load(const int* load, int* maxRank, int* weight, int* sum){
	int max = 0;
	print_array(load, size);
	for(int i = 0; i < size; ++i){
		*sum += load[i];
		if(load[i] > max){
			max = load[i];
			*maxRank = i;
		}
	}

	*weight = max / 2;
}

void get_new_task(int* weight){

	pthread_mutex_lock(&taskListMutex);

	//printf("%d %d\n", load_counter, new_tasks_ready);
	while(load_counter <= 0 && !new_tasks_ready){
		pthread_mutex_unlock(&taskListMutex);
		pthread_cond_wait(&new_tasks_ready_cond, &initAskMutex);
		new_tasks_ready = 0;
		counter_task = 0;
		load_counter = new_load_counter;
		pthread_mutex_lock(&taskListMutex);
	}

	*weight = taskList[counter_task++];

	if(counter_task >= pos_begin_task+N_TASK_FOR_EACH){
		counter_task = pos_begin_task;
	} else {
		++counter_task;
	}

	--load_counter;

	pthread_mutex_lock(&initAskMutex);
	if(load_counter == ASK_SENSITIVITY){
		printf("SIGNALIZED %d %d\n", rank, load_counter);
		new_tasks_required = 1;
		pthread_cond_signal(&task_left_eq_sens_cond);
	}
	pthread_mutex_unlock(&initAskMutex);

	pthread_mutex_unlock(&taskListMutex);
}

void* asker_routine(void* d){
	MPI_Request req[size-1];
	int loadStat[size];
	int zeroLoad = 0;

	MPI_Request allgatherReq;
	//MPI_Allgather_init(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD,	MPI_INFO_NULL, &allgatherReq);

	while(1){
		pthread_mutex_lock(&initAskMutex);
		printf("%d STARTING WAIT\n", rank);
		while(!new_tasks_required){
			pthread_cond_wait(&task_left_eq_sens_cond, &initAskMutex);
			new_tasks_required = 0;
		}
		printf("\n++++++%dASKER AWAKES++++%d++\n\n", rank, load_counter);
		pthread_mutex_unlock(&initAskMutex);

		int reqcounter;
		for(int i = 0; i < size; ++i){
			if(i != rank){
				MPI_Isend(&rank, 1, MPI_INT, i, ASK_TASKS_TAG, MPI_COMM_WORLD, &req[reqcounter++]);
			}
		}

		int recvFromRank, newNTasks, loadSum = 0;
		MPI_Allgather(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &recvFromRank, &newNTasks, &loadSum);

		printf("========%d=========%d====%d=\n", rank, loadSum, newNTasks);

		pthread_mutex_lock(&taskListMutex);

		if(loadSum > 0){
			printf("%d recieving %d from %d", rank, newNTasks, recvFromRank);
			MPI_Recv(taskList, newNTasks, MPI_INT, 
				recvFromRank, SEND_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			
			new_load_counter = newNTasks;
			new_tasks_ready = 1;
			pthread_cond_signal(&new_tasks_ready_cond);
			pthread_mutex_unlock(&taskListMutex);
		} else {
			taskList[counter_task] = -1;
		}

		MPI_Waitall(size-1, req, MPI_STATUSES_IGNORE);
	}
}

void* contributer_routine(void* b){
	int loadStat[size];

	int askingRank;
	MPI_Request allgatherReq, recvReq, sendReq;
	MPI_Allgather_init(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD,	MPI_INFO_NULL, &allgatherReq);

	while(1){
		MPI_Recv(&askingRank, 1, MPI_INT, MPI_ANY_SOURCE, ASK_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		pthread_mutex_lock(&taskListMutex);

		int sendingRank, nSendingTasks, loadSum = 0;
		MPI_Allgather(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &sendingRank, &nSendingTasks, &loadSum);

		printf("========%d=========%d====%d=\n", rank, loadSum, nSendingTasks);
		print_array(loadStat, size);
		if(loadSum > 0){
			if(sendingRank == rank){
				printf("%d going to send %d to %d\n", rank, nSendingTasks, askingRank);
				MPI_Isend(taskList+load_counter-nSendingTasks, nSendingTasks, 
						MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD, &sendReq);
				load_counter -= nSendingTasks;
			}
		}

		pthread_mutex_unlock(&taskListMutex);

		if(sendingRank == rank && loadSum > 0)
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

int main(int argc, char** argv){
	int threadImpMPI;
	MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &threadImpMPI);
	
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	if(rank == 0) printf("MPI implementation OK: %d\n", threadImpMPI == MPI_THREAD_MULTIPLE);

	pthread_t contributer, asker;
	init_asker_thread(&asker);
	init_contributer_thread(&contributer);
	POSIX_SUCCEED(pthread_mutex_init(&taskListMutex, NULL));
	POSIX_SUCCEED(pthread_mutex_init(&initAskMutex, NULL));
	POSIX_SUCCEED(pthread_cond_init(&task_left_eq_sens_cond, NULL));
	POSIX_SUCCEED(pthread_cond_init(&new_tasks_ready_cond, NULL));

	int jobResilt = 0;

	int taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);
	for(glob_iter_counter = 0; glob_iter_counter < LEN_LIST; ++glob_iter_counter){
		generate_task_list();
		printf("%d NEW ITERATION HAPPENED\n", rank);
		while(1){
			get_new_task(&taskWeight);
			if(taskWeight == -1){
				break;
			}

			//printf("%d calcs %d, left %d\n", rank, taskWeight, load_counter);
			for(int i = 0; i < taskWeight; ++i){
				jobResilt += sin(i);
			}
		}
		MPI_Barrier(MPI_COMM_WORLD);
	}

	pthread_cancel(contributer);
	pthread_cancel(asker);
	//pthread_kill(contributer, SIGINT);
	//pthread_kill(asker, SIGINT);

	MPI_Finalize();

}
