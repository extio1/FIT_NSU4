#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <unistd.h>
#include <sys/resource.h>

void stack_alloc(uint64_t* size){
	static int page_counter = 1;
	char buffer[4096];
	*size += 4096;
	printf("%ld bytes allocated - %d pages\n", *size, page_counter);
	//usleep(100);
	++page_counter;
	stack_alloc(size);
}

void print_limit_stack(){
	struct rlimit lim;
	if(getrlimit(RLIMIT_STACK, &lim) == -1){
		perror("getrlimit() error\n");
	}
	printf("stack size: soft %ld, hard %ld\n", lim.rlim_cur, lim.rlim_max);
}

void change_limit_stack(rlim_t rlim_soft, rlim_t rlim_hard){
	//rlim_t rlim_soft = 4096*10;
	//rlim_t rlim_hard = -1;
	struct rlimit new_limit = {rlim_soft, rlim_hard};

	if(setrlimit(RLIMIT_STACK, &new_limit) == -1){
		perror("setrlimit() error\n");
	} else {
		printf("Stack limit changed to soft:%ld, hard:%ld\n", rlim_soft, rlim_hard);
	}
}

int main(){
	uint64_t* stack_size = calloc(1, 8);

	print_limit_stack();
	change_limit_stack(-1, -1);

	pid_t pid = getpid();
	printf("%d\n", pid);
	sleep(10);

	stack_alloc(stack_size);

	return 0;
}

