package com.kaisa.kams.components.controller.report;

import com.kaisa.kams.components.params.report.DataTableBusinessReportParam;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.ProductTypeService;
import com.kaisa.kams.components.service.report.BillLoanReportService;
import com.kaisa.kams.components.service.report.GerendaiReportService;
import com.kaisa.kams.components.service.report.HouseInfoReportService;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.components.utils.excelUtil.ExcelExportUtil;
import com.kaisa.kams.components.view.report.ChedaiBusinessReportView;
import com.kaisa.kams.components.view.report.GerendaiBusinessReportView;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.Product;
import com.kaisa.kams.models.ProductType;
import com.kaisa.kams.models.report.BillBusinessReport;

import org.apache.commons.lang.StringUtils;
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

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by lw on 2017/5/25.
 */
@IocBean
@At("/business_report")
public class BusinessReportController {

    @Inject
    private GerendaiReportService gerendaiReportService;
    @Inject
    private LoanSubjectService loanSubjectService;
    @Inject
    private HouseInfoReportService houseInfoReportService;
    @Inject
    private ProductService productService;
    @Inject
    private ProductTypeService productTypeService;
    @Inject
    private BillLoanReportService billLoanReportService;

    /**
     * 跳转到车贷业务报表主页面
     *
     * @return
     */
    @At("/business_chedai")
    @Ok("beetl:/report/businessReport/chedai_report.html")
    @RequiresPermissions("business_report:view")
    public Context cheDaiIndex() {
        return getIndexInitInfo();
    }

    private Context getIndexInitInfo() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanStatus.values());
        ctx.set("loanSubjects",loanSubjectService.queryAble());
        return ctx;
    }

    /**
     * 跳转到员工贷业务报表主页面
     *
     * @return
     */
    @At("/business_gerendai")
    @Ok("beetl:/report/businessReport/gerendai_report.html")
    @RequiresPermissions("business_report:view")
    public Context geRenDaiIndex() {
        return getIndexInitInfo();
    }


    @At("/business_chedai_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_report:view")
    public Object cheDaiList(@Param("..")DataTableBusinessReportParam param ) {
        return gerendaiReportService.queryBusinessCheDaiListForTable(param);
    }

    @At("/business_gerendai_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_report:view")
    public Object geRenDaiList(@Param("..")DataTableBusinessReportParam param ) {
        return gerendaiReportService.queryBusinessGerendaiListForTable(param);
    }

    /**
     * 跳转到票据报表主页面
     *
     * @return
     */
    @At("/business_bill")
    @Ok("beetl:/report/businessReport/piaoju_report.html")
    @RequiresPermissions("business_report:view")
    public Context billIndex() {
        Context ctx = Lang.context();
        ctx.set("statusList", LoanRepayStatus.values());
        ctx.set("LoanStatusList", LoanStatus.values());
        List<Map<String,String>> pt = ProductTypesUtils.getProductTypeList();
        List<Product> billTypeList = null;
        for(Map<String,String> m:pt){
            String codeVal = m.get("code");
            String nameVal = m.get("name");
            if(("PIAOJU").equals(codeVal) && StringUtils.isNotEmpty(nameVal)){
                ProductType productType = productTypeService.fetchByName(nameVal);
                if(null != productType){
                    billTypeList = productService.queryByType(productType.getId());
                }
            }

        }
        ctx.set("billType",billTypeList);
        return ctx;
    }

    @At("/business_bill_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_report:view")
    public Object billList(@Param("..")DataTableBusinessReportParam param ) {
        return billLoanReportService.billBusinessReportPage(param);
    }

    @At("/business_bill_export")
    @Ok("void")
    @RequiresPermissions("business_report:view")
    public void billreportExport(@Param("..")DataTableBusinessReportParam param,
                                     HttpServletResponse resp )throws Exception {
        param = setExportInitParam(param);
        List<BillBusinessReport> billList= billLoanReportService.billBusinessReportPage(param).getData();
        for(BillBusinessReport bill : billList){
            bill.setStatus(gerendaiReportService.getLoanRepayStatus(bill.getStatus()));
            bill.setLoanStatus(gerendaiReportService.getStatus(bill.getLoanStatus()));
        }
        ExcelExportUtil.export(resp,billList,gerendaiReportService.getProductType("PIAOJU"));
    }


    @At("/business_chedai_export")
    @Ok("void")
    @RequiresPermissions("business_report:view")
    public void reportExport(@Param("..")DataTableBusinessReportParam param,
                             HttpServletResponse resp )throws Exception {

        param = setExportInitParam(param);
        List<ChedaiBusinessReportView> cdList= gerendaiReportService.queryBusinessCheDaiListForTable(param).getData();
        for(ChedaiBusinessReportView cd :cdList){
            if(null != cd.getRepayMethod()){
                cd.setRepayMethod(gerendaiReportService.getRepayMethod(cd.getRepayMethod()));
            }
            cd.setStatus(gerendaiReportService.getStatus(cd.getStatus()));
            if(null != cd.getLoanTerm() && null != cd.getTermType()){
                cd.setLoanTerm(gerendaiReportService.getTermType(cd.getTermType(),cd.getLoanTerm()));
            }
        }
        ExcelExportUtil.export(resp,cdList,gerendaiReportService.getProductType("CHEDAI"));
    }

    @At("/business_gerendai_export")
    @Ok("void")
    @RequiresPermissions("business_report:view")
    public void gerendaireportExport(@Param("..")DataTableBusinessReportParam param,
                             HttpServletResponse resp )throws Exception {
        param = setExportInitParam(param);
        List<GerendaiBusinessReportView> cdList= gerendaiReportService.queryBusinessGerendaiListForTable(param).getData();
        for(GerendaiBusinessReportView cd :cdList){
            if(null != cd.getRepayMethod()){
                cd.setRepayMethod(gerendaiReportService.getRepayMethod(cd.getRepayMethod()));
            }
            if(null != cd.getLoanTerm() && null != cd.getTermType()){
                cd.setLoanTerm(gerendaiReportService.getTermType(cd.getTermType(),cd.getLoanTerm()));
            }
            cd.setStatus(gerendaiReportService.getStatus(cd.getStatus()));
        }
        ExcelExportUtil.export(resp,cdList,gerendaiReportService.getProductType("GERENDAI"));
    }


    /**
     * 跳转到赎楼业务报表主页面
     *
     * @return
     */
    @At("/business_shulou")
    @Ok("beetl:/report/businessReport/shulou_report.html")
    @RequiresPermissions("business_report:view")
    public Context shuLouIndex() {
        return getIndexInitInfo();
    }

    @At("/business_shulou_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_report:view")
    public Object shulouList(@Param("..")DataTableBusinessReportParam param ) {
        return houseInfoReportService.getBusinessShuLouReportForTable(param);
    }
    @At("/business_shulou_export")
    @Ok("void")
    @RequiresPermissions("business_report:view")
    public void shulouReportExport(
        @Param("..")DataTableBusinessReportParam param,
        HttpServletResponse resp )throws Exception {

        param = setExportInitParam(param);
        List list = houseInfoReportService.getBusinessShuLouReportForTable(param).getData();
        ExcelExportUtil.export(resp,list,"赎楼贷");
    }


    /**
     * 跳转到红本业务报表主页面
     *
     * @return
     */
    @At("/business_hongben")
    @Ok("beetl:/report/businessReport/hongben_report.html")
    @RequiresPermissions("business_report:view")
    public Context hongBenIndex() {
        return getIndexInitInfo();
    }
    @At("/business_hongben_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_report:view")
    public Object hongbenlist(@Param("..")DataTableBusinessReportParam param) {
        return houseInfoReportService.getBusinessHongBenReportForTable(param);
    }
    @At("/business_hongben_export")
    @Ok("void")
    @RequiresPermissions("business_report:view")
    public void hongbenReportExport(@Param("..")DataTableBusinessReportParam param,
                                   HttpServletResponse resp )throws Exception {
        param = setExportInitParam(param);
        List list = houseInfoReportService.getBusinessHongBenReportForTable(param).getData();
        ExcelExportUtil.export(resp,list,"红本贷");
    }

    private DataTableBusinessReportParam setExportInitParam(DataTableBusinessReportParam param) {
        param.setDraw(1);
        param.setLength(Integer.MAX_VALUE);
        param.setStart(0);
        return param;
    }


}
