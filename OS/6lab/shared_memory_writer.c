#include <stdio.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

int main(){
	int shfd = open("shared_file", O_RDWR);

	if(shfd == -1){
		perror("open() error");
	}

	size_t region_size = getpagesize();

	if(ftruncate(shfd, region_size)==-1){
		perror("ftruncate error");
	}

	void* shared_start = mmap(NULL, region_size, PROT_WRITE, MAP_SHARED, shfd, 0);
	close(shfd);

	if(shared_start == MAP_FAILED){
		perror("mmap() error");
	}

	memset(shared_start, 0, region_size);

	unsigned int* buffer = (unsigned int*) shared_start;
	const int max_num = 333;
	int i = 0;
	unsigned int num = 0;
	while(1){
		buffer[i] = num;
		++i;
		if(i >= region_size / sizeof(unsigned int)){
			i = 0;
		}
		if(num > max_num){
			num = 0;
		} else {
			++num;
		}
	}

	if( munmap(shared_start, getpagesize()) == -1 ){
		perror("munmap error");
	}
}
