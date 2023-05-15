#include <stdlib.h>
#include <stdio.h>
#include <string.h>

void func(){
	char* buffer = malloc(100);
	memcpy(buffer, "hello world <placeholder>\0", 26);
	printf("V : buffer [%s]\n\n", buffer);

	printf("--: Buffer ptr have been released\n");
	free(buffer);
	printf("V : buffer [%s]\n", buffer);
	printf("Some chars from buffer(int): %d %d %d \n\n", buffer[0], buffer[1], buffer[3]);

	const size_t SIZE_OF_ANOTHER_BUFFER = 12;
	printf("--: another_buffer have been created\n");
	char* another_buffer = malloc(100);
	printf("--: [goodbye war\\0] (%ld) was assigned to another buffer\n", SIZE_OF_ANOTHER_BUFFER);
	memcpy(another_buffer, "goodbye void\0", SIZE_OF_ANOTHER_BUFFER);

	printf("P : another_buffer %p\n", &another_buffer);
	printf("P : buffer %p\n", &buffer);
	printf("\n");
	printf("V : buffer - [%s]\n", buffer);
	printf("V : another_buffer - [%s]\n", another_buffer);
	printf("\n");

	printf("-- : ptr from buffer[0] is shifted to buffer[sizeof(buffer)/2]\n");
	printf("=====================%p\n", another_buffer);
	another_buffer += SIZE_OF_ANOTHER_BUFFER / 2;
	//free(another_buffer); //causes abort()
	printf("P : another_buffer %p\n", another_buffer);
	printf("V : another_buffer %s\n", another_buffer);
}

int main(){
	func();
	return 0;
}
