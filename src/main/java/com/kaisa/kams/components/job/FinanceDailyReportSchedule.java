package com.kaisa.kams.components.job;

import com.kaisa.kams.components.service.report.FinanceDailyReportService;
import com.kaisa.kams.components.utils.DateUtil;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Created by pengyueyang on 2017/1/16.
 */
@IocBean
public class FinanceDailyReportSchedule implements Job{

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceDailyReportSchedule.class);

    @Inject
    private FinanceDailyReportService financeDailyReportService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("Start finance daily report job at {}", LocalDate.now());
        String preDayDate = DateUtil.formatDateToString(DateUtil.getPreDaysDate(1));
        financeDailyReportService.executeDailyJob(preDayDate);
        LOGGER.info("Finance daily report job end at {}", LocalDate.now());
    }
}
