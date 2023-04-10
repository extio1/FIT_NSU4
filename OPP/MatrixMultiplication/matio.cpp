#include "matio.h"
#include <stdio.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <string>

void enter_matrix(double* data, const int xDim, const int yDim, const char* path) {
	std::string line;

	std::ifstream in(path);
	if (in.is_open())
	{
		while (getline(in, line))
		{
			char* buff = const_cast<char*>(line.c_str());
			char* tmp_char;
			tmp_char = strtok(buff, " ");    // вызовем функцию strtok для разделения строки в buff по пробелам
			while (tmp_char != NULL) {    ///выводим части пока они существуют
				std::cout << tmp_char << "\n";   ///выведем часть на экран
				tmp_char = strtok(NULL, " ");    ///получим следующую часть
			}
		}
	}
	in.close();		// закрываем файл
}
