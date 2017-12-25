package com.kaisa.kams.components.controller.report;

import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.report.ExtensionReportService;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.models.DataTables;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import javax.servlet.http.HttpServletResponse;

/**
 * 展期财务报表
 *
 * @author pengyueyang created on 2017/12/11.
 */
@IocBean
@At("/extension_report")
public class ExtensionReportController {

    @Inject
    private LoanSubjectService loanSubjectService;

    @Inject
    private ExtensionReportService extensionReportService;

    @At("/index")
    @Ok("beetl:/report/extension_report.html")
    @GET
    @RequiresPermissions("extension_report:view")
    public Context index() {
        Context ctx = Lang.context();
        ctx.set("loanSubjectList", loanSubjectService.queryAble());
        return ctx;
    }

    @At("/list")
    @AdaptBy(type = JsonAdaptor.class)
    @POST
    @RequiresPermissions("extension_report:view")
    public DataTables list(@Param("..") DataTableFinanceReportParam param) {
        int count = extensionReportService.getCount(param);
        DataTables result = new DataTables(param.getDraw(), count, count);
        result.setData(extensionReportService.getList(param));
        return result;
    }

    @At("/export")
    @Ok("void")
    @RequiresPermissions("extension_report:view")
    public void export(@Param("..") DataTableFinanceReportParam param, HttpServletResponse response) {
        param.setLength(Integer.MAX_VALUE);
        param.setStart(0);
        ExcelExportUtil.export(response, extensionReportService.getList(param), "展期报表");
    }


}
