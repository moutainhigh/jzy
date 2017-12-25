package com.kaisa.kams.components.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by pengyueyang on 2017/8/14.
 * 任务调度守护线程
 */
public class DelayTaskQueueDaemonThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayTaskQueueDaemonThread.class);

    /* 固定线程池大小 */
    private final int FIXED_THREAD_POOL_SIZE = 20;

    /* 延时任务队列 */
    private DelayQueue<DelayTask> delayTaskDelayQueue = new DelayQueue<>();


    private DelayTaskQueueDaemonThread() {
    }

    private static class LazyHolder {
        private static DelayTaskQueueDaemonThread delayTaskQueuedaemonThread = new DelayTaskQueueDaemonThread();
    }

    public static DelayTaskQueueDaemonThread getInstance() {
        return LazyHolder.delayTaskQueuedaemonThread;
    }

    /* 线程执行者 */
    Executor executor = Executors.newFixedThreadPool(FIXED_THREAD_POOL_SIZE);
    /* 守护线程 */
    private Thread daemonThread;

    /**
     * 守护线程初始化
     */
    public void init() {
        daemonThread = new Thread(() -> execute());
        daemonThread.setDaemon(true);
        daemonThread.setName("Delay Task Queue Daemon Thread");
        daemonThread.start();
    }

    private void execute() {
        LOGGER.info("Start execute task");
        while (true) {
            try {
                //从延迟队列中取值,如果没有对象过期则队列一直等待，
                DelayTask task = delayTaskDelayQueue.take();
                if (task != null) {
                    //执行任务
                    Runnable runnable = task.getTask();
                    if (runnable == null) {
                        continue;
                    }
                    executor.execute(runnable);
                    LOGGER.info("[at task:" + task + "]   [Time:" + System.currentTimeMillis() + "]");
                }
            } catch (Exception e) {
                LOGGER.error("Execute task error: {}", e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * 添加任务
     * @param time 延时毫秒数
     * @param task 任务 实现Runnable即可
     */
    public void add(long time, Runnable task, String id) {
        //转换成ns
        long nanoSeconds = TimeUnit.NANOSECONDS.convert(time, TimeUnit.MILLISECONDS);
        //创建一个任务
        DelayTask delayTask= new DelayTask(nanoSeconds, task, id);
        //将任务放在延迟的队列中
        delayTaskDelayQueue.put(delayTask);
    }

    /**
     * 移除任务
     */
    public boolean removeTask(String id){
        DelayTask<Runnable> task = new DelayTask(id);
        return delayTaskDelayQueue.remove(task);
    }
}
