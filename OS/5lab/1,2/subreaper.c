#include <stdio.h>
#include <unistd.h>
#include <sys/prctl.h>
#include <stdlib.h>

int main(int argc, char** argv){

	if( prctl(PR_SET_CHILD_SUBREAPER) != -1){
		printf("subreaper launched: %s (pid: %d)\n", argv[0], getpid());

		pid_t pid = fork();

		if(pid == -1){
			perror("fork error()");
			exit(-1);
		} else {
			if(pid == 0){
				execv("./grandchzombie", argv);
			} else {
				sleep(30);
			}
		}

	} else {
		perror("prctl error:");
	}
}
