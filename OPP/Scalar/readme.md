Есть два массива arr1, arr2 длины LENGTH, нужно вычислить значение ANSWER, получаемое следующим образом:<br/>

for(int i = 0; i < LENGTH; ++i){<br/>
  for(int j = 0; j < LENGTH; ++j){<br/>
    ANSWER += arr(i) * arr(j)<br/>
  }<br/>
}<br/>

Язык - C.
Использовалась библиотека MPI для параллельных вычислений.

* mainRow.c Одним процессом считает ANSWER

* mainParallelPTP.c Пораждает n процессов. 0 процесс является координирующим, делит массив 1 на порции, 1..n процессы считают части ответа и отдают их 0-му, он складывает и получает ANSWER 

* mainParallelGroup.c В общем соответсвует логике предыдушей реализации, но использовались команды MPI относящиеся к группам процессов.

Результаты замеров для LENGTH = 150'000:<br/>
  1 - 44.199898<br/>
  2 - 15.412100<br/>
  3 - 11.304995<br/>
