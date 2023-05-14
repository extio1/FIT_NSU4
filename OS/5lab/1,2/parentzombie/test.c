#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>

int main(){
	pid_t pid = fork();

	if(pid == 0){
		sleep(10);
		exit(5);
	} else {
		exit(2);
	}
}
