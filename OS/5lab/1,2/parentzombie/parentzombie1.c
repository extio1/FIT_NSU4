#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>

int main(int argc, char** argv){
	pid_t mypid = getpid();

	pid_t pid = fork();
	if(pid == -1){
		perror("fork() error");
		return -1;
	}

	if(pid == 0){
		execv("./parentzombie2", argv);
	} else {
		sleep(10);
	}
}
