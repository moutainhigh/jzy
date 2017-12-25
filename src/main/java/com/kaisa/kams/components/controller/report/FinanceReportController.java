package com.kaisa.kams.components.controller.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.service.report.BillLoanReportService;
import com.kaisa.kams.components.service.report.GerendaiReportService;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.report.FinanceDailyReportService;
import com.kaisa.kams.components.service.report.HouseInfoReportService;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.components.view.report.GerendaiReportView;
import com.kaisa.kams.enums.FinanceReportType;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.BillReport;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.report.HouseReport;
import com.kaisa.kams.models.report.CheDaiFinanceDailyReport;
import com.kaisa.kams.models.report.GeRenDaiFinanceDailyReport;

import org.apache.commons.collections.CollectionUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liuwen01 on 2017/5/17.
 */
@IocBean
@At("/report")
public class FinanceReportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinanceReportController.class);

    private final String DAILY_REPORT_PREFIX = "财务日报表-";

    @Inject
    private GerendaiReportService gerendaiReportService;
    @Inject
    private BillLoanReportService billLoanReportService;
    @Inject
    private HouseInfoReportService houseInfoReportService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private FinanceDailyReportService financeDailyReportService;




    /**
     * 跳转到财务报表主页面
     *
     * @return
     */
    @At("/finance_report")
    @Ok("beetl:/report/finance_report.html")
    @RequiresPermissions("finance_report:view")
    public Context list() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanStatus.values());
        ctx.set("loanSubjectList",loanSubjectService.queryAble());
        ctx.set("reportNameList",ProductTypesUtils.getProductTypeList());
        return ctx;
    }

    @At("/report_list")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("finance_report:view")
    public Object reportList(@Param("..")DataTableFinanceReportParam param) {
        String reportName = param.getReportName();
        if(isBillReport(reportName)){
            return billLoanReportService.billReportPage(param);
        }
        if(isCheDaiOrGeRenDai(reportName)){
            return gerendaiReportService.queryChedaiListForTable(param);
        }
        if(isHongBenOrShuLou(reportName)){
            return houseInfoReportService.getFinanceReportForTable(param);
        }
        return null;
    }

    private boolean isBillReport(String reportName) {
        if (FinanceReportType.PIAOJU.name().equals(reportName)) {
            return true;
        }
        if (FinanceReportType.YINPIAO.name().equals(reportName)) {
            return true;
        }
        return false;
    }

    @At("/report_export")
    @Ok("void")
    @RequiresPermissions("finance_report:view")
    public void reportExport(@Param("..")DataTableFinanceReportParam param, HttpServletResponse resp)throws Exception {
        param.setDraw(1);
        param.setLength(Integer.MAX_VALUE);
        param.setStart(0);
        String reportName = param.getReportName();
        String reportTime = param.getReportTime();
        if(isBillReport(reportName)){
            List<BillReport> billReportList = billLoanReportService.billReportPage(param).getData();
            for( BillReport billReport :billReportList){
                billReport.setStatus(gerendaiReportService.getStatus(billReport.getStatus()));
            }
            ExcelExportUtil.export(resp,billReportList,(FinanceReportType.PIAOJU.name().equals(reportName)?"票据":"银票")+reportTime);
        }
        if(isCheDaiOrGeRenDai(reportName)){
            List<GerendaiReportView> grdList= gerendaiReportService.queryChedaiListForTable(param).getData();
            for(GerendaiReportView grd :grdList){
                if(null != grd.getRepaymentMethods()){
                    grd.setRepaymentMethods(gerendaiReportService.getRepayMethod(grd.getRepaymentMethods()));
                }
                if(null != grd.getTermType() && null!= grd.getLoanTerm()){
                    grd.setLoanTerm(gerendaiReportService.getTermType(grd.getTermType(),grd.getLoanTerm()));
                }
                grd.setStatus(gerendaiReportService.getStatus(grd.getStatus()));
            }
            if(FinanceReportType.CHEDAI.name().equals(reportName)){
                ExcelExportUtil.export(resp,grdList,"车贷");
            }else {
                ExcelExportUtil.export(resp,grdList,"个人贷");
            }
        }
        if(isHongBenOrShuLou(reportName)){
            List<HouseReport> houseReportList= houseInfoReportService.getFinanceReportForTable(param).getData();
            for(HouseReport houseReport : houseReportList){
                if(null != houseReport.getRepaymentMethods()){
                    houseReport.setRepaymentMethods(gerendaiReportService.getRepayMethod(houseReport.getRepaymentMethods()));
                    houseReport.setStatus(gerendaiReportService.getStatus(houseReport.getStatus()));
                }
            }
            if(FinanceReportType.HONGBEN.name().equals(reportName)){
                ExcelExportUtil.export(resp,houseReportList,"红本");
            }else {
                ExcelExportUtil.export(resp,houseReportList,"赎楼");
            }
        }
    }


    @At("/finance_daily_report")
    @Ok("beetl:/report/finance_daily_report.html")
    @RequiresPermissions("finance_daily_report:view")
    public Context financeDailyReport() {
        Context ctx = Lang.context();
        ctx.set("reportNameList",ProductTypesUtils.getProductTypeList());
        return ctx;
    }

    @At("/finance_daily_report_list")
    @Ok("json")
    @RequiresPermissions("finance_daily_report:view")
    @AdaptBy(type=JsonAdaptor.class)
    public Object financeDailyReportList(@Param("..")DataTableParam param ) {
        return financeDailyReportService.getReportDataTables(param);
    }


    @At("/finance_daily_report_export")
    @Ok("void")
    @RequiresPermissions("finance_daily_report:view")
    @AdaptBy(type=JsonAdaptor.class)
    public void financeDailyReportExport(@Param("report_name")String reportName,@Param("report_time")String reportTime,HttpServletResponse resp) {
        DataTableParam param = new DataTableParam();
        param.setStart(0);
        param.setLength(Integer.MAX_VALUE);
        Map<String,String> searchKeys = new HashMap<>();
        searchKeys.put("report_name",reportName);
        searchKeys.put("report_time",reportTime);
        param.setSearchKeys(searchKeys);
        DataTables dataTables = financeDailyReportService.getReportDataTables(param);
        try {
            List list = dataTables.getData();
            if (isCheDaiOrGeRenDai(reportName) && CollectionUtils.isNotEmpty(list)) {
                list.forEach(report -> setRepayMethodStr(report,reportName));
            }
            ExcelExportUtil.export(resp,list,DAILY_REPORT_PREFIX+ProductTypesUtils.getName(reportName)+reportTime);
        } catch (Exception e) {
            LOGGER.error("Export finance daily report error message:{},",e.getMessage());
            e.printStackTrace();
        }
    }

    private void setRepayMethodStr(Object obj, String reportName) {
        if (FinanceReportType.CHEDAI.name().equals(reportName)) {
            CheDaiFinanceDailyReport report = (CheDaiFinanceDailyReport)obj;
            if (null != report.getRepayMethod()) {
                report.setRepayMethodStr(report.getRepayMethod().getDescription());
            }
            return;
        }
        if (FinanceReportType.GERENDAI.name().equals(reportName)) {
            GeRenDaiFinanceDailyReport report = (GeRenDaiFinanceDailyReport) obj;
            if (null != report.getRepayMethod()) {
                report.setRepayMethodStr(report.getRepayMethod().getDescription());
            }
            return;
        }

    }


    private boolean isHongBenOrShuLou(String reportName) {
         if (FinanceReportType.HONGBEN.name().equals(reportName)) {
             return true;
         }
         if (FinanceReportType.SHULOU.name().equals(reportName)){
             return true;
         }
         return false;
    }

    private boolean isCheDaiOrGeRenDai(String reportName) {
        if (FinanceReportType.CHEDAI.name().equals(reportName)) {
            return true;
        }
        if (FinanceReportType.GERENDAI.name().equals(reportName)) {
            return true;
        }
        return false;
    }


}
