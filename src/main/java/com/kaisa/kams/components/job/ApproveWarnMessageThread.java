package com.kaisa.kams.components.job;


import com.kaisa.MainSetup;
import com.kaisa.kams.components.service.flow.ApproveWarnMessageService;
import com.kaisa.kams.data.ApproveWarnMessageData;

import org.nutz.ioc.Ioc;

/**
 * Created by pengyueyang on 2017/8/14.
 * 业务审批短息线程任务
 */
public class ApproveWarnMessageThread implements Runnable{

    private final ApproveWarnMessageData data;

    public ApproveWarnMessageThread(ApproveWarnMessageData data) {
        this.data = data;
    }

    @Override
    public void run() {
        Ioc ioc = MainSetup.ioc;
        ApproveWarnMessageService approveWarnMessageService = ioc.get(ApproveWarnMessageService.class);
        approveWarnMessageService.processTask(data);
    }

    @Override
    public int hashCode() {
        return data.getTaskId().hashCode();
    }

}
