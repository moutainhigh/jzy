package com.kaisa.kams.components.job;

import com.kaisa.kams.components.service.LoanRepayService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sunwanchao on 2017/1/16.
 */
@IocBean
public class OverdueLoanSchedule implements Job{

    private static final Logger LOGGER = LoggerFactory.getLogger(OverdueLoanSchedule.class);

    @Inject
    private LoanRepayService loanRepayService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        loanRepayService.handleOverdue();
    }
}
