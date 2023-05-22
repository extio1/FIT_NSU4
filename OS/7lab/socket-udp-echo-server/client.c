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
#include <arpa/inet.h>

#define PORT 3500
#define MAX_MESS_LEN 1000

int sockfd;

int main(){
	if( (sockfd = socket(AF_INET, SOCK_DGRAM, 0)) == -1 ){
		perror("socket() error");
		exit(-1);
	}

	struct sockaddr_in srvaddr;
	srvaddr.sin_family = AF_INET;
	srvaddr.sin_port = htons(PORT);
	srvaddr.sin_addr.s_addr = inet_addr("127.0.0.1");
//	inet_aton("127.0.0.1", &srvaddr.sin_addr.s_addr);
	memset(srvaddr.sin_zero, 0, 8);

	socklen_t len = sizeof(struct sockaddr_in);
/*
	if( bind(sockfd, (struct sockaddr*) &clt, len) == -1 ){
		perror("bind error");
		exit(0);
	}
*/
	printf("Client use to send %d port, ip %d\n", ntohs(srvaddr.sin_port), ntohl(srvaddr.sin_addr.s_addr));

	printf("Enter message (max %d symbols). To exit print 'q'.\n", MAX_MESS_LEN);
	size_t max_len = MAX_MESS_LEN;
	char* buffer = malloc(MAX_MESS_LEN);
	while(1){
		size_t readn = 0;

		if( (readn = getline(&buffer, &max_len, stdin)) < 0){
			perror("stdin input error");
			break;
		}

		if(strcmp(buffer, "q\n") == 0){
			break;
		}

		if(sendto(sockfd, buffer, readn,
			 0, (struct sockaddr*) &srvaddr, len) == -1)
		{
			printf("sendto error: %s\n", strerror(errno));
			continue;
		}

		if((recvfrom(sockfd, buffer, MAX_MESS_LEN,
				0, (struct sockaddr*) &srvaddr, (socklen_t*) &len)) < 0)
		{
			printf("recvfrom error: %s\n", strerror(errno));
			continue;
		}

		printf("Server response: %s\n", buffer);
	}

	close(sockfd);
}

