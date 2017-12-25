package com.kaisa.kams.components.controller.tool;

import com.kaisa.kams.components.service.flow.FlowConfigureService;
import com.kaisa.kams.components.service.HouseInfoService;
import com.kaisa.kams.components.service.LoanRepayService;
import com.kaisa.kams.components.service.PostLoanService;
import com.kaisa.kams.components.service.report.FinanceDailyReportService;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;


/**
 * Created by wangqx on 2017/6/1.
 */
@IocBean
@At("/tool")
public class ToolController {
    @Inject
    private LoanRepayService loanRepayService;
    @Inject
    private FinanceDailyReportService financeDailyReportService;
    @Inject
    private PostLoanService postLoanService;
    @Inject
    private HouseInfoService houseInfoService;
    @Inject
    private FlowConfigureService flowConfigureService;

    @At("/run_overdue_job")
    @Ok("json")
    public Object runOverdueJob() {
        loanRepayService.handleOverdue();
        NutMap result = new NutMap();
        result.setv("message","run overdue job success");
        return result;
    }


    @At("/run_finance_daily_report_job")
    @Ok("json")
    public Object runFinanceDailyReportJob(@Param("date") String preDayDate) {
        financeDailyReportService.executeDailyJob(preDayDate);
        NutMap result = new NutMap();
        result.setv("message","run finance daily report job success");
        return result;
    }

    @At("/send_message")
    @Ok("json")
    public Object sendMessage() {
        NutMap result = new NutMap();
        postLoanService.sendMessageForExpiring();
        return result;
    }


    @At("/save_houseManagerHistory")
    @Ok("json")
    public Object saveHouseManageHistory() {
        NutMap result = new NutMap();
//        loanRepayService.saveHouseManageHistory();
//        result.setv("message","success");
        return result;
    }


    @At("/save_loanRecordHistory")
    @Ok("json")
    public Object saveLoanRecordHistory() {
        NutMap result = new NutMap();
        loanRepayService.saveLoanRecordHistory();
        result.setv("message","success");
        return result;
    }

    @At("/save_houseInfoHistory")
    @Ok("json")
    public Object saveHouseInfoHistory() {
        NutMap result = new NutMap();
        houseInfoService.saveHouseInfoHistory();
        result.setv("message","success");
        return result;
    }

    @At("/save_fee_history")
    @Ok("json")
    public Object saveFeeHistory() {
        NutMap result = new NutMap();
        loanRepayService.saveFee();
        result.setv("message","success");
        return result;
    }

    @At("/transfer_history_flow_data")
    @Ok("json")
    public NutMap transferHistoryFlowData(){
        NutMap result = new NutMap();
        flowConfigureService.transferHistoryFlowData();
        result.setv("message","success");
        return result;
    }

}
