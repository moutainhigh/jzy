package com.kaisa.kams.components.controller.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.service.report.RiskControlReportService;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.enums.LoanStatus;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/5/24.
 */
@IocBean
@At("/report")
public class RiskControlReportController {

    @Inject
    private RiskControlReportService riskControlReportService;
    /**
     * 跳转到财务报表主页面
     *
     * @return
     */
    @At("/riskControl_page")
    @Ok("beetl:/report/riskControl_report.html")
    @RequiresPermissions("riskControl_report:view")
    public Context index() {
        Context ctx = Lang.context();
        //状态
        ctx.set("statusList", LoanStatus.values());
        return ctx;
    }


    @At("/riskControl_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("riskControl_report:view")
    public Object riskControllist(@Param("..")DataTableParam param ) {
        return riskControlReportService.riskControlReportPage(param);
    }

    @At("/riskControl_export")
    @Ok("void")
    @RequiresPermissions("riskControl_report:view")
    public void reportExport(@Param("report_time")String report_time ,HttpServletResponse resp )throws Exception {
        DataTableParam param = new DataTableParam();
        param.setDraw(1);
        param.setLength(999999);
        param.setStart(0);
        Map map  = new HashMap();
        param.setSearchKeys(map);
        param.getSearchKeys().put("report_time",report_time);
        List list = riskControlReportService.riskControlReportPage(param).getData();
        ExcelExportUtil.export(resp,list,"风控"+report_time);
    }


}
