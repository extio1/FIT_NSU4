#include "free_tree.h"
#include <stdio.h>
#include <string.h>

// nodes are stored from bigger addresses to smaller

NodesDesc* nd;

int counter=0;
void traverse_tree(Node* from){
	++counter;
	if(from != NULL){
		if(counter < 10){
			printf("----%p (%ld) %p %p (ptr:%p)\n", from, from->size, from->left_child, from->right_child, from->ptr_to_begin);
			traverse_tree(from->left_child);
			traverse_tree(from->right_child);
		} 
	}
}

void init_tree(const size_t init_size_nodes, const size_t init_size_data, void* init_ptr_end, void* init_ptr_to_data){
	nd = (NodesDesc*)(init_ptr_end-sizeof(NodesDesc));
	NodesDesc ndd;

	ndd.size = init_size_nodes - sizeof(NodesDesc) - sizeof(Node);
	ndd.bump_pointer = (Node*) (init_ptr_end - sizeof(NodesDesc) - sizeof(Node));
	ndd.ptr_to_root = ndd.bump_pointer;
	ndd.inited =  true;

	*nd = ndd;
	create_node(init_size_data, init_ptr_to_data);
}

void add_node(Node* new_node){
	if(nd->inited == true){
		Node* root = nd->ptr_to_root;
		if(root == NULL){
			nd->ptr_to_root = new_node;
		} else {
			Node* node = root;
			Node* parent_node = NULL;
			while(node != NULL){
				parent_node = node;
				if(new_node->size >= node->size){
					node = node->right_child;
				} else {
					node = node->left_child;
				}
			}
			
			if(new_node->size >= parent_node->size){
				parent_node->right_child = new_node;
			} else {
				parent_node->left_child = new_node;
			}
		}
	}
}

Node* find_node(const uint64_t size){
	if(nd->inited == true){
		Node* node = nd->ptr_to_root;
		while(node != NULL){
			if(node->size < size){
				node = node->right_child;
			} else if(node->size >= size && (node->left_child == NULL || node->left_child->size < size)){
				return node;
			} else {
				node = node->left_child;
			}
		}
	}
	return NULL;
}

Node* find_most_left_from(Node* node){
	while(node->left_child != NULL){
		node = node->left_child;
	}
	return node;
}

void remove_node_from_tree(Node* root, Node* deleting_node){
	if(nd->inited == true){
		Node* parent_node = NULL;
		Node* node = root;

		while(node != deleting_node){
			parent_node = node;
			if(deleting_node->size <= node->size){
				node = node->left_child;
			} else {
				node = node->right_child;
			}
		}


			if(deleting_node->left_child == NULL && deleting_node->right_child == NULL){
				if(parent_node != NULL){
					if(parent_node->left_child == deleting_node){
						parent_node->left_child = NULL;
					} else {
						parent_node->right_child = NULL;
					}
				} else {
					nd->ptr_to_root = NULL;
				}
			} else if (deleting_node->right_child == NULL){
				if(parent_node != NULL){
					if(parent_node->left_child == deleting_node){
							parent_node->left_child = deleting_node->left_child;
					} else {
							parent_node->right_child = deleting_node->left_child;
					}
				} else {
					nd->ptr_to_root = deleting_node->left_child;
				}
			} else if (deleting_node->left_child == NULL){
				if(parent_node != NULL){
					if(parent_node->left_child == deleting_node){
							parent_node->left_child = deleting_node->right_child;
					} else {
							parent_node->right_child = deleting_node->right_child;
					}
				} else {
					nd->ptr_to_root = deleting_node->right_child;
				}
			} else {
				if(parent_node != NULL){
					Node* most_left_from_right = find_most_left_from(deleting_node->right_child);
					if(parent_node->left_child == deleting_node){
							parent_node->left_child = most_left_from_right;
					} else {
							parent_node->right_child = most_left_from_right;
					}
					remove_node_from_tree(deleting_node->right_child, most_left_from_right);
					most_left_from_right->left_child = deleting_node->left_child;
					most_left_from_right->right_child = deleting_node->right_child;
				} else {
					Node* most_left_from_right = find_most_left_from(deleting_node->right_child);
					remove_node_from_tree(deleting_node->right_child, most_left_from_right);
					nd->ptr_to_root = most_left_from_right;
					most_left_from_right->left_child = deleting_node->left_child;
					most_left_from_right->right_child = deleting_node->right_child;
				}
			}
		}
}

Node* create_node(const uint64_t size, void* ptr_to_data){
	if(nd->inited == true){
		Node node;
		node.size = size-sizeof(Node*); 
		node.ptr_to_begin = ptr_to_data+sizeof(Node*);
		node.left_child = NULL;
		node.right_child = NULL;

		nd->size -= sizeof(Node);
		*(nd->bump_pointer) = node;
		return (nd->bump_pointer)--;
	}
	return NULL;
}

void reserve_node(Node* node, const uint64_t size){
	int size_remain = node->size-size;
	node->size = size;
	//printf("asdadasdasdasd\n");
	counter=0;
	//traverse_tree(nd->ptr_to_root);
	if(size_remain > sizeof(Node)){
		add_node(create_node(size_remain, node->ptr_to_begin+size));
	}

	//printf("asdadasdasdasd\n");
	counter=0;
	//traverse_tree(nd->ptr_to_root);

	//printf("asdadasdasdasd\n");
	counter=0;
	remove_node_from_tree(nd->ptr_to_root, node);
	//printf("%p %p\n\n",nd->ptr_to_root, node);
	//averse_tree(nd->ptr_to_root);
}
