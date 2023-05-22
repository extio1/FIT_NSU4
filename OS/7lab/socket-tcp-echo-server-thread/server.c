#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <pthread.h>
#include <netinet/ip.h>
#include <netinet/udp.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>

#define PORT 3500
#define MAX_MESS_LEN 1000
#define MAX_CLIENTS 10

int srvsock;
int clt_counter;

void* terminator(void* in){
	printf("To terminate server print q\n");

	while(fgetc(stdin) != 'q'){}

	close(srvsock);
	exit(0);
}

void* handler(void* sockfd_ptr){
	int sockfd = * (int*)sockfd_ptr;

	char buffer[MAX_MESS_LEN];

	while(1){
		size_t len = 0;
		if((len=read(sockfd, buffer, MAX_MESS_LEN)) < 0){
			printf("read error: %s\n", strerror(errno));
			continue;
		}

		if(write(sockfd, buffer, len) == -1){
			printf("write error: %s\n", strerror(errno));
			continue;
		}
	}
}

int main(){
	if( (srvsock = socket(AF_INET, SOCK_STREAM, 0)) == -1 ){
		perror("socket() error");
		exit(-1);
	}

	pthread_t tid;
	if( pthread_create(&tid, NULL, terminator, NULL) == -1 ){
		perror("thread create error\n");
		exit(-1);
	}

	struct sockaddr_in srv, clt;
	srv.sin_family = AF_INET;
	srv.sin_port = htons(PORT);
	srv.sin_addr.s_addr = INADDR_ANY;
	memset(srv.sin_zero, 0, 8);

	socklen_t len = sizeof(struct sockaddr_in);
	if( bind(srvsock, (struct sockaddr*) &srv, len) == -1 ){
		perror("bind error");
		exit(-1);
	}

	if( listen(srvsock, 5) == -1 ){
		perror("listen() error");
		exit(-1);
	}

	printf("Server starts at %d port, ready to serve %d\n", ntohs(srv.sin_port), ntohl(srv.sin_addr.s_addr));

	while(1){
		int cltsock;
		if( (cltsock = accept(srvsock, (struct sockaddr*)&srv, &len)) == -1){
			perror("accept() error");
			continue;
		} else {
			pthread_t tid;
			if( pthread_create(&tid, NULL, handler, &cltsock) == -1){
				perror("thread create error");
				continue;
			}
		}
	}
}
