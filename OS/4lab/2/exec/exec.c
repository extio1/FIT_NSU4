#include <stdio.h>
#include <unistd.h>

int main(int argc, char** argv){
	pid_t pid = getpid();
	printf("PID: %d\n", pid);
	sleep(2);
	if( execv("./exec", argv) == -1){
		printf("exec(self) error.\n");
	}
	printf("HELLO WORLD!\n");
}
