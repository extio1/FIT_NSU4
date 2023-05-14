#include <unistd.h>
#include <stdio.h>

int main(int argc, char** argv){

	if(setuid(1020) != 0){
		perror("Error while changing uid to 1001\n");
	}

	uid_t real_uid = getuid();
	uid_t act_uid = geteuid();

	printf("Real user id: %d\nActual user id: %d\n", real_uid, act_uid);

	FILE* f = fopen("fileReadOnly", "r");
	if(f == NULL){
		perror("File <fileReadOnly> haven't opened\n");
	} else {
		printf("File <fileReadOnly> have just opened\n");
		fclose(f);
	}

	return 0;
}
