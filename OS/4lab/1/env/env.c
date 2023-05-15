#include <stdlib.h>
#include <stdio.h>

void print_env(const char* name){
	char* value = getenv(name);
	if( value == NULL ){
		printf("getopt() no matches.\n");
	} else {
		printf("%s=%s\n", name, value);
	}

}

int main(){
	print_env("MY_ENV");
	if( setenv("MY_ENV", "123123", 1) == -1 ){
		printf("setenv() error.\n");
	}
	print_env("MY_ENV");

}
