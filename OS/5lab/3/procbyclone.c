#define _GNU_SOURCE
#include <sched.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <signal.h>
#include <string.h>

#define STACK_SIZE getpagesize()

void set_file_size(int fd, const size_t size){
	if(lseek(fd, size, SEEK_SET) == -1){
		perror("lseek() error:");
		return;
	}

	if(write(fd, "", 1) == -1){
		perror("write() error:");
		return;
	}
}

void* add_region_for_stack(const char* path, const size_t size){
	int fd = open(path, O_RDWR | O_SYNC | O_CREAT, 0600);
	if(fd != -1){
		set_file_size(fd, size);
		void* ptr = mmap(NULL, size, PROT_READ | PROT_WRITE,
				 MAP_SHARED_VALIDATE /*| MAP_SYNC*/, fd, 0);
		if(ptr != MAP_FAILED){
			memset(ptr, 0xFF, size);
			return ptr;
		} else {
			perror("mmap() error");
		}
		close(fd);
	} else {
		perror("open() error");
	}
	return NULL;
}

void alloc_string(int counter){
	char str[12] = "hello world";
	sleep(5);
	if(counter < 10){
		char str[17]="alloc_string_call";
		alloc_string(++counter);
	}

	char str1[16] = "alloc_string_out";
}


int child_entrance(){
	printf("child_entrance %p\n", child_entrance);
	printf("alloc_string %p\n", alloc_string);
	printf("child_entrance %p\n", child_entrance);

	int counter = 0;
	int exit_code = 7;

	printf("%p\n", &counter);
	printf("%p\n", &exit_code);

	char str[17]="alloc_string_call";
	alloc_string(counter);

	return exit_code;
}

int main(){
	printf("main %p\n", main);
	void* end_child_stack = add_region_for_stack("stack", STACK_SIZE);
	if(end_child_stack != NULL){
		pid_t child_pid = clone(child_entrance, end_child_stack+STACK_SIZE, SIGCHLD /*SIGSEGV*/, NULL);
		if(child_pid == -1){
			printf("clone() error: %s", strerror(errno));
			exit(-1);
		}
	} else {
		perror("begin_child_stack() error");
	}
}


