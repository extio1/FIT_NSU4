#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>

#define LEN_LIST 2
#define N_TASK_FOR_EACH 100

#define ASK_SENSITIVITY 5

#define L 10

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

#define ASK_TASKS_TAG 100
#define SEND_TASKS_TAG 200

int* taskList;
int rank, size;

int new_tasks_was_required = 0;
int new_tasks_ready = 0;
int was_request_for_new_tasks = 0;

int glob_iter_counter;
pthread_mutex_t taskListMutex;
pthread_mutex_t initAskMutex;
pthread_mutex_t newTasksReadyMutex;

pthread_cond_t task_left_eq_sens_cond;
pthread_cond_t new_tasks_ready_cond;

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

void generate_task_list(){
	load_counter = N_TASK_FOR_EACH;
	counter_task = pos_begin_task;
	for(int i = 0; i < N_TASK_FOR_EACH*size; ++i){
		taskList[i] = abs(N_TASK_FOR_EACH/2-i%N_TASK_FOR_EACH) * abs(rank-(glob_iter_counter%size)) * L;
	}
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

// is was_request_for_new_tasks really need?

void get_new_task(int* weight){

	pthread_mutex_lock(&taskListMutex);

	if(load_counter <= 0){
		if(new_tasks_was_required){
				pthread_mutex_lock(&newTasksReadyMutex);

				if(!new_tasks_ready){
					pthread_mutex_unlock(&taskListMutex);
					pthread_cond_wait(&new_tasks_ready_cond, &newTasksReadyMutex);
					pthread_mutex_lock(&taskListMutex);
				}

				new_tasks_ready = 0; // поток asker положил сюда новые задачи 
				counter_task = pos_begin_task; // в числе new_load_counter штук
				load_counter = new_load_counter;

				new_tasks_was_required = 0;

				pthread_mutex_unlock(&newTasksReadyMutex);
		} else {
			counter_task = pos_begin_task;
			load_counter = 1;
			*weight = -1;
			pthread_mutex_unlock(&taskListMutex);
			return;
		}
	}

	*weight = taskList[counter_task];

	if(counter_task >= pos_begin_task+N_TASK_FOR_EACH){
		counter_task = pos_begin_task;
	} else {
		++counter_task;
	}

	--load_counter;

	pthread_mutex_lock(&initAskMutex);
	if(load_counter == ASK_SENSITIVITY){
		new_tasks_was_required = 1;
		pthread_cond_signal(&task_left_eq_sens_cond);
	}
	pthread_mutex_unlock(&initAskMutex);

	pthread_mutex_unlock(&taskListMutex);
}

void* asker_routine(void* d){
	MPI_Request req[size-1];
	int loadStat[size];
	int zeroLoad = 0;

	while(1){
		pthread_mutex_lock(&initAskMutex);
		if(!new_tasks_was_required){
			pthread_cond_wait(&task_left_eq_sens_cond, &initAskMutex);
		}
		pthread_mutex_unlock(&initAskMutex);

		int reqcounter;
		for(int i = 0; i < size; ++i){
			if(i != rank){
				MPI_Isend(&rank, 1, MPI_INT, i, ASK_TASKS_TAG, MPI_COMM_WORLD, &req[reqcounter++]);
			}
		}

		int recvFromRank = -1, newNTasks = 0;
		MPI_Allgather(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &recvFromRank, &newNTasks);

		pthread_mutex_lock(&taskListMutex);

		if(newNTasks > 0){
			MPI_Recv(&taskList[pos_begin_task], newNTasks, MPI_INT,
					recvFromRank, SEND_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			new_load_counter = newNTasks;
		} else {
			new_load_counter = 1;
		}

		pthread_mutex_lock(&newTasksReadyMutex);
		new_tasks_ready = 1;
		pthread_cond_signal(&new_tasks_ready_cond);
		pthread_mutex_unlock(&newTasksReadyMutex);

		pthread_mutex_unlock(&taskListMutex);

		MPI_Waitall(size-1, req, MPI_STATUSES_IGNORE);

	}
}

void* contributer_routine(void* b){
	int loadStat[size];

	int askingRank;
	MPI_Request sendReq;

	while(1){
		MPI_Recv(&askingRank, 1, MPI_INT, MPI_ANY_SOURCE, ASK_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		pthread_mutex_lock(&taskListMutex);

		int sendingRank = -1, nSendingTasks = 0;
		MPI_Allgather(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &sendingRank, &nSendingTasks);

		if(sendingRank == rank && nSendingTasks > 0){
			MPI_Isend(&taskList[counter_task+load_counter-nSendingTasks], nSendingTasks,
					MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD, &sendReq);
			load_counter -= nSendingTasks;
		}

		pthread_mutex_unlock(&taskListMutex);

		if(sendingRank == rank && nSendingTasks > 0)
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

	pos_begin_task = rank*N_TASK_FOR_EACH;
	counter_task = pos_begin_task;

	float jobResult = 0;

	int taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);
	for(glob_iter_counter = 0; glob_iter_counter < LEN_LIST; ++glob_iter_counter){
		generate_task_list();

		while(1){
			get_new_task(&taskWeight);
			if(taskWeight < 0){
				break;
			}

			for(int i = 0; i < taskWeight; ++i){
				jobResult += sin(i);
			}
		}

		printf("%d OUT !!!!!!!%d iter!!!!\n", rank, glob_iter_counter);

		MPI_Barrier(MPI_COMM_WORLD);
	}

	printf("%d DONE!! %f \n", rank, jobResult);


	pthread_kill(contributer, SIGINT);
	pthread_kill(asker, SIGINT);

	MPI_Finalize();

}
