#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <sys/resource.h>
#include <signal.h>

#define NPAGES 300

uint64_t size_heap_allocated = 0;

void heap_alloc(uint64_t size){
	static int page_counter = 1;
	char* buffer = malloc(4096);
	size += 4096;

	printf("%ld bytes allocated - %d pages\n", size, page_counter);
	usleep(100);
	++page_counter;

	printf("reading the buffer - %d ", buffer[0]);
	if(page_counter < 10000000)
		heap_alloc(size);

	free(buffer);
}

void print_limit_data(){
	struct rlimit lim;
	if(getrlimit(RLIMIT_DATA, &lim) == -1){
		perror("getrlimit() error\n");
	}
	printf("data size: soft %ld, hard %ld\n", lim.rlim_cur, lim.rlim_max);
}

void change_limit_data(rlim_t rlim_soft, rlim_t rlim_hard){
	struct rlimit new_limit = {rlim_soft, rlim_hard};

	if(setrlimit(RLIMIT_DATA, &new_limit) == -1){
		perror("setrlimit() error\n");
	} else {
		printf("data limit changed to soft:%ld, hard:%ld\n", rlim_soft, rlim_hard);
	}
}

void stop_recurs(){
	printf("SIGSEGV got\n");
	exit(3);
}

int main(){
	print_limit_data();
	change_limit_data(4096*NPAGES, -1);

	pid_t pid = getpid();
	printf("%d\n", pid);
	sleep(10);

	signal(SIGSEGV, stop_recurs);
	heap_alloc(size_heap_allocated);

	return 0;
}

