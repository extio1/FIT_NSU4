#include <stdio.h>
#include <sys/mman.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>

int main(){
	const int max_num = 333;
	int i = 0;
	unsigned int num = 0;

	int pipefd[2];
	if( pipe(pipefd) == -1 ){
		perror("pipe() error");
		exit(-1);
	}

	pid_t pid = fork();
	if( pid == -1 ){
		perror("fork() error");
		exit(-1);
	}


	printf("max value: %d\n", max_num);
	if(pid == 0){
		if( close(pipefd[1]) == -1 ){
			perror("close() error");
		}
		unsigned int prev_num;
		unsigned int num;
		if( read(pipefd[0], &prev_num, sizeof(unsigned int)) == -1){
			perror("read() error");
			exit(-1);
		} else {
			while(1){
				if( read(pipefd[0], &num, sizeof(unsigned int)) == -1){
					perror("read() error");
					exit(-1);
				}
				//printf("%d ", num);
				if( num != prev_num+1 && prev_num != max_num){
					printf("current num !=  previous+1 (%d != %d+1)\n", num, prev_num);
				}
				prev_num = num;
			}
		}
		close(pipefd[1]);
	} else {
		if( close(pipefd[0]) == -1 ){
			perror("close() error");
		}
		unsigned int num = 0;
		while(1){
			if( write(pipefd[1], &num, sizeof(unsigned int)) == -1 ){
				perror("writer process write() error");
			}
			if( num >= max_num ){
				num = 0;
			} else {
				++num;
			}
		}
		close(pipefd[0]);
	}

}
