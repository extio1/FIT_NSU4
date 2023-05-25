#include <stdbool.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <stdio.h>
#include <math.h>
#include <mpi.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <cassert>
#include <limits.h>

#define LEN_LIST 15
#define N_TASK_FOR_EACH 2000
#define SENSITIVITY 5

#define L 100

#define POSIX_SUCCEED(func) if(func == -1) {perror("func() error"); exit(-1);}

#define ASK_TASKS_TAG 100
#define SEND_TASKS_TAG 200
#define SEND_TASKS_SIZE_TAG 201
#define NOTIFY_CONTIBUTER_TAG 300

int* taskList;
int* tempList;

int rank, size;
const int managerRank = 0;

int glob_iter_counter;
pthread_mutex_t taskListMutex;

int new_tasks_ready = 0;

int pos_begin_task;
int load_counter = N_TASK_FOR_EACH;
int new_load_counter;
int counter_task;

void sigusr_contibuter_handler(int signal){
	exit(0);
}

void print_disbalance_params(int duration, MPI_Comm comm){
	int main_worker_rank = 1;
	int workers_size = size-1;
	int durs[workers_size];
	MPI_Gather(&duration, 1, MPI_DOUBLE, durs, 1, MPI_DOUBLE, main_worker_rank, comm);

	double max_diff = 0;
	double max_dur = 0;
	if(rank == main_worker_rank){
		for(int i = 0; i < workers_size; ++i){
			for(int j = i; j < workers_size; ++j){
				double diff = abs(durs[i]-durs[j]);
				if(diff > max_diff)
					max_diff = diff;
			}

			if(durs[i] > max_dur)
				max_dur = durs[i];
		}
		printf("[%d] iteration: max diff %f\n", rank, max_diff);
		printf("[%d] iteration: diabalance proportion %f %%\n\n", rank, max_dur/max_diff*100);
	}
}

void generate_task_list(){
	pthread_mutex_lock(&taskListMutex);
	load_counter = N_TASK_FOR_EACH;
	counter_task = pos_begin_task;
	for(int i = 0; i < N_TASK_FOR_EACH*size; ++i){
		taskList[i] = abs(N_TASK_FOR_EACH/2-i%N_TASK_FOR_EACH) * abs(rank-(glob_iter_counter%size)) * L;
	}
	pthread_mutex_unlock(&taskListMutex);
}

void get_rank_by_max_load(const int* load, int* maxRank, int* weight){
	int max = 0;

	for(int i = 0; i < size; ++i){
		if(load[i] >= max){
			max = load[i];
			*maxRank = i;
		}
	}

	*weight = max / 2;
}

int n_new_tasks = -1;
void get_new_task(int* weight){
	pthread_mutex_lock(&taskListMutex);

	if(load_counter <= 0){
		MPI_Send(&rank, 1, MPI_INT, managerRank, ASK_TASKS_TAG, MPI_COMM_WORLD);

		pthread_mutex_unlock(&taskListMutex);
		MPI_Recv(&n_new_tasks, 1, MPI_INT, MPI_ANY_SOURCE, SEND_TASKS_SIZE_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		MPI_Recv(tempList, n_new_tasks, MPI_INT, MPI_ANY_SOURCE, SEND_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
		pthread_mutex_lock(&taskListMutex);

		for(int i = 0; i < n_new_tasks; ++i){
			taskList[pos_begin_task+i] = tempList[i];
		}

		counter_task = pos_begin_task;
		load_counter = n_new_tasks;

	}

	*weight = taskList[counter_task++];
	--load_counter;

	pthread_mutex_unlock(&taskListMutex);
}

void foo(int* loadStat){
	MPI_Gather(&load_counter, 1, MPI_INT, loadStat, 1, MPI_INT, managerRank, MPI_COMM_WORLD);
}

void* contributer_routine(void* b){
	signal(SIGUSR1, sigusr_contibuter_handler);

	while(1){
		int askingRank = -1;
		MPI_Bcast(&askingRank, 1, MPI_INT, managerRank, MPI_COMM_WORLD);
		if(askingRank == rank){
			int ignore_asking_proc_size = -1;
			int ignore_info[2];
			MPI_Gather(&ignore_asking_proc_size, 1, MPI_INT, NULL, 0, MPI_DATATYPE_NULL, managerRank, MPI_COMM_WORLD);
			MPI_Bcast(ignore_info, 2, MPI_INT, managerRank, MPI_COMM_WORLD);	
			continue;
		}


		pthread_mutex_lock(&taskListMutex);
		MPI_Gather(&load_counter, 1, MPI_INT, NULL, 0, MPI_DATATYPE_NULL, managerRank, MPI_COMM_WORLD);

		int sendingInfo[2];
		MPI_Bcast(sendingInfo, 2, MPI_INT, managerRank, MPI_COMM_WORLD);

		int sending_rank = sendingInfo[0];
		int n_sending_task = sendingInfo[1];
		if(sending_rank == rank){
			if(n_sending_task > 0){
				MPI_Send(&n_sending_task, 1, 
						MPI_INT, askingRank, SEND_TASKS_SIZE_TAG, MPI_COMM_WORLD);
				MPI_Send(&taskList[counter_task+load_counter-n_sending_task], n_sending_task,
						MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD);
				load_counter -= n_sending_task;
			} else {
				int eof[2] = {1, -1};
				MPI_Send(&eof[0], 1, MPI_INT, askingRank, SEND_TASKS_SIZE_TAG, MPI_COMM_WORLD);
				MPI_Send(&eof[1], 1, MPI_INT, askingRank, SEND_TASKS_TAG, MPI_COMM_WORLD);
			}
			
		}

		pthread_mutex_unlock(&taskListMutex);
	}

	return NULL;
}

void manager(){
	signal(SIGUSR1, sigusr_contibuter_handler);

	int load_counter_manager = -1;
	int askingRank = -1;
	int loadStat[size];
	while(1){
		MPI_Recv(&askingRank, 1, MPI_INT, MPI_ANY_SOURCE, ASK_TASKS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

		MPI_Bcast(&askingRank, 1, MPI_INT, managerRank, MPI_COMM_WORLD);
		MPI_Gather(&load_counter_manager, 1, MPI_INT, loadStat, 1, MPI_INT, managerRank, MPI_COMM_WORLD);

		int sendingRank = 0, nSendingTasks = 0;
		get_rank_by_max_load(loadStat, &sendingRank, &nSendingTasks);

		int sendingInfo[2] = {sendingRank, nSendingTasks};
		MPI_Bcast(sendingInfo, 2, MPI_INT, managerRank, MPI_COMM_WORLD);
	}

	MPI_Finalize();
}

int main(int argc, char** argv){
	int threadImpMPI;
	MPI_Init_thread(&argc, &argv, MPI_THREAD_MULTIPLE, &threadImpMPI);

	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	MPI_Comm proletariat_comm;
	MPI_Comm_split(MPI_COMM_WORLD, (rank==0), rank, &proletariat_comm);

	if(rank == 0) manager();

	pthread_t contributer;
	POSIX_SUCCEED(pthread_create(&contributer, NULL, contributer_routine, NULL));
	POSIX_SUCCEED(pthread_detach(contributer));

	POSIX_SUCCEED(pthread_mutex_init(&taskListMutex, NULL));

	pos_begin_task = rank*N_TASK_FOR_EACH;
	counter_task = pos_begin_task;

	double prog_start, prog_end;

	double jobResult = 0;

	int taskWeight;
	taskList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH*size);
	tempList = (int*)malloc(sizeof(int)*N_TASK_FOR_EACH);

	prog_start = MPI_Wtime();

	for(glob_iter_counter = 0; glob_iter_counter < LEN_LIST; ++glob_iter_counter){
		generate_task_list();
		double start, finish;
		start = MPI_Wtime();
		while(1){
			get_new_task(&taskWeight);
			if(taskWeight < 0){
				break;
			}

			for(int i = 0; i < taskWeight; ++i){
				jobResult += sin(i);
			}
		}
		finish = MPI_Wtime();

		double iter_duration = finish-start;
		printf("%d rank: [%d] iteration takes %f sec.\n", rank, glob_iter_counter, iter_duration);
		MPI_Barrier(proletariat_comm);
		print_disbalance_params(iter_duration, proletariat_comm);
		
		MPI_Barrier(proletariat_comm);
	}

	prog_end = MPI_Wtime();

	double result = 0;
	MPI_Allreduce(&jobResult, &result, 1, MPI_DOUBLE, MPI_SUM, proletariat_comm);
	if(rank == 1){
		printf("result: %f\n", result);
		printf("-------------------------------\nGlobal program execution time: %fsec.\n", prog_end-prog_start);
	}


	POSIX_SUCCEED(pthread_kill(contributer, SIGUSR1));
	POSIX_SUCCEED(pthread_mutex_destroy(&taskListMutex));

	free(taskList);
	free(tempList);

	MPI_Finalize();

}
