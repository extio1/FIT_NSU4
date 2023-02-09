#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

#define NOT_STATED -1
#define LENGTH 100000

struct arrsData{
	double* left;
	double* right;
	int lenLeft;
};

//Определить размер фрагмента одинакового для всех процессов
/*
int defineSegmentSize(int sizeArr, int amProc){
	if(sizeArr % amProc == 0){
		return sizeArr / amProc;
	} else {
		int newSegmentSize = sizeArr / amProc;
		while (sizeArr % newSegmentSize != 0){
			++newSegmentSize;
		}
		return newSegmentSize;
	}
}
*/
int main(int argc, char** argv){
	int rank = NOT_STATED;
	int size = NOT_STATED;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank); 

	//	Определяем новый тип данных для передачи процессам
	// информации о массивах, над которыми они должны работать 
	MPI_Datatype arrsDataType; // Идентификатор для нового типа
	// Список использованных типов внутри нового
	MPI_Datatype arrsDataUsedTypes[3] = {MPI_DOUBLE, MPI_DOUBLE, MPI_INT}; 
	int blockLen[3] = {1, 1, 1}; // Число использованных блоков 
							   // (на случай наличия поля-массива нужно указать его длину)

	MPI_Aint offset[3] = {8, 16, 20}; //тип MPI_Aint хранит адреса (по сути оболочка над указателем)
						//тут он используется для хранения отсупов
	//offset[0] = 8;
	//offset[1] = 16;
	//offset[2] = 20;

	MPI_Type_create_struct(3, blockLen, offset, arrsDataUsedTypes, &arrsDataType);
	int check = MPI_Type_commit(&arrsDataType); //"регистрируем" тип, теперь его можно использовать
	if(check != MPI_SUCCESS){
		printf("An error occurred in process #%d while creating the new type.\n", rank);
		exit(1);
	}


	if(rank == 0){
		struct arrsData* message = (struct arrsData*) malloc(sizeof(struct arrsData) * size);
		double arr1[LENGTH];
		double arr2[LENGTH];
/* //ОТПРАВИТЬ АСИНХРОННО СООБЩЕНИЯ 1-n ПРОЦЕССАМ, ПОСЛЕ ЦИКЛА ПРОВЕРИТЬ, ЧТО ВСЕ ДОШЛО, ЕСЛИ НЕТ, ВЫДАТЬ ОШИБКУ
   //ПОТОМ ЖДАТЬ ПОСЫЛКИ ОТ 1-n ПРОЦЕССОВ, ПО ПОЛУЧЕНИЮ СУММИРОВАТЬ ОТВЕТ
		for(int i = 1; i < size+1; i++){
			MPI_Send(message, 1, )
		}*/

		free(message);
	} else {
		printf("I'm %d of %d \n", rank, size);
	}

//double MPI_Wtime (void)


/*
	long long outputValue = 0;
    for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                    outputValue += arr1[i] * arr2[j];
            }
    }*/


	check = MPI_Finalize();
	if(check == MPI_ERRORS_RETURN){
		printf("Error occurred in %d process",  rank);
	}
}
