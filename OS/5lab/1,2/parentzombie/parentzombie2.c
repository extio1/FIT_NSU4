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
		printf("Child make exit()\n");
		sleep(5);
		printf("%d %d\n", getpid(), getppid());
		exit(5);
	} else {
		sleep(15);
		exit(7);
	}
}
