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

#define PID_EQ_SELF -1
#define ALL_MAPS 0
#define SPECIAL_MAPS 1
#define SPECIAL_ADDR 2

void show_maps(int pid){
	char path_buff[260];

	if(pid == PID_EQ_SELF){
		memcpy(path_buff, "/proc/self/maps", 18);
	} else {
		sprintf(path_buff, "/proc/%d/maps", pid);
	}

	FILE* f = fopen(path_buff, "rb");

	char sym;
	while(!feof(f)){
		fread(&sym, 1, 1, f);
		printf("%c", sym);
	}

	fclose(f);
}

void print_pagemap_byte_info(const uint64_t pageinfo){
	printf("Result: %llx\n", (unsigned long long) pageinfo);
   	printf("----------------------------------------------------\nYes - 1, No - 0\n");
   	printf("The page is present in RAM: %ld\n", GET_BIT(pageinfo,63));
   	printf("The page is in swap space: %ld\n", GET_BIT(pageinfo,62));
   	printf("The page is a file-mapped page or a shared anonymous page.: %ld\n", GET_BIT(pageinfo,61));
   	printf("The page is exclusively mapped: %ld\n", GET_BIT(pageinfo,56));
   	printf("PTE is soft-dirty: %ld\n", GET_BIT(pageinfo,55));
   	printf("\n");
}

void read_pagemap_by_address_beginning_the_page(const char* path, unsigned long long vaddr){
	FILE* f = fopen(path, "rb");
	uint64_t offset = vaddr / getpagesize() * PAGE_INFO_SIZE;

   	if( fseek(f, offset, SEEK_SET) == -1 ){
      perror("Error while fseek() in /proc/PID/pagemap.\n");
      return;
   	}	

	uint64_t pageinfo;
   	fread(&pageinfo, PAGE_INFO_SIZE, 1, f);

   	print_pagemap_byte_info(pageinfo);

	fclose(f);
}

void explore_page_address_belongs_to(const char* path_buff, const unsigned long long virt_addr){
	unsigned long long virt_addr_beginning_the_page = virt_addr - (virt_addr % getpagesize());
	read_pagemap_by_address_beginning_the_page(path_buff, virt_addr);
}

void clear_softdirty_bit(const int pid){
	char path[256];
	if(pid == PID_EQ_SELF){
		memcpy(path, "/proc/self/clear_refs", 21);
	} else {
		sprintf(path, "/proc/%d/clear_refs", pid);
	}

	FILE* f = fopen(path, "w");
	fprintf(f, "%d", 4);
	fclose(f);
}

void read_all_pagemap_by_maps(const int pid){
	char maps_path[20];
	char procmap_path[30];
	if(pid == PID_EQ_SELF){
		memcpy(maps_path, "/proc/self/maps\0", 15);
		memcpy(procmap_path, "/proc/self/pagemap\0", 18);
	} else {
		sprintf(maps_path, "/proc/%d/maps", pid);
		sprintf(procmap_path, "/proc/%d/pagemap", pid);
	}

	
	static int var = 12312312;
	FILE* map = fopen(maps_path, "rb");
	FILE* proc = fopen(procmap_path, "rb");

	size_t size_buff = 1024;
	char* map_str = malloc(size_buff);
	char* delemiter;
	unsigned long long page_begin;
	uint64_t pageinfo;

	//clear_softdirty_bit(pid);

	while(getline(&map_str, &size_buff, map) != -1){
		page_begin = strtoll(map_str, NULL, 16);

		uint64_t offset = page_begin / getpagesize() * PAGE_INFO_SIZE;
	   	if( fseek(proc, offset, SEEK_SET) == -1 ){
	      perror("Error while fseek() in /proc/PID/pagemap.\n");
	      return;
	   	}
	   	fread(&pageinfo, PAGE_INFO_SIZE, 1, proc);
	   	printf("%s", map_str);
	   	print_pagemap_byte_info(pageinfo);
	}

	fclose(map);
	fclose(proc);
}

void print_help(){
	printf("This program helps to examine /proc/PID/pagemap file.\n");
	printf("Necessarily! Use --pid=PID to attach the proccess.\n");
	printf("-a: to explore all available pages from /proc/PID/maps.\n");
	printf("--addr=ADDRESS: to explore special ADDRESS (the page it's belongs to).\n");
	printf("-h for help.\n");
}

int main(int argc, char** argv){

	const struct option long_options[5] = {
		{ "help", no_argument, NULL, 'h' },
		{ "pid", required_argument, NULL, 'p' },
		{ "addr", required_argument, NULL, 'd' },
		{ "all", no_argument, NULL, 'a' },
		{ NULL, 0, NULL, 0}
	};

	int mode = SPECIAL_MAPS;
	int pid = -2; 
	int option_position = -1;
	unsigned long long special_addr = -1;
	char path_buff[260];

	while( (option_position = getopt_long(argc, argv, "ahpd:", long_options, &option_position)) != -1){
		switch(option_position){
		case 'h': {
			print_help();
			return 0;
		}
		case 'p': {
			if(strcmp(optarg, "self") == 0){
				pid = PID_EQ_SELF;
				memcpy(path_buff, "/proc/self/pagemap\0", 19);
			} else {
				pid = atoi(optarg);
				sprintf(path_buff, "/proc/%d/pagemap", pid);
			}	
			break;
		}
		case 'a':{
			mode = ALL_MAPS;
			break;
		}
		case 'd':{
			mode = SPECIAL_ADDR;
			special_addr = strtoll(optarg, NULL, 8);
			break;
		}
	}
	}

	if(pid != -2){ //if pid entered
		char buffer[512];
		switch(mode){
		case ALL_MAPS:{
			show_maps(pid);
			read_all_pagemap_by_maps(pid);
			break;
		}
		case SPECIAL_MAPS:{
			show_maps(pid);
			printf("Which address you'd like to examine?: 0x");
			scanf("%s", buffer);
			unsigned long long virt_addr = strtol(buffer, NULL, 16);
			read_pagemap_by_address_beginning_the_page(path_buff, virt_addr);
			break;
		}
		case SPECIAL_ADDR:{
			explore_page_address_belongs_to(path_buff, special_addr);
			break;
		}
	}

	} else {
		printf("No pid entered. Use -h (--help) for help.\n");
	}

	return 0;
}
