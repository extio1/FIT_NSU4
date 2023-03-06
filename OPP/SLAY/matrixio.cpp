#include "matrixio.h"
#include <string>
#include <fstream>
#include <iostream>
using namespace std;

void entryMatrix(double* mat, const int dimension, const char* filePath) {
	string strmat;
	ifstream file(filePath);
	for (int i = 0; i < dimension; i++) {
		getline(file, strmat);
		int k = 0;
		int begin = 0;
		int length = 0;
		for (int j = 0; j < strmat.size(); j++) {
			if (strmat[j] == ' ' || j == strmat.size() - 1) {
				mat[i * dimension + k] = atof((strmat.substr(begin, length)).c_str());
				begin = j + 1;
				length = 1;
				k++;
			}
			else {
				length++;
			}
		}
	}
}

void entryLine(double* line, const int dimension, const char* filePath) {
	string strmat;
	ifstream file(filePath);
	getline(file, strmat);
	int k = 0;
	int begin = 0;
	int length = 0;
	for (int j = 0; j < strmat.size(); j++) {
		if (strmat[j] == ' ' || j == strmat.size() - 1) {
			line[k] = atof((strmat.substr(begin, length)).c_str());
			begin = j + 1;
			length = 1;
			k++;
		}
		else {
			length++;
		}
	}
}


void printMatrix(const double* arr, const int dim) {
	for (int i = 0; i < dim; ++i) {
		for (int j = 0; j < dim; ++j) {
			std::cout << arr[i * dim + j] << ' ';
		}
		std::cout << '\n';
	}
}

void printLine(const double* arr, const int dim) {
	for (int i = 0; i < dim; ++i) {
		std::cout << arr[i] << ' ';
	}
	std::cout << '\n';
}

void writeBinary(const double* arr, const int size){
	FILE* output = fopen("output.bin", "wb");
	if(output == NULL){
		printf("Error while creating file with result");
		writeBinary(arr, size);
	}

	int written = fwrite(&arr, sizeof(double), size, output);
	if(written != size){
		printf("ERROR: written %d of %d", written, size);
	} else {
		printf("output.bin contains actual version of answer vector.\n");
	}
}
