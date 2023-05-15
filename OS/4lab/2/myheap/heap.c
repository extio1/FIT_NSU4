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

#define PAGE_SIZE 4096
#define ASGN_IFNN(dest, val) if(dest!=NULL) dest=val
#define ASGN_IFNN_TOFLD(dest, fld, val) if(dest!=NULL) dest->fld=val
#define ADD_BYTES(to, n) (char*)to+n
#define AFTER(thing) thing+sizeof(thing)
#define ADD_BYTES_AFTER(thing, n) (char*)(thing)+sizeof(*thing)+n

typedef struct node{
	uint64_t size;
	struct node* bigger;
	struct node* smaller;
	bool free;
} Node;

size_t NODE_DESC_SIZE = sizeof(struct node);
void* heap_start;

Node* first_node;
Node* last_node;

int init_heap(const size_t npages){
	int heapfd = open("heap_data", O_RDWR | O_CREAT, S_IRWXU | S_IRWXO | S_IRWXG);

	if( heapfd != -1 ){
		uint64_t heap_size = npages*PAGE_SIZE;

		if( lseek(heapfd, heap_size, SEEK_SET) == -1 ){
			perror("Error while establishing file size");
			return -1;
		}

		if( write(heapfd, "", 1) == -1 ) {
			perror("Error while establishing file size");
			return -1;
		}

		void** ptr_to_begin = mmap(NULL, heap_size, PROT_READ | PROT_WRITE,
					MAP_SHARED, heapfd, 0);

		if( ptr_to_begin == MAP_FAILED ){
			perror("Error while mmap()");
			return -1;
		}

		memset(ptr_to_begin, 0xCC, heap_size);
		heap_start = ptr_to_begin;
		first_node = (Node*) ptr_to_begin;

		//(*ptr_to_begin) = (void*) first_node;
		first_node->free=true;
		first_node->size = heap_size-sizeof(Node);
		first_node->smaller = NULL;
		first_node->bigger = NULL;

		return 0;
	} else {
		return -1;
	}

}

Node* create_new_node(const void* at, const size_t new_size,
		     Node* new_smaller, Node* new_bigger)
{
	Node* new_node = (Node*) at;
	new_node->size = new_size;
	new_node->bigger = new_bigger;
	new_node->smaller = new_smaller;
	new_node->free = true;

	return new_node;
}

//возвращает указатель на структуру, которая описывает свободный регион
Node* find_free_node(const size_t required_size){
	Node* node = first_node;
	while(node != NULL && (node->size < required_size || (node->free==false))){
		node = node->bigger;
	}
	return node;
}

void* my_malloc(const size_t size){
	Node* free_node = find_free_node(size);
	//printf("malloc %p new_node %p and %ld\n", free_node, ADD_BYTES_AFTER(free_node, size), sizeof(free_node));
	if(free_node != NULL){
		free_node->free = false;
		printf("Size %ld %ld %ld\n", free_node->size, size, NODE_DESC_SIZE);
		if((free_node->size-size) > NODE_DESC_SIZE){
			Node* new_node = create_new_node(ADD_BYTES_AFTER(free_node,size),
					 free_node->size-size-NODE_DESC_SIZE,
					 free_node, free_node->bigger);

			//printf("SIZE: %ld\n", new_node->size);
	//		(free_node->bigger)->smaller = new_node;
			free_node->bigger = new_node;
//	printf("Node created %p, prev %p, next %p", new_node, new_node->smaller, new_node->bigger);
		}
		free_node->size = size;
		memset((void*)free_node+NODE_DESC_SIZE, 0x11, size);
		return (void*)free_node+NODE_DESC_SIZE;
 	} else {
		return NULL;
	}
}

void my_free(void* ptr){
	Node* node = ptr-NODE_DESC_SIZE;
	node->free = true;
	Node* prev_node = node->smaller;
	if((prev_node != NULL) && (prev_node->free == true)){
		size_t s = prev_node->size;
		prev_node->size += node->size;
		prev_node->bigger = node->bigger;
		ASGN_IFNN_TOFLD(node->bigger, smaller, prev_node);
		memset(ptr-s, 0x00, prev_node->size);
	} else {
		memset(ptr, 0x00, node->size);
	}
}

void print_nodes(){
	Node* node = first_node;
	printf("-------------\n");
	do {
		printf("%p(%d) -> ", node, node->free);
		node = node->bigger;
	} while(node != NULL);
printf("\n--------------\n");
}

int main(){
	printf("pid: %d\n", getpid());
	printf("Code %d\n", init_heap(2));

	void* ptr = my_malloc(100);
	printf("ptr : This node %p\n", ptr-NODE_DESC_SIZE);

	void* ptr1 = my_malloc(100);
	printf("ptr1: This node %p\n", ptr1-NODE_DESC_SIZE);

	void* ptr2 = my_malloc(100);
	printf("ptr2: This node %p\n", ptr2-NODE_DESC_SIZE);

	void* ptr3 = my_malloc(100);
	printf("ptr3: This node %p\n", ptr3-NODE_DESC_SIZE);

	print_nodes();

	//my_free(ptr3);
	//printf("\nFree done ptr3\n");
	//print_nodes();
	my_free(ptr);
	printf("\nFree done ptr\n");
	print_nodes();

	void* ptr4 = my_malloc(100);
	printf("ptr4: This node %p\n", ptr4-NODE_DESC_SIZE);
	print_nodes();

	void* ptr5 = my_malloc(100);
	printf("This node %p, last node %p\n", ptr5-NODE_DESC_SIZE, last_node);
	print_nodes();

	my_free(ptr);
	print_nodes();

//	my_free(ptr1);
//	printf("last node %p prelast node %p\n", last_node, last_node->smaller);
	//print_nodes();

	//print_nodes();
	//printf("-------------first_node %p\n", first_node);
/*	//printf("ptr to new region %p\n", ptr_to_new_region);
	printf("Another ptr to new region %p\n", my_malloc(100));
	//printf("-------------first_node %p\n", first_node);
	print_nodes();
	printf("And another ptr to new region %p\n", my_malloc(100));
	print_nodes();
	//printf("-------------first_node %p\n", first_node);
	printf("freed\n");
	print_nodes();
	//printf("-------------first_node %p\n", first_node);
	my_free(ptr_to_new_region);
	//printf("-------------first_node %p\n", first_node);
	my_malloc(100);
	print_nodes();
	//printf("-------------first_node %p\n", first_node);


	printf("heap start %p\n", first_node);
	printf("size %ld\n", first_node->size);
	//sleep(20);
	//Free_node* node = *heap_begin;
	//printf("%p\n", node);
	//printf("%p\n", heapd->smallest->start);
*/
}
