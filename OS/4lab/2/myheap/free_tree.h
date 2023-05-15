#pragma once
#include <stdint.h>
#include <stdbool.h>

typedef struct node{
	uint64_t size;
	void* ptr_to_begin;
	struct node* left_child;
	struct node* right_child;
} Node;

typedef struct nodes_region_descriptor{
	bool inited;
	uint64_t size;
	Node* bump_pointer; // pointer to place where creatinig of the new node is possible
	Node* ptr_to_root;
} NodesDesc;

void init_tree(const uint64_t init_size_nodes, const uint64_t init_size_data, void* init_ptr_end, void* init_ptr_to_data);

void add_node(Node* new_node);
void remove_node_from_tree(Node* root, Node* node);

Node* create_node(const uint64_t size, void* ptr_to_data);
void reserve_node(Node* node, const uint64_t size);

Node* find_node(const uint64_t size);

void traverse_tree(Node* from);