#include <stdlib.h>
#include <errno.h>
#include <stdio.h>
#include <unistd.h>

int main(){
	pid_t pid = fork();

	if(pid == -1){
		perror("fork() error");
	}

	if(pid == 0){
		sleep(5);
		exit(10);
	} else {
		sleep(4);
		printf("ppid : %d\n", getppid());
		exit(1);
	}
}
