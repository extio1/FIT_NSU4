package threadpool;

import factory.Factory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import static factory.Factory.fabricConditionDescriptor;

public class PoolExecutor implements Executor {
    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();
    private final Worker[] workers;

    public PoolExecutor(int nThreads){
        workers = new Worker[nThreads];
        for(int i = 0; i < nThreads; ++i) {
            workers[i] = new Worker();
            workers[i].setName("Worker #"+i);
            workers[i].start();
        }
    }

    @Override
    public void execute(Runnable command) {
        if(command == null){
            throw new NullPointerException();
        }

        try {
            tasks.put(command);
            Factory.fabricConditionDescriptor.incrementCarsInQueue();
        } catch (InterruptedException ignored) {
        }
    }

    public void quit(){
        Arrays.stream(workers).forEach(Thread::interrupt);
    }


    private class Worker extends Thread{
        @Override
        public void run(){
            while(!Thread.interrupted()){
                try {
                    tasks.take().run();
                    fabricConditionDescriptor.decrementCarsInQueue();
                    fabricConditionDescriptor.incrementCarProductionAmount();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

}
