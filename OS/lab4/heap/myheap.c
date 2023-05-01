#include <stdio.h>
#include <sys/mman.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdbool.h>

#include "free_tree.h"

#define PAGE_SIZE getpagesize()
#define INIT_HEAP_DATA_SIZE PAGE_SIZE*2
#define INIT_HEAP_NODES_SIZE PAGE_SIZE

/*#define ASGN_IFNN(dest, val) if(dest!=NULL) dest=val
#define ASGN_IFNN_TOFLD(dest, fld, val) if(dest!=NULL) dest->fld=val
#define ADD_BYTES(to, n) (char*)to+n
#define AFTER(thing) thing+sizeof(thing)
#define ADD_BYTES_AFTER(thing, n) (char*)(thing)+sizeof(*thing)+n

size_t NODE_DESC_SIZE = sizeof(struct node);*/

typedef struct head_descriptor{
	void* start_heap_data;
	void* end_heap_nodes;
	size_t heap_size; 
} HeapD;

HeapD heapd;

int find_place_between_heap_and_other(){
	FILE* map = fopen("/proc/self/maps", "r");
	if(map == NULL){
		perror("Error while opening /proc/self/maps\n");
		return -1;
	}

	size_t size_buff = 1024;
	const int sizeof_ptr_in_maps = 12;
	char* map_str = malloc(sizeof(char)*size_buff);
	char* tmp_char = malloc(sizeof(char)*size_buff);
	char* tmp_char_stable = tmp_char;

	unsigned long long page_begin;
	unsigned long long page_end;

	int previos_was_heap = 0;
	while(getline(&map_str, &size_buff, map) != -1){
		page_begin = strtoll(map_str, NULL, 16);
		tmp_char = strtok(map_str, " ");
		page_begin = strtoll(tmp_char, NULL, 16);
		page_end = strtoll(tmp_char+sizeof_ptr_in_maps+1, NULL, 16);
		for(int i = 0; i < 5; ++i){
			tmp_char = strtok(NULL, " ");
		}

		if(previos_was_heap){
	   		heapd.end_heap_nodes = (void*) page_begin;
			previos_was_heap = 0;
		}	
		if(strcmp(tmp_char, "[heap]\n") == 0){
	   		heapd.start_heap_data = (void*) page_end;
	   		previos_was_heap = 1; 
		}
	}

	fclose(map);
	free(tmp_char_stable);
	free(map_str);
}

int init_heap(){
	int data_heapfd = open("heap_data",  O_RDWR | O_CREAT, S_IRWXU | S_IRWXO | S_IRWXG);
	int nodes_heapfd = open("heap_nodes", O_RDWR | O_CREAT, S_IRWXU | S_IRWXO | S_IRWXG);

	if( data_heapfd != -1 && nodes_heapfd != -1){
		if( find_place_between_heap_and_other() == -1 ){
			perror("Error while initializing heap: find_place_between_heap_and_other().\n");
			return 100;
		} 

		//establish file sizes
		if( lseek(data_heapfd, INIT_HEAP_DATA_SIZE, SEEK_SET) == -1 ){
			perror("Error while establishing file size");
			return 101;
		}
		if( write(data_heapfd, "", 1) == -1 ) {
			perror("Error while establishing file size");
			return 102;
		}
		if( lseek(nodes_heapfd, INIT_HEAP_NODES_SIZE, SEEK_SET) == -1 ){
			perror("Error while establishing file size");
			return 101;
		}
		if( write(nodes_heapfd, "", 1) == -1 ) {
			perror("Error while establishing file size");
			return 102;
		}

		void* data_begin = mmap(heapd.start_heap_data, INIT_HEAP_DATA_SIZE, PROT_READ | PROT_WRITE,
							    MAP_SHARED, data_heapfd, 0);

		void* nodes_begin = mmap(heapd.end_heap_nodes-INIT_HEAP_NODES_SIZE, INIT_HEAP_NODES_SIZE, 
							   PROT_READ | PROT_WRITE, MAP_SHARED, nodes_heapfd, 0);

		if( data_begin == MAP_FAILED || nodes_begin == MAP_FAILED){
			perror("Error while mmap()");
			return 200;
		}
		memset(heapd.start_heap_data, 0xFF, INIT_HEAP_DATA_SIZE);
		memset(heapd.end_heap_nodes-INIT_HEAP_NODES_SIZE, 0xFF, INIT_HEAP_NODES_SIZE);

		init_tree(INIT_HEAP_NODES_SIZE, INIT_HEAP_DATA_SIZE, heapd.end_heap_nodes, heapd.start_heap_data);

		return 0;
	} else {
		return 1000;
	}

}

void* my_malloc(const size_t size){
	Node* free_node = find_node(size);
	//Перед началом блока данных на куче записан указатель на соответсующей ему нод
	*((Node**)(free_node->ptr_to_begin-sizeof(Node*))) = free_node;
	reserve_node(free_node, size);
	return free_node->ptr_to_begin;
}

void my_free(void* ptr){
	Node** node = ptr - sizeof(Node*);
	(*node)->left_child = NULL;
	(*node)->right_child = NULL;
	add_node(*node);
	//printf("++++++++++%p - %d (%p, %p)\n", *node, (*node)->size, (*node)->left_child, (*node)->right_child);
	memset((char*)(node), 0x00, (*node)->size+sizeof(Node*));
}



int main(){
	init_heap();
/*	printf("pid: %d\n", getpid());
	printf("Code %d\n", init_heap());

	printf("%p\n", heapd.start_heap_data);
	printf("%p\n", heapd.end_heap_nodes);*/

	void* ptr1 = my_malloc(100);
	memset(ptr1, 0x77, 100);
	printf("Malloc1: %p\n", ptr1);
	void* ptr2 = my_malloc(100);
	memset(ptr2, 0x77, 100);
	printf("Malloc2: %p\n", ptr2);
	void* ptr3 = my_malloc(100);
	memset(ptr3, 0x77, 100);
	printf("Malloc3: %p\n", ptr3);

	my_free(ptr2);
	printf("free2\n");

	ptr2 = my_malloc(100);
	memset(ptr2, 0x77, 100);
	printf("Malloc2: %p\n", ptr2);

	void* ptr4 = my_malloc(100);
	memset(ptr4, 0x77, 100);
	printf("Malloc4: %p\n", ptr4);

	my_free(ptr3);
	printf("free3\n");

	ptr3 = my_malloc(100);
	memset(ptr3, 0x77, 100);
	printf("Malloc3: %p\n", ptr3);

}
