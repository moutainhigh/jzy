package com.kaisa.kams.components.job;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by wangqx on 2017/8/14.
 */
public class DelayTask<T extends Runnable> implements Delayed {

    /* 唯一id */
    private final String id;
    /* 到期时间（纳秒） */
    private final long time;
    /* 任务对象 */
    private final T task;
    /* 初始化大小 */
    private static final AtomicLong rawValue = new AtomicLong(0L);
    /* 队列大小 */
    private final long position;

    public DelayTask(long timeOut, T task, String id) {
        this.task = task;
        this.time = System.nanoTime() + timeOut;
        this.position = rawValue.getAndIncrement();
        this.id = id;
    }

    public DelayTask(String id) {
        this.time = 0L;
        task = null;
        position = 0;
        this.id = id;
    }

    /**
     * 返回与此对象相关的剩余延迟时间，以给定的时间单位表示（纳秒）
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.time - System.nanoTime(), unit.NANOSECONDS);
    }

    /**
     * 比较方法 确定优先取出的顺序
     */
    @Override
    public int compareTo(Delayed other) {
        if (this == other) {
            return 0;
        }
        if (other instanceof DelayTask) {
            DelayTask otherTask = (DelayTask) other;
            long diff = this.time - otherTask.time;
            if (diff < 0) {
                return -1;
            }
            if (diff > 0) {
                return 1;
            }
            if (this.position < otherTask.position) {
                return -1;
            }
            return 1;
        }
        long remainTime = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
        if (remainTime == 0) {
            return 0;
        }
        if (remainTime > 0) {
            return 1;
        }
        return -1;
    }

    public T getTask() {
        return this.task;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DelayTask) {
            DelayTask delayTask = (DelayTask) object;
            return getId().equals(delayTask.getId());
        }
        return false;
    }

}
