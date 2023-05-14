#include <stdio.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

int main(){
	int shfd = open("shared_file", O_RDONLY);

	if(shfd == -1){
		perror("open() error");
	}

	const size_t region_size = getpagesize();
	void* shared_start = mmap(NULL, region_size, PROT_READ, MAP_SHARED, shfd, 0);
	close(shfd);

	if(shared_start == MAP_FAILED){
		perror("mmap() error");
	}

	unsigned int* buffer = (unsigned int*) shared_start;
	unsigned int prev_num = 0;
	int i = 0;
	while(1){
		unsigned int num = buffer[i];
		if(num != prev_num+1){
			printf("Previuos num %d current %d\n", prev_num, num);
		}
		if(i >= (region_size / sizeof(unsigned int)-1)){
			i = 0;
		} else {
			++i;
		}
		prev_num = num;
	}

	if( munmap(shared_start, getpagesize()) == -1 ){
		perror("munmap error");
	}
}
