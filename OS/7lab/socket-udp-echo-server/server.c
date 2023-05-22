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

int sockfd;

void* terminator(void* in){
	printf("To terminate server print q\n");

	while(fgetc(stdin) != 'q'){}

	close(sockfd);
	exit(0);
}

int main(){
	if( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) == -1 ){
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
	if( bind(sockfd, (struct sockaddr*) &srv, len) == -1 ){
		perror("bind error");
		exit(0);
	}

	printf("Server starts at %d port, ready to serve %d\n", ntohs(srv.sin_port), ntohl(srv.sin_addr.s_addr));

	char buffer[MAX_MESS_LEN];
	socklen_t cltaddrlen = len;
	while(1){
		size_t len = 0;
		if((len=recvfrom(sockfd, buffer, MAX_MESS_LEN,
				MSG_WAITALL, (struct sockaddr*) &clt, &cltaddrlen)) < 0){
			printf("recvfrom error: %s\n", strerror(errno));
		}

		if(sendto(sockfd, buffer, len,
			 0, (struct sockaddr*) &clt, cltaddrlen) == -1){
			printf("sendto error: %s\n", strerror(errno));
		}
	}
}
