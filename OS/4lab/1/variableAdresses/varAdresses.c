#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>

int globalInit = 1234;
int globalUninit;
const int globalConst = 2345;

int* ptr_inside_func(){
	printf("-\n");
	int localStackVar = 404;
	printf("P : localStackVar: %p\n", &localStackVar);

	int* addr = &localStackVar;
	return addr;
}

int* ptr_inside_another_func(){
	printf("-\n");
	int anotherLocalStackVar = 200;
	printf("P : anotherLocalStackVar: %p\n", &anotherLocalStackVar);

	int* addr = &anotherLocalStackVar;
	return addr;
}

int** static_in_func(){
	printf("-\n");
	int** returnArr = malloc(sizeof(int*)*2);
	static int staticVar = 100;
	static int staticVarUninit;
	printf("P : static var inside func: %p \n", &staticVar);
	printf("P : static var inside func uninitialized: %p \n", &staticVarUninit);

	printf("V : static var 1 inside func: %d \n", staticVar);
	printf("V : static var 2 inside func: %d \n", staticVarUninit);

	returnArr[0] = &staticVar;
	returnArr[1] = &staticVarUninit;

	return returnArr;
}

int** const_in_func(){
	printf("-\n");
	int** returnArr = malloc(sizeof(int*)*2);
	const int constVar = 100;
	const int constVarUninit;
	printf("P : const var inside func: %p \n", &constVar);
	printf("P : const var inside func uninitialized: %p \n", &constVarUninit);

	printf("V : const var 1 inside func: %d \n", constVar);
	printf("V : const var 2 inside func: %d \n", constVarUninit);

	returnArr[0] = &constVar;
	returnArr[1] = &constVarUninit;
	return returnArr;
}

void clean_stack(){
	for(int i = 0; i < 50; ++i){
		int b = i + 100;
	}
}

int main(){

	pid_t pid = getpid();
	printf("PID: %d\n", pid);


	printf("LOCAL VARIABLES:\n\n");
	int* ptrToLocalVarFromFunc = ptr_inside_func();
	printf("  P : ptrToLocalVarFromFunc: %p \n", ptrToLocalVarFromFunc);
	printf("  V : value by ptr from 1 func: %d \n", *ptrToLocalVarFromFunc);
	int* anotherPtrToLocalVarFrFunc = ptr_inside_another_func();
	printf("  P : anotherPtrToLocalVarFrFunc: %p\n", anotherPtrToLocalVarFrFunc);
	printf("  V : value by ptr from 2 func: %d \n", *anotherPtrToLocalVarFrFunc);
	printf("  V : value by ptr from 1 func: %d \n", *ptrToLocalVarFromFunc);

	printf("\nSTATIC VARIABLES:\n\n");
	int** arr;
	arr = static_in_func();
	printf("  V : static var outside func %p \n", arr[0]);
	printf("  V : static var outside func uninitialized %p \n", arr[1]);
	*arr[0] = 1024;
	*arr[1] = 550;
	arr = static_in_func();

	printf("\nCONSTANTS FUNC:\n\n");
	arr = const_in_func();
	printf("  V : const var outside func %p \n", arr[0]);
	printf("  V : const var outside func uninitialized %p \n", arr[1]);
	*arr[0] = 1024; // assigning to const
	*arr[1] = 550;  //

	arr = const_in_func();
	printf("Here is the call of ptr_inside_func, it will assign to some ptr value 404\n");
	ptr_inside_func();
	arr = const_in_func();

	printf("\nGLOBAL:\n\n");
	printf("P : global init %p\n", &globalInit);
	printf("P : global uninit %p\n", &globalUninit);
	printf("P : global const %p\n", &globalConst);

	sleep(300);
}
