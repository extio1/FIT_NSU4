#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>

#define LEN_LIST 2
#define N_TASK_FOR_EACH 10

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

// is was_request_for_new_tasks really need?

void eof_transmit(int* weight){
	counter_task = pos_begin_task;
	load_counter = 0;
	taskList[counter_task] = -1; 
	*weight = -1;
}

void get_new_task(int* weight){

	pthread_mutex_lock(&taskListMutex);

	if(load_counter <= 0){
		printf("%d SDFSDFSDFSDF %d %d %d\n", rank, counter_task, new_tasks_was_required, load_counter);
		if(new_tasks_was_required){
				pthread_mutex_lock(&newTasksReadyMutex);
				if(!new_tasks_ready){
					printf("wait\n\n");
					pthread_mutex_unlock(&taskListMutex);
					pthread_cond_wait(&new_tasks_ready_cond, &newTasksReadyMutex);
					pthread_mutex_lock(&taskListMutex);
				}

				printf("%d ggooott %d %d %d\n", rank, pos_begin_task, new_load_counter, taskList[pos_begin_task]);

				if(new_load_counter == 0){
					eof_transmit(weight);
					pthread_mutex_unlock(&taskListMutex);
					return;
				}

				new_tasks_ready = 0;
				pthread_mutex_unlock(&newTasksReadyMutex);

				new_tasks_was_required = 0;
				counter_task = pos_begin_task;
				load_counter = new_load_counter;
		} else {
			eof_transmit(weight);
			pthread_mutex_unlock(&taskListMutex);
			return;
		}
	}

	*weight = taskList[counter_task++];
/*
	if(counter_task >= pos_begin_task+N_TASK_FOR_EACH){
		counter_task = pos_begin_task;
	} else {
		++counter_task;
	}
*/
	--load_counter;

	//pthread_mutex_lock(&initAskMutex);
	if(load_counter == ASK_SENSITIVITY){
		new_tasks_was_required = 1;
		pthread_cond_signal(&task_left_eq_sens_cond);
	}
	//pthread_mutex_unlock(&initAskMutex);

	//print_array_from_size(taskList, counter_task, load_counter);

	pthread_mutex_unlock(&taskListMutex);
}

void* asker_routine(void* d){
	MPI_Request req[size-1];
	int loadStat[size];
	int zeroLoad = 0;

	while(1){
		pthread_mutex_lock(&taskListMutex);
		//pthread_mutex_lock(&initAskMutex);
		if(!new_tasks_was_required){
			pthread_cond_wait(&task_left_eq_sens_cond, &taskListMutex);
		}
		printf("%d asker awaken\n", rank);
		//pthread_mutex_unlock(&initAskMutex);
		pthread_mutex_unlock(&taskListMutex);

		int reqcounter;
		for(int i = 0; i < size; ++i){
			if(i != rank){
				MPI_Send(&rank, 1, MPI_INT, i, ASK_TASKS_TAG, MPI_COMM_WORLD);
			}
		}

		int recvFromRank = -1, newNTasks = 0;
		MPI_Allgather(&zeroLoad, 1, MPI_INT, loadStat, 1, MPI_INT, MPI_COMM_WORLD);
		get_rank_by_max_load(loadStat, &recvFromRank, &newNTasks);

		pthread_mutex_lock(&taskListMutex);

		if(newNTasks > 0){
			MPI_Recv(&taskList[pos_begin_task], newNTasks, MPI_INT,
					recvFromRank, SEND_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			printf("%d recv: ", rank);
			new_load_counter = newNTasks;
			print_array(&taskList[pos_begin_task], new_load_counter);
		} else {
			taskList[pos_begin_task] = -1;
			new_load_counter = 0;
		}

		pthread_mutex_lock(&newTasksReadyMutex);
		new_tasks_ready = 1;
		pthread_cond_signal(&new_tasks_ready_cond);
		//printf("READY\n\n\n\n");
		pthread_mutex_unlock(&newTasksReadyMutex);

		pthread_mutex_unlock(&taskListMutex);
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
			MPI_Send(&taskList[counter_task+load_counter-nSendingTasks+1], nSendingTasks,
					MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD);
			print_array_from_size(taskList, counter_task+load_counter-nSendingTasks+1, nSendingTasks);
			load_counter -= nSendingTasks;
			printf("%d send: ", rank);
		}

		pthread_mutex_unlock(&taskListMutex);
	}
}

pthread_attr_t attrAsker, attrContributer;
void init_asker_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrAsker));
	POSIX_SUCCEED(pthread_create(tid, NULL, asker_routine, NULL));
	pthread_detach(*tid);
}

void init_contributer_thread(pthread_t* tid){
	POSIX_SUCCEED(pthread_attr_init(&attrContributer));
	POSIX_SUCCEED(pthread_create(tid, NULL, contributer_routine, NULL));
	pthread_detach(*tid);
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

	int c = 0;

	unsigned long long jobResult = 0;

	int taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);

	for(glob_iter_counter = 0; glob_iter_counter < LEN_LIST; ++glob_iter_counter){
		generate_task_list();

		if(rank == 1){ printf("\nbegins\n"); print_array(taskList, N_TASK_FOR_EACH*size); printf("\nend\n");}

		MPI_Barrier(MPI_COMM_WORLD);

		while(1){
			get_new_task(&taskWeight);
			if(taskWeight < 0){
				break;
			}

			++c;
			printf("(%d, %d) ", rank, taskWeight);
			//for(int i = 0; i < taskWeight; ++i){
			//usleep(1000*(rank%glob_iter_counter+1));
				jobResult += taskWeight*1LL;
			//}
		}

		printf("\n---------\n");

		printf("%d OUT !!!!!!!%d iter!!!!\n", rank, glob_iter_counter);

		MPI_Barrier(MPI_COMM_WORLD);
	}

	printf("%d DONE!! %lld %d %d\n", rank, jobResult, c, N_TASK_FOR_EACH);

	unsigned long long result;
	MPI_Reduce(&jobResult, &result, 1, MPI_LONG_LONG_INT, MPI_SUM, 0, MPI_COMM_WORLD);
	if(rank == 0)
		printf("!!!!!!!!!!!!!!!!! result: %lld\n", result);

	//pthread_kill(contributer, SIGINT);
	//pthread_kill(asker, SIGINT);
	pthread_cancel(contributer);
	pthread_cancel(asker);

	MPI_Finalize();

}
