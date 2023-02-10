#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

#define NOT_STATED -1
#define UNCORRECT_ANSWER 100
#define LENGTH 100000

struct arrsData {
	double* left;
	double* right;
	int lenLeft;
};

//Определить размер фрагмента одинакового для всех процессов
int defineSegmentSize(const int sizeArr, const int amProc){
	static int extraCells = sizeArr % amProc;
	const static int standartSize = sizeArr / amProc;

	if (extraCells == 0) {
		return standartSize;
	}
	else {
		--extraCells;
		return standartSize + 1;
	}
}

int main(int argc, char** argv) {
	int rank = NOT_STATED;
	int size = NOT_STATED;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	//	Определяем новый тип данных для передачи процессам
	// информации о массивах, над которыми они должны работать 
	MPI_Datatype arrsDataType; // Идентификатор для нового типа
	// Список использованных типов внутри нового
	MPI_Datatype arrsDataUsedTypes[3] = { MPI_DOUBLE, MPI_DOUBLE, MPI_INT };
	int blockLen[3] = { 1, 1, 1 }; // Число использованных блоков 
	// (на случай наличия поля-массива нужно указать его длину)

	MPI_Aint offset[3] = { 8, 16, 20 }; //тип MPI_Aint хранит адреса (по сути оболочка над указателем)
	//тут он используется для хранения отсупов
//offset[0] = 8;
//offset[1] = 16;
//offset[2] = 20;

	MPI_Type_create_struct(3, blockLen, offset, arrsDataUsedTypes, &arrsDataType);
	int check = MPI_Type_commit(&arrsDataType); //"регистрируем" тип, теперь его можно использовать
	if (check != MPI_SUCCESS) {
		printf("An error occurred in process #%d while creating the new type.\n", rank);
		exit(1);
	}


	if (rank == 0) {
		MPI_Request reqs[LENGTH];
		struct arrsData* messange = (struct arrsData*)malloc(sizeof(struct arrsData) * size);
		double arr1[LENGTH];
		double arr2[LENGTH];
		//ОТПРАВИТЬ АСИНХРОННО СООБЩЕНИЯ 1-n ПРОЦЕССАМ, ПОСЛЕ ЦИКЛА ПРОВЕРИТЬ, ЧТО ВСЕ ДОШЛО, ЕСЛИ НЕТ, ВЫДАТЬ ОШИБКУ
		//ПОТОМ ЖДАТЬ ПОСЫЛКИ ОТ 1-n ПРОЦЕССОВ, ПО ПОЛУЧЕНИЮ СУММИРОВАТЬ ОТВЕТ

		for (int i = 1; i < size; i++) {
			int proccesJobSize = defineSegmentSize(LENGTH, size);
			messange[i - 1].left = &arr1[(i-1) * proccesJobSize];
			messange[i - 1].right = arr2;
			messange[i - 1].lenLeft = proccesJobSize;
			MPI_Isend(&messange[i-1], 1, arrsDataType, i, 123, MPI_COMM_WORLD, &reqs[i-1]);
		}
		
		MPI_Status stat;
		int counterInputPr = 0;
		double answerSum = 0;
		double incomeVal;
		for (int i = 0; i < size; ++i) {
			MPI_Recv(&incomeVal, 1, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &stat);
			if (stat.MPI_ERROR != MPI_SUCCESS) {
				printf("Error while sending messange from %d by %d tag\n", stat.MPI_SOURCE, stat.MPI_TAG);
			} 
			else {
				answerSum += incomeVal;
				//++counterInputPr;
			}
		}
		printf("I'm 0 and I'm DONE!\n\n");
		free(messange);		
	} /*else {
		MPI_Status stat;
		arrsData inputData;
		double chunkOfAnswer = 0;
		MPI_Recv(&inputData, 1, arrsDataType, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &stat);
		for (double* iter = inputData.left; iter < inputData.left + inputData.lenLeft; ++iter) {
			for(int j = 0; j < LENGTH; ++j){
				chunkOfAnswer += *iter * *(inputData.right + j); 
			}
		}
		//читаем указатели на нужные массивы, выполняем цикл
		MPI_Send(&chunkOfAnswer, 1, MPI_DOUBLE, 0, MPI_ANY_TAG, MPI_COMM_WORLD); // отсылает 0му, что насчитала
		printf("I'm %d of %d \n", rank, size);
	}*/

	//double MPI_Wtime (void)


	/*
		long long outputValue = 0;
		for (int i = 0; i < len; i++) {
				for (int j = 0; j < len; j++) {
						outputValue += arr1[i] * arr2[j];
				}
		}*/


	check = MPI_Finalize();
	if (check == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process", rank);
	}
}
