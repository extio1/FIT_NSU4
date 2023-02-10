#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <sys/mman.h>
#include <sys/stat.h>        // For mode constants
#include <fcntl.h>           // For O_* constants

#define NOT_STATED -1 		 //коды ошибок не несут смысловой
#define UNCORRECT_ANSWER 100 //нагрузки, они просто отличны от нуля
#define RUNTIME_ERROR 200

#define LENGTH 100000

struct arrsData {
	double* left;
	double* right;
	int lenLeft;
};

//Распределяет лишние ячейки вектора по процессам
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

void createSharedMemory(const char* arr1Ind, const char* arr2Ind, int* shm1, int* shm2){

	if ( (shm1 = shm_open(arr1Ind, O_EXCL|O_CREAT|O_RDWR, 0777)) == -1 || (shm2 = shm_open(arr2Ind, O_EXCL|O_CREAT|O_RDWR, 0777)) == -1) {
    	printf("Object already opened.\n");
    	exit(RUNTIME_ERROR);
    }else{
    	printf("Done. Object created.\n");
    }

    if (ftruncate(shm1, LENGTH) == -1 || ftruncate(shm2, LENGTH) == -1){
    	printf("Error while setting the memory size.\n");
    	exit(RUNTIME_ERROR);
    }else{
    	printf("The memory size is set.\n");
    }

}

int main(int argc, char** argv) {
	int rank = NOT_STATED;
	int size = NOT_STATED;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &size);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	//Определяем новый тип данных для передачи процессам
	// информации о массивах, над которыми они должны работать 
	MPI_Datatype arrsDataType; // Идентификатор для нового типа
	// Список использованных типов внутри нового
	MPI_Datatype arrsDataUsedTypes[3] = { MPI_DOUBLE, MPI_DOUBLE, MPI_INT };
	int blockLen[3] = { 1, 1, 1 }; // Число использованных блоков 
	// (на случай наличия поля-массива нужно указать его длину)

	MPI_Aint offset[3] = { 8, 16, 20 }; //тип MPI_Aint хранит адреса (по сути оболочка над указателем)
	//тут он используется для хранения отсупов

	MPI_Type_create_struct(3, blockLen, offset, arrsDataUsedTypes, &arrsDataType); // вызывает [WARNING] yaksa: 1 leaked handle pool objects
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

		char memname1[] = "memory1";
		char memname2[] = "memory2";
		int discr1, discr2;
		createSharedObject(memname1, memname2, &discr1, &discr2);
		double* addr1; //указатели на массивы в разделяемой памяти
		double* addr2;
		const int sizeOfMem = LENGTH * sizeof(double);
		addr1 = mmap(0, sizeOfMem, PROT_WRITE|PROT_READ, MAP_SHARED, discr1, 0); //выделяем на объектах разделяемой памяти массивы doudlе-ов
		addr2 = mmap(0, sizeOfMem, PROT_WRITE|PROT_READ, MAP_SHARED, discr2, 0); //0 - позволяем системе самой определить откуда начинать распологать
														// данные. LENGTH - длина куска памяти. PROT_WRITE|PROT_READ - устанавливаем разрешения на чтения и запись
														// MAP_SHARED - указываем, что другие процессы могут с ней работать; 0 -смещение относительно начала


		memcpy(addr1, arr1, sizeOfMem); //записываем данные из массивов arr в 
		memcpy(addr2, arr2, sizeOfMem); //разделяемую память начиная с addr

		//double* arr1 = (double*) malloc(sizeof(double)*LENGTH);
		//double* arr2 = (double*) malloc(sizeof(double)*LENGTH);
		//ОТПРАВИТЬ АСИНХРОННО СООБЩЕНИЯ 1-n ПРОЦЕССАМ
		for (int i = 1; i < size; i++) {
			int proccesJobSize = defineSegmentSize(LENGTH, size);
			messange[i - 1].left = &arr1[(i-1) * proccesJobSize];
			messange[i - 1].right = arr2;
			messange[i - 1].lenLeft = proccesJobSize;
			MPI_Isend(&messange[i-1], 1, arrsDataType, i, 123, MPI_COMM_WORLD, &reqs[i-1]);
		}
		
		MPI_Status stat;
		double answerSum = 0;
		double incomeVal;

		for (int i = 0; i < size-1; ++i) {
			MPI_Recv(&incomeVal, 1, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &stat);
			if (stat.MPI_ERROR != MPI_SUCCESS) {
				printf("Error while sending messange from %d by %d tag\n", stat.MPI_SOURCE, stat.MPI_TAG);
			} 
			else {
				answerSum += incomeVal;
			}
		}

		printf("Answer is %f\n", answerSum);
		free(messange);
		//free(arr1);
		//free(arr2);
		shm_unlink(memname1);
		shm_unlink(memname2);
	/*} else {
		MPI_Status stat;
		arrsData inputData;
		double chunkOfAnswer = 0;
		//MPI_Recv(&inputData, 1, arrsDataType, 0, MPI_ANY_TAG, MPI_COMM_WORLD, &stat);
		/*double a = *inputData.left;
		
		for (double* iter = inputData.left; iter < inputData.left + inputData.lenLeft; ++iter) {
			for(int j = 0; j < LENGTH; ++j){
				chunkOfAnswer += *iter * (*(inputData.right + j)); 
			}
		}*//*
		printf("I'm %d and I'm ALIVE!\n", rank);
		//читаем указатели на нужные массивы, выполняем цикл
		//MPI_Send(&chunkOfAnswer, 1, MPI_DOUBLE, 0, 321, MPI_COMM_WORLD); // отсылает 0му, что насчитала
		printf("I'm %d of %d \n", rank, size);
		*/
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
	if (check == MPI_ERRORS_RETURN) {
		printf("Error occurred in %d process while MPI_Finalize()", rank);
	}
}
