#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdint.h>

#define GET_BIT(X,Y) (X & ((uint64_t)1 << Y)) >> Y

void show_maps(char** argv){
	char path_buff[260];

	if(strcmp(argv[1], "self") == 0){
		memcpy(path_buff, "/proc/self/maps", 18);
	} else {
		sprintf(path_buff, "/proc/%d/maps", atoi(argv[1]));
	}

	FILE* f = fopen(path_buff, "rb");

	char sym;
	while(!feof(f)){
		fread(&sym, 1, 1, f);
		printf("%c", sym);
	}

	fclose(f);
}

void read_procmap(const char* path, unsigned long long vaddr){
	FILE* f = fopen(path, "rb");
	uint64_t offset = vaddr / getpagesize() * 8;

   	if( fseek(f, offset, SEEK_SET) == -1 ){
      perror("Error while fseek() in /proc/PID/pagemap.\n");
      return;
   	}	

	uint64_t pageinfo = 123;
   	fread(&pageinfo, 8, 1, f);

	//printf("=====%ld\n", pageinfo);
   	printf("Result: %llx\n", (unsigned long long) pageinfo);
   	printf("----------------------------------------------------\nYes - 1, No - 0\n");
   	printf("The page is present in RAM: %ld\n", GET_BIT(pageinfo,64));
   	printf("The page is in swap space: %ld\n", GET_BIT(pageinfo,63));
   	printf("The page is a file-mapped page or a shared anonymous page.: %ld\n", GET_BIT(pageinfo,61));
   	//printf("------------------------------------------------------");
   	printf("The page is exclusively mapped: %ld\n", GET_BIT(pageinfo,56));
   	printf("PTE is soft-dirty: %ld\n", GET_BIT(pageinfo,55));

	fclose(f);
}

int main(int argc, char** argv){
	if(argc != 2){
		printf("Uncorrect num of arguments.\n");
		return 0;
	}

	show_maps(argv);

	char buffer[512];
	printf("Which address you'd like to examine?: 0x");
	scanf("%s", &buffer);
	unsigned long long virt_addr = strtol(buffer, NULL, 16);


	char path_buff[260];
	if(strcmp(argv[1], "self") == 0){
		memcpy(path_buff, "/proc/self/pagemap\0", 19);
	} else {
		sprintf(path_buff, "/proc/%d/pagemap", atoi(argv[1]));
	}

	read_procmap(path_buff, virt_addr);

	return 0;
}
