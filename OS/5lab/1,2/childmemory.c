#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/wait.h>

int global = 505;

int main(){
	int local = 404;

	printf("(Parent) pid %d ppid %d\n", getpid(), getppid());
	printf("(Parent) DATA: Global %d, local %d\n", global, local);
	printf("(Parent) PTR : Global %p, local %p\n", &global, &local);

	pid_t pid;
	if((pid = fork()) == -1){
		perror("Error while fork()");
		return -1;
	}

	printf("Parent pid %d, child pid %d\n", getpid(), pid);
	sleep(2);
	if(pid == 0){
		printf("(Child) pid %d, ppid %d\n", getpid(), getppid());
		printf("(Child) DATA: Global %d, local %d\n", global, local);
		printf("(Child) PTR : Global %p, local %p\n", &global, &local);

		local = 100;
		global = 200;

		printf("(Child) DATA: Global %d, local %d\n", global, local);
		printf("(Child) PTR : Global %p, local %p\n", &global, &local);
		raise(SIGSEGV);
		exit(5);
	} else {
		int child_status = -1;
		sleep(5);
		printf("(Parent) DATA: Global %d, local %d\n", global, local);
		printf("(Parent) PTR : Global %p, local %p\n", &global, &local);

		int pid_from_wait;
		if( (pid_from_wait = wait(&child_status)) != -1){
			printf("process %d was terminated\n", pid_from_wait);
		} else {
			printf("error while wait() of it's child %d\n", pid);
		}

		if(WIFEXITED(child_status)){
			printf("\nChild process %d was terminated OK\n", pid);
			printf("It's return status: %d\n", WEXITSTATUS(child_status));
		} else {
			printf("\nChild process %d has terminated NOT OK\n", pid);
		}

		if(WIFSIGNALED(child_status)){
			printf("Child process was terminated by signal %d\n", WTERMSIG(child_status));
		}
	}
}
