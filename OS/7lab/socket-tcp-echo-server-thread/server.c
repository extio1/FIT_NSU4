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
#include <poll.h>
#include <arpa/inet.h>

#define PORT 3500
#define MAX_MESS_LEN 1000
#define MAX_CLIENTS 10
#define TIMEOUT 5000

int srvsock;
int clt_counter;

void* terminator(void* in){
	printf("To terminate server print q\n");

	while(fgetc(stdin) != 'q'){}

	close(srvsock);
	exit(0);
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

	struct pollfd clients[MAX_CLIENTS];
	clients[0].fd = srvsock;
	clients[0].events = POLLIN;

	for(int i = 1; i < MAX_CLIENTS; ++i)
		clients[i].fd = -1;

	char buffer[MAX_MESS_LEN];
	while(1){
		int nready = poll(clients, MAX_CLIENTS, -1);

		if(nready == 0){
			printf("timeout\n");
			continue;
		} else if(nready == -1){
			perror("poll() error");
		}

		if(clients[0].revents & POLLIN){
			if(clients[0].revents & POLLERR){
				printf("Error on listenning socket\n");
			}
			int cltfd;
			if( (cltfd = accept(srvsock, (struct sockaddr*)&clt, &len)) == -1){
				printf("Error accepting new client\n");
			}

			printf("New client accepted on socket %d\naddr:%s\nport%d\n",
				cltfd, inet_ntoa(clt.sin_addr), ntohs(clt.sin_port));
			for(int i = 1; i < MAX_CLIENTS; ++i){
				if(clients[i].fd == -1){
					clients[i].fd = cltfd;
					clients[i].events = POLLIN;
					break;
				}
			}
		}

		for(int i = 1; i < MAX_CLIENTS; ++i){
			if(clients[i].fd != -1){
				if(clients[i].revents & POLLHUP){
					close(clients[i].fd);
					clients[i].fd = -1;
					printf("Client on %d desc hung up\n", clients[i].fd);
				} else if(clients[i].revents & POLLERR){
					printf("Error on %d desc client\n", clients[i].fd);
				} else if(clients[i].revents & POLLIN){
					int nread = read(clients[i].fd, buffer, MAX_MESS_LEN);
					if(nread != write(clients[i].fd, buffer, nread)){
						fprintf(stderr, "Writing to client on %d socker error:%s\n", clients[i].fd, strerror(errno));
					}
				}
			}
		}
	}
}
