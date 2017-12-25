package com.kaisa.kams.components.controller.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.report.BillLoanReportService;
import com.kaisa.kams.components.service.report.ComprehensiveDayReportService;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.enums.LoanStatus;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/6/7.
 */
@IocBean
@At("/report")
public class ComprehensiveReportController {

    @Inject
    private BillLoanReportService billLoanReportService;

    @Inject
    private ComprehensiveDayReportService comprehensiveDayReportService;
    /**
     * 跳转到财务报表主页面
     *
     * @return
     */
    @At("/comprehensive_report_page")
    @Ok("beetl:/report/businessReport/comprehensive_report.html")
    @RequiresPermissions("comprehensive_report:view")
    public Context index() {
        return setStatusInContext();
    }

    @At("/comprehensive_day_report_page")
    @Ok("beetl:/report/businessReport/comprehensive_day_report.html")
    @RequiresPermissions("comprehensive_day_report:view")
    public Context indexComprehensiveDayReport() {
        return setStatusInContext();
    }

    private Context setStatusInContext() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanStatus.values());
        return ctx;
    }


    @At("/comprehensiveReport_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("comprehensive_report:view")
    public Object comprehensiveReportlist(@Param("..")DataTableParam param ) {
        String report_time = param.getSearchKeys().get("report_time");
        param.getSearchKeys().put("loanBeginDate",report_time.split("~")[0]);
        param.getSearchKeys().put("loanEndDate",report_time.split("~")[1]);
        return billLoanReportService.comprehensiveReportPage(param);
    }

    @At("/comprehensive_day_report_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("comprehensive_day_report:view")
    public Object comprehensiveDayReportlist(@Param("..")DataTableParam param ) {
        return comprehensiveDayReportService.comprehensiveDayReportPage(param);
    }


    @At("/comprehensiveReport_export")
    @Ok("void")
    @RequiresPermissions("comprehensive_report:view")
    public void comprehensiveReportExport(@Param("report_time")String report_time , HttpServletResponse resp) {
        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        map.put("loanBeginDate",report_time.split("~")[0]);
        map.put("loanEndDate",report_time.split("~")[1]);
        param.setSearchKeys(map);
        List list = billLoanReportService.comprehensiveReportPage(param).getData();
        ExcelExportUtil.export(resp,list,"综合报表"+report_time);
    }

    @At("/comprehensive_day_report_export")
    @Ok("void")
    @RequiresPermissions("comprehensive_day_report:view")
    public void comprehensiveDayReportExport(@Param("report_time")String report_time , HttpServletResponse resp) {
        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        map.put("report_time",report_time);
        param.setSearchKeys(map);
        List list = comprehensiveDayReportService.comprehensiveDayReportPage(param).getData();
        ExcelExportUtil.export(resp,list,"综合日报表"+report_time);
    }

}
