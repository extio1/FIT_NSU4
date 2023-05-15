#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/mman.h>
#include <signal.h>

#define PAGE_SIZE 4096

void sigsegv_handler(int num){
	write(1, "! SIGSEGV captured :Program will be finished with exit code -1.\n", 64);
	_exit(-1);
}

int main(){
	signal(SIGSEGV, sigsegv_handler);
	pid_t pid = getpid();
	printf("%d\n", pid);
	sleep(10);

	char* ptr_to_new_region = mmap(NULL, PAGE_SIZE*10, PROT_NONE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
	if(ptr_to_new_region != MAP_FAILED){
		printf("New region added at %p\n", ptr_to_new_region);
	} else {
		perror("Failed while mmap() ");
		exit(-1);
	}

	sleep(5);

	if(munmap(ptr_to_new_region+PAGE_SIZE*4, PAGE_SIZE*2) != 0){
		perror("Failed while munmap() ");
		exit(-1);
	} else {
		printf("Pages from 4 to 6 unmapped\n");
	}

	sleep(5);

	printf("Trying to read from new region\n");
	printf("%c", *ptr_to_new_region);

	return 0;
}

