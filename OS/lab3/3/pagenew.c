#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdint.h>
#include <getopt.h>

#define GET_BIT(X,Y) (X & ((uint64_t)1 << Y)) >> Y
#define PAGE_INFO_SIZE 8

void show_maps(int pid){
	char path_buff[260];

	if(pid == -1){
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

void read_procmap_by_address(const int pid, unsigned long long vaddr){
	if(pid == -1){
		memcpy(path_buff, "/proc/self/pagemap\0", 19);
	} else {
		sprintf(path_buff, "/proc/%d/pagemap", atoi(optarg));
		pid = atoi(optarg);
	}	

	FILE* f = fopen(path, "rb");
	uint64_t offset = vaddr / getpagesize() * PAGE_INFO_SIZE;

   	if( fseek(f, offset, SEEK_SET) == -1 ){
      perror("Error while fseek() in /proc/PID/pagemap.\n");
      return;
   	}	

	uint64_t pageinfo;
   	fread(&pageinfo, PAGE_INFO_SIZE, 1, f);

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

void explore_all_memory(const int pid){
	if(pid == -1){
		memcpy(path_buff, "/proc/self/pagemap\0", 19);
	} else {
		sprintf(path_buff, "/proc/%d/pagemap", atoi(optarg));
		pid = atoi(optarg);
	}	
}

void print_help(){
	printf("This program helps to examine converting page's addresses from virtual to physical.\n");
	printf("Necessarily! Use --pid=PID to attach the proccess.\n");
	printf("-a: to explore all available pages.\n");
	printf("-h for help.\n");
}

int main(int argc, char** argv){
	if(argc != 2){
		printf("Uncorrect num of arguments. Use -h for help.\n");
		return 0;
	}

	int pid = -2; 
	const struct option long_options[4] = {
		{ "help", no_argument, NULL, 'h' },
		{ "pid", required_argument, NULL, 'p' },
		{ "all", no_argument, NULL, 'a' },
		{ NULL, 0, NULL, 0}
	};

	bool flag_explore_all_memory = false;
	int option_position = -1;
	char path_buff[260];
	while( (option_position = getopt_long(argc, argv, "ahp:", long_options, &option_position)) != -1){
		switch(option_position){
		case 'h': {
			print_help();
			return 0;
		}
		case 'p': {
			if(strcmp(optarg, "self") == 0){
				memcpy(path_buff, "/proc/self/pagemap\0", 19);
			} else {
				pid = atoi(optarg);
				sprintf(path_buff, "/proc/%d/pagemap", pid);
			}	
			break;
		}
		case 'a':{
			flag_explore_all_memory = true;
			break;
		}
	}
	}

	if(pid != -2){
		if(!flag_explore_all_memory){
			show_maps(pid);

			char buffer[512];
			printf("Which address you'd like to examine?: 0x");
			scanf("%s", &buffer);

			unsigned long long virt_addr = strtol(buffer, NULL, 16);
			read_procmap_by_address(pid, virt_addr);

		} else {
			explore_all_memory(pid);
		}
	} else {
		printf("No pid entered. Use -h (--help) for help.\n");
	}

	return 0;
}
