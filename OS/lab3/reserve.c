#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <dirent.h>
#include <string.h>
#include <stdbool.h>

struct reserve_data_paths{
	char* path;
	char* reservedPath;
	char* fileName;
	size_t pathLen;
	size_t nameLen;
};

void swap(char* c1, char* c2){
	char temp;
	temp = *c1;
	(*c1) = (*c2);
	(*c2) = temp;
}

void reserveStr(char* str, const int len){
	for(int i = 0; i < len/2; i++){
		swap(&str[i], &str[len-1-i]);
	}
}

char* concatPaths(const char* s1, const char* s2, const int lens1, const int lens2){
	char* concated = malloc(sizeof(char) * (lens1+lens2+1));

	memcpy(concated, s1, lens1);

	if(s1[lens1-1] != '/'){
		concated[lens1] = '/';
		memcpy(concated+lens1+1, s2, lens2+1);
	} else {
		memcpy(concated+lens1, s2, lens2+1);
	}

	return concated;
}

int reserveRegularFile(struct reserve_data_paths* data){
	FILE* fileIn,* fileOut;

	char* inPath = concatPaths(data->path, data->fileName, data->pathLen, data->nameLen);
	if( (fileIn = fopen(inPath, "rb")) == NULL){
		return -1;
	}

	reserveStr(data->fileName, data->nameLen);
	char* outPath = concatPaths(data->reservedPath, data->fileName, data->pathLen, data->nameLen);
	printf("%s\n", outPath);
	if( (fileOut = fopen(outPath, "wb")) == NULL){
		return -1;
	}
	

	fseek(fileIn, 0, SEEK_END);

	char n;
	fread(&n, 1, 1, fileIn);
	fwrite(&n, 1, 1, fileOut); 

	free(inPath);
	free(outPath);
	fclose(fileIn);
	fclose(fileOut);

	return 0;
}

void copy_reserve_regfiles(struct reserve_data_paths* data){

	DIR* dir;
	struct dirent *entry;
	if( (dir = opendir(data->path)) == NULL){
		printf("Error: opendir() returned NULL.\n");
	}

	while ( (entry = readdir(dir)) != NULL) {
     	if(entry->d_type ==  DT_REG){
     		data->fileName = entry->d_name;
     		if(reserveRegularFile(data) == -1){
     			printf("Error while reserving %s\n", entry->d_name);
     		}
     	} 
    }

	closedir(dir);
}

int mkdirres(char** argv){

	unsigned short beginName = 0;
	unsigned short lengthName = 0;
	unsigned long lengthPath = 0;
	for (int i = 0; argv[1][i] != '\0'; ++i){
		++lengthName;
		++lengthPath;
		if(argv[1][i] == '/'){
			lengthName = 0;
			beginName = i+1;
		}
	}

	char* reservedName = malloc(sizeof(char) * lengthPath);

	for (int i = 0; i < beginName; ++i){
		reservedName[i] = argv[1][i];
	}

	for (int i = 0; i < lengthName; ++i){
		reservedName[beginName+i] = argv[1][beginName+lengthName-i-1];
	}

	printf("%s\n", reservedName);

	mkdir(reservedName, 0777);

	struct reserve_data_paths data;
	data.path = argv[1];
	data.reservedPath = reservedName;
	data.pathLen = lengthPath;
	data.nameLen = lengthName;
	
	copy_reserve_regfiles(&data);

	free(reservedName);

	return 0;
}

int main(int argc, char** argv){
	if(argc != 2){
		printf("Error: Uncorrect num of args.\n");
		exit(1);
	}

	struct stat dirstat;
	if( stat(argv[1], &dirstat) == -1 ){
		printf("Error: syscall stat() error.\n");
		exit(1);
	}

	if( (dirstat.st_mode & S_IFMT) != S_IFDIR ){
		printf("Error: entered path is not referred to some directory.\n");
		exit(1);
	}

	mkdirres(argv);

	return 0;
}
