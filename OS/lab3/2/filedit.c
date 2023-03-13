#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>

void makedir(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	if( mkdir(argv[1], 0777) == -1 ){
		printf("Error: syscall mkdir() error.\n");
		return;
	}
}

void showdir(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	DIR* dir;
	struct dirent *entry;
	if( (dir = opendir(argv[1])) == NULL){
		printf("Error: opendir() returned NULL. Directory %s is not opened.\n", argv[1]);
		return;
	}

	while ( (entry = readdir(dir)) != NULL) {
		printf("%s\n", entry->d_name);
    }

    closedir(dir);
}

void removedir(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	if( rmdir(argv[1]) == -1 ){
		printf("Error: syscall rmdir() error.\n");
		return;
	}
}

void createfile(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	int newfd = open(argv[1], S_IRWXU);
	if(newfd == -1){
		printf("File creationg error.\n");
	}
	utimensat(newfd, argv[1], NULL, 0); //set current time

	close(newfd);
}

void showfile(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	FILE* f = fopen(argv[1], "rb");
	char buff;
	while(!feof(f)){
		fread(&buff, 1, 1, f);
		printf("%c", buff);
	}

	fclose(f);
}

void removefile(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	if( unlink(argv[1]) == -1){
		printf("Error: syscall unlink() error while deliting %s\n", argv[1]);
	}
}

void linksoft(int argc, char** argv){
	if(argc != 3){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	if( symlink(argv[1], argv[2]) == -1){
		printf("Error: syscall syslink() error while creating sym link to %s as %s\n", argv[1], argv[2]);
	}
}

void readsoftlink(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	const int BUFFER_SIZE = 4096;
	char buffer[BUFFER_SIZE];
	if( readlink(argv[1], buffer, BUFFER_SIZE) == -1){
		printf("Error: syscall readlink() error while reading sym link %s", argv[1]);
	}
	printf("%s", buffer);
}

void readsoft(int argc, char** argv){
	showfile(argc, argv);
}

void deletesoftlink(int argc, char** argv){
	removefile(argc, argv);
}

void linkhard(int argc, char** argv){
	if(argc != 3){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	if( link(argv[1], argv[2]) == -1){
		printf("Error: syscall ink() error while creating hard link to %s as %s\n", argv[1], argv[2]);
	}
}

void deletehardlink(int argc, char** argv){
	removefile(argc, argv);
}

void showstat(int argc, char** argv){
	if(argc != 2){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	struct stat fstat;
	if( stat(argv[1], &fstat) == -1 ){
		printf("Error: syscall stat() error.\n");
	}

	printf("Permissions: %d; Hard links: %ld;\n", fstat.st_mode & 0777, fstat.st_nlink);
}

void changemode(int argc, char** argv){
	if(argc != 3){
		printf("Error: uncorrect num of args.\n");
		return;
	}

	struct stat fstat;
	if( chmod(argv[1], atoi(argv[2])) == -1 ){
		printf("Error: syscall chmod() error.\n");
	}
}


int main(int argc, char** argv){
	FILE* procinfo = fopen("/proc/self/status", "r");
	char* command;
	size_t len;
	getline(&command, &len, procinfo);
	command += 6;
	fclose(procinfo);

	if(strcmp(command, "makedir\n") == 0){
		makedir(argc, argv);
	} else if(strcmp(command, "showdir\n") == 0)	{
		showdir(argc, argv);
	} else if(strcmp(command, "removedir\n") == 0)	{
		removedir(argc, argv);
	} else if(strcmp(command, "createfile\n") == 0)	{
		createfile(argc, argv);
	} else if(strcmp(command, "showfile\n") == 0)	{
		showfile(argc, argv);
	} else if(strcmp(command, "removefile\n") == 0)	{
		removefile(argc, argv);
	} else if(strcmp(command, "linksoft\n") == 0)	{
		linksoft(argc, argv);
	} else if(strcmp(command, "readsoftlink\n") == 0)	{
		readsoftlink(argc, argv);
	} else if(strcmp(command, "readsoft\n") == 0)	{
		readsoft(argc, argv);
	} else if(strcmp(command, "deletesoftlink\n") == 0)	{
		deletesoftlink(argc, argv);
	} else if(strcmp(command, "linkhard\n") == 0)	{
		linkhard(argc, argv);
	} else if(strcmp(command, "deletehardlink\n") == 0)	{
		deletehardlink(argc, argv);
	} else if(strcmp(command, "showstat\n") == 0)	{
		showstat(argc, argv);
	} else if(strcmp(command, "changemode\n") == 0)	{
		changemode(argc, argv);
	} else {
		printf("Nothing to execute.\n");
	}

}
