package com.kaisa.kams.components.job;

import com.kaisa.kams.components.service.report.ComprehensiveDayReportService;
import com.kaisa.kams.components.utils.DateUtil;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by sunwanchao on 2017/1/16.
 */
@IocBean
public class ComprehensiveReportSchedule implements Job{

    private static final Logger LOGGER = LoggerFactory.getLogger(ComprehensiveReportSchedule.class);

    @Inject
    private ComprehensiveDayReportService comprehensiveDayReportService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOGGER.info("------------ comprehensiveDayReportService.handleGeneralComprehensiveReportByDay()--------------");
        String busiDate  = DateUtil.formatDateToString(DateUtil.getDateAfter(new Date(),-1));
        comprehensiveDayReportService.handleGeneralComprehensiveReportByDay(busiDate);
    }
}
