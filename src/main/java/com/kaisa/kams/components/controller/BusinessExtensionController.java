package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BusinessExtensionService;
import com.kaisa.kams.enums.LoanLimitType;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanTermType;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.math.BigDecimal;

/**
 * Created by sunwanchao on 2017/4/11.
 */
@IocBean
@At("/business_extension")
public class BusinessExtensionController {
    @Inject
    private BusinessExtensionService businessExtensionService;

    /**
     *
     * @param loanId 标的id
     * @param termType 展期期限类型
     * @param term 展期期限
     * @param loanLimitType 展期利息类型
     * @param interest 展期利息
     * @return
     */
    @POST
    @At("/insert")
    @Ok("json")
    @RequiresPermissions("business_extension:create")
    public Object insert(@Param("loanId") String loanId,
                         @Param("termType") LoanTermType termType,
                         @Param("term") String term,
                         @Param("loanLimitType") LoanLimitType loanLimitType,
                         @Param("interest") BigDecimal interest,
                         @Param("repayMethod") String repayMethod,@Param("enterpriseExplain") String enterpriseExplain,
                         @Param("enterpriseAgreement") String enterpriseAgreement,@Param("calculationMethod") String calculationMethod
                         ,@Param("repayDateType") String repayDateType){
        return businessExtensionService.insert(loanId,termType,term,loanLimitType,interest, repayMethod,enterpriseExplain,enterpriseAgreement,calculationMethod,repayDateType);
    }

    @GET
    @At("/query_history")
    @Ok("json")
    public Object queryHistory(@Param("loanId") String loanId){
        return businessExtensionService.queryRepayByLoanId(loanId);
    }

    @POST
    @At("/generate_loan_repay")
    @Ok("json")
    @RequiresPermissions("business_extension:view")
    public Object generateLoanRepay(@Param("loanId") String loanId,
                                    @Param("termType") LoanTermType termType,
                                    @Param("term") String term,
                                    @Param("loanLimitType") LoanLimitType loanLimitType,
                                    @Param("interest") BigDecimal interest,
                                    @Param("repayMethod") String repayMethod,
                                    @Param("calculationMethod") String calculationMethod,
                                    @Param("repayDateType") String repayDateType){
        return businessExtensionService.generateLoanRepay(loanId,termType,term,loanLimitType,interest, repayMethod,calculationMethod,repayDateType);
    }

    @POST
    @At("/query_loan_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("business_extension:view")
    public Object queryLoanList(@Param("..")DataTableParam param){
        return businessExtensionService.queryLoanList(param);
    }
}
