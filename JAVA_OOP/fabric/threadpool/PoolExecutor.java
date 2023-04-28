package threadpool;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Executor;

public class PoolExecutor implements Executor {
    private final Queue<Runnable> tasks = new ArrayDeque<>();
    private final Worker[] workers;

    public PoolExecutor(int nThreads){
        workers = new Worker[nThreads];
        for(int i = 0; i < nThreads; ++i)
            workers[i] = new Worker(workers);
    }

    @Override
    public void execute(Runnable command) {
        if(command == null){
            throw new NullPointerException();
        }

        tasks.add(command);
    }

    public void quit(){
        Arrays.stream(workers).forEach(Thread::interrupt);
    }

    private class Worker extends Thread{
        private final Object syncObj;
        public Worker(Object syncObj){
            this.syncObj = syncObj;
        }

        @Override
        public void run(){
            while(!Thread.interrupted()){
                try {
                    syncObj.wait();
                } catch (InterruptedException e) {
                    break;
                }

                synchronized (tasks) {
                    Objects.requireNonNull(tasks.poll()).run();
                }
            }
            System.out.println("Thread "+Thread.currentThread()+" interrupted");
        }
    }

}
