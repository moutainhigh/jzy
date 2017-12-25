package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BillLoanService;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunwanchao on 2017/2/27.
 */
@IocBean
@At("/bill_business_apply")
public class BillBusinessApplyController {

    @Inject
    private BillLoanService billLoanService;


    @POST
    @At("/add_borrower")
    @Ok("json")
    public Object addBorrower(@Param("productId") String productId,
                              @Param("saleId") String saleId,
                              @Param("loanSubjectId") String loanSubjectId,
                              @Param("masterBorrowerId") String masterBorrowerId,
                              @Param("itemStr") String attachItemStr,
                              @Param("loanId") String loanId) {
        return billLoanService.addBorrower(productId, saleId, loanSubjectId, masterBorrowerId, attachItemStr, loanId, ShiroSession.getLoginUser());
    }

    @At("/add_bill")
    @Ok("json")
    public Object addBill(@Param("totalAmount") BigDecimal totalAmount,
                          @Param("interest") BigDecimal interest,
                          @Param("discountTime") Date discountTime,
                          @Param("accountName") String accountName,
                          @Param("accountBank") String accountBank,
                          @Param("accountNo") String accountNo,
                          @Param("loanId") String loanId,
                          @Param("loanSubjectId") String loanSubjectId,
                          @Param("intermediaryStr") String intermediaryStr) {
        return billLoanService.addBillLoan(totalAmount,interest,discountTime, accountName, accountBank, accountNo,loanId, loanSubjectId,ShiroSession.getLoginUser(), intermediaryStr);
    }

    @At("/add_bill_repay")
    @Ok("json")
    public Object addBillRepay(@Param("billLoanRepayStr") String billLoanRepayStr, @Param("loanId") String loanId) {
        return billLoanService.addBillLoanRepay(billLoanRepayStr, loanId, ShiroSession.getLoginUser());
    }

    @POST
    @At("/add_bill_submit")
    @Ok("json")
    public Object addBill(@Param("productId") String productId,
                          @Param("saleId") String saleId,
                          @Param("loanSubjectId") String loanSubjectId,
                          @Param("masterBorrowerId") String masterBorrowerId,
                          @Param("itemStr") String attachItemStr,
                          @Param("totalAmount") BigDecimal totalAmount,
                          @Param("interest") BigDecimal interest,
                          @Param("discountTime") Date discountTime,
                          @Param("accountName") String accountName,
                          @Param("accountBank") String accountBank,
                          @Param("accountNo") String accountNo,
                          @Param("billLoanRepayStr") String billLoanRepayStr,
                          @Param("loanId") String loanId,
                          @Param("intermediaryStr") String intermediaryStr) {
        return billLoanService.addBillLoan(productId, saleId, loanSubjectId, masterBorrowerId, attachItemStr,totalAmount,interest,discountTime, accountName, accountBank, accountNo,billLoanRepayStr, loanId, ShiroSession.getLoginUser(), intermediaryStr);
    }

    @At("/query_bill")
    @Ok("json")
    public NutMap queryBillLoan(@Param("loanId") String loanId) {
        return billLoanService.queryBillLoanInfo(loanId);
    }
}
