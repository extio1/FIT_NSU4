#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <dirent.h>
#include <string.h>
#include <stdbool.h>
#include "dirrev.h"

struct reverse_data_paths{
	const char* path;
	char* reversedPath;
	size_t pathLen;
};

void swap(char* c1, char* c2){
	char temp;
	temp = *c1;
	(*c1) = (*c2);
	(*c2) = temp;
}

void reverseName(char* str, const int len){
	int endPosName = 0;
	for(int i = 0; i < len; ++i){
		if(str[i] == '.'){
			break;
		}
		endPosName = i;
	}

	for(int i = 0; i < (endPosName)/2; ++i){
		swap(&str[i], &str[endPosName-i]);
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

int reverseRegularFile(struct reverse_data_paths* data, char* fileName){
	int nameLen = 0;

	for(int i = 0; fileName[i] != '\0'; ++i){
		++nameLen;
	}


	FILE* fileIn,* fileOut;

	char* inPath = concatPaths(data->path, fileName, data->pathLen, nameLen);
	if( (fileIn = fopen(inPath, "rb")) == NULL){
		printf("File %s have not opened.\n", inPath);
		return -1;
	}

	reverseName(fileName, nameLen);
	char* outPath = concatPaths(data->reversedPath, fileName, data->pathLen, nameLen);
	if( (fileOut = fopen(outPath, "wb")) == NULL){
		printf("File %s have not opened.\n", outPath);
		return -1;
	}
	

	fseek(fileIn, -1, SEEK_END);
	int buff = 0;
	for(int i = 2; ftell(fileIn) > 0; ++i){
		fread(&buff, 1, 1, fileIn);
		fseek(fileIn, -i, SEEK_END);
		fwrite(&buff, 1, 1, fileOut);
		buff = 0;
	} 
	fread(&buff, 1, 1, fileIn);
	fwrite(&buff, 1, 1, fileOut);

	free(inPath);
	free(outPath);
	fclose(fileIn);
	fclose(fileOut);

	return 0;
}

int copy_reverse_regfiles(struct reverse_data_paths* data){

	DIR* dir;
	struct dirent *entry;
	if( (dir = opendir(data->path)) == NULL){
		printf("Error: opendir() returned NULL. Directory %s is not opened.\n", data->path);
		return -1;
	}

	while ( (entry = readdir(dir)) != NULL) {
     	if(entry->d_type ==  DT_REG){
     		if(reverseRegularFile(data, entry->d_name) == -1){
     			printf("Error while reserving %s\n", entry->d_name);
     		}
     	} 
    }

	closedir(dir);

	return 0;
}

int mkdirrev(const char* path_to_directory){
	struct stat dirstat;
	if( stat(path_to_directory, &dirstat) == -1 ){
		printf("Error: syscall stat() error.\n");
		return -1;
	}

	if( (dirstat.st_mode & S_IFMT) != S_IFDIR ){
		printf("Error: entered path is not referred to some directory.\n");
		return -1;
	}

	unsigned short beginName = 0;
	unsigned short lengthDirName = 0;
	unsigned long lengthPath = 0;
	for (int i = 0; path_to_directory[i] != '\0'; ++i){
		++lengthDirName;
		++lengthPath;
		if(path_to_directory[i] == '/'){
			lengthDirName = 0;
			beginName = i+1;
		}
	}

	char* reversedName = malloc(sizeof(char) * lengthPath);

	for (int i = 0; i < beginName; ++i){
		reversedName[i] = path_to_directory[i];
	}

	for (int i = 0; i < lengthDirName; ++i){
		reversedName[beginName+i] = path_to_directory[beginName+lengthDirName-i-1];
	}

	mkdir(reversedName, 0777);

	struct reverse_data_paths data;
	data.path = path_to_directory;
	data.reversedPath = reversedName;
	data.pathLen = lengthPath;

	int ret = copy_reverse_regfiles(&data);

	free(reversedName);

	return ret;
}
