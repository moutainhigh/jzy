package com.kaisa.kams.components.utils.flow;

import com.kaisa.kams.components.job.ApproveWarnMessageThread;
import com.kaisa.kams.components.job.DelayTaskQueueDaemonThread;
import com.kaisa.kams.data.ApproveWarnMessageData;

import org.nutz.ioc.impl.PropertiesProxy;

/**
 * Created by wangqx on 2017/8/14.
 */
public class ApproveWarnMessageUtils {

    public final static PropertiesProxy property = new PropertiesProxy("/data0/java/config/custom/delay_task_conf.properties");
    public final static String APPROVE_WARN_MESSAGE_DELAY_MILLI_SECONDS = "approve.warn.message.delay.milli.seconds";
    private final static long DELAY_MILLI_SECONDS;
    private final static long ONE_MINUTE_MILLI_SECONDS = 60000L;
    public final static String  COMMON_PRODUCT_ID = "****************";
    public final static String  COMMON_PRODUCT_CODE = "********";
    static {
        if (property.keys().contains(APPROVE_WARN_MESSAGE_DELAY_MILLI_SECONDS)) {
            DELAY_MILLI_SECONDS = Long.valueOf(property.get(APPROVE_WARN_MESSAGE_DELAY_MILLI_SECONDS)).longValue();
        } else {
            //默认10分钟
            DELAY_MILLI_SECONDS = 600000L;
        }
    }

    public static long getDelayMinutes() {
        return DELAY_MILLI_SECONDS / ONE_MINUTE_MILLI_SECONDS;
    }

    public static void addTask(ApproveWarnMessageData data) {
        ApproveWarnMessageThread task = new ApproveWarnMessageThread(data);
        DelayTaskQueueDaemonThread.getInstance().add(DELAY_MILLI_SECONDS, task, data.getTaskId());
    }

    public static void removeTask(String taskId) {
        DelayTaskQueueDaemonThread.getInstance().removeTask(taskId);
    }
}
