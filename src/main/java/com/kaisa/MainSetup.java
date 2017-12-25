package com.kaisa;


import com.kaisa.kams.components.job.*;
import org.nutz.dao.Dao;
import org.nutz.dao.util.Daos;
import org.nutz.integration.quartz.QuartzManager;
import org.nutz.ioc.Ioc;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;


/**
 * Created by dominic on 2016/11/9.
 */

public class MainSetup implements Setup {

    public static Ioc ioc;

    public void init(NutConfig conf) {
        MainSetup.ioc = conf.getIoc();
        Dao dao = ioc.get(Dao.class);
        Daos.createTablesInPackage(dao, "com.kaisa.kams.models", false);
        Daos.migration(dao, "com.kaisa.kams.models", true, false, true, true);
        QuartzManager manager = ioc.get(QuartzManager.class);
        setCrons(manager);
        DelayTaskQueueDaemonThread.getInstance().init();
    }

    public void destroy(NutConfig conf) {
    }

    private void setCrons(QuartzManager manager) {
        //风控报表任务
        manager.cron("0 30 12 * * ? ",RiskControlReportSchedule.class);
        //逾期任务
        manager.cron("0 0 1 * * ? ", OverdueLoanSchedule.class);
        //财务日报表任务
        manager.cron("0 30 1 * * ? ", FinanceDailyReportSchedule.class);
        //综合日报表任务
        manager.cron("0 0 2 * * ? ", ComprehensiveReportSchedule.class);
        //快到期短信任务
        manager.cron("0 0 9 * * ? ", ExpiringMessageSchedule.class);

    }

}

