package com.kaisa.kams.components.job;

import com.kaisa.kams.components.service.PostLoanService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Created by lw on 2017/7/21.
 */
@IocBean
public class ExpiringMessageSchedule implements Job{

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpiringMessageSchedule.class);

    @Inject
    private PostLoanService postLoanService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("Start sendMessageForExpiring job at {}", LocalDate.now());

        postLoanService.sendMessageForExpiring();
        LOGGER.info("Finance sendMessageForExpiring job end at {}", LocalDate.now());
    }
}
