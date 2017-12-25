package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.BillLoanService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.PostLoanService;
import com.kaisa.kams.components.service.ProductInfoTmplService;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by liuwen01 on 2016/12/12.
 */
@IocBean
@At("/post_loan")
public class PostLoanController {

    @Inject
    private PostLoanService postLoanService;

    @Inject
    private LoanService loanService;

    @Inject
    private ProductInfoTmplService productInfoTmplService;

    @Inject
    private BillLoanService billLoanService;
    /**
     * 跳转到快到期贷款页面
     *
     * @return
     */
    @At
    @Ok("beetl:/postLoan/list.html")
    @RequiresPermissions("postLoan:view")
    public Context list() {
        return getInitInfo();
    }

    private Context getInitInfo() {
        Context ctx = Lang.context();
        //产品条线
        ctx.set("businessTypeList", BusinessLine.values());
        return ctx;
    }

    /**
     * 跳转到逾期贷款页面
     *
     * @return
     */
    @At
    @Ok("beetl:/postLoan/overdueList.html")
    @RequiresPermissions("overdueLoan:view")
    public Context overdueList() {
        return getInitInfo();
    }

    /**
     * 跳转到已结清贷款页面
     *
     * @return
     */
    @At
    @Ok("beetl:/postLoan/clearedList.html")
    @RequiresPermissions("clearedLoan:view")
    public Context clearedList() {
        return getInitInfo();
    }

    @At("/list_post_loan")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("postLoan:view")
    public Object list(@Param("..")DataTableParam param) {
        return postLoanService.queryPostLoanListForTable(param);
    }

    @At("/query_overdue_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("overdueLoan:view")
    public DataTables queryOverdueList(@Param("..")DataTableParam param) {
        return postLoanService.queryOverdueListForTable(param);
    }

    @At("/query_cleared_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("clearedLoan:view")
    public Object queryClearedList(@Param("..")DataTableParam param) {
        return postLoanService.queryClearedListForTable(param);
    }

    /**
     * 通过ID查询快到期贷款信息
     * @return
     */
    @At("/fetch_post_loan")
    @GET
    @Ok("json")
    public Object fetchPostLoan(@Param("id")String id,@Param("repayId")String repayId, @Param("type")String type){
        NutMap result = new NutMap();
        List<LoanRepay> postLoanList;
        if(null != type && ("LOANED").equals(type)){
            //单信息
            postLoanList =  postLoanService.fetchById(repayId,LoanRepayStatus.LOANED);
        }else {
            postLoanList = postLoanService.fetchOverdueByLoanId(id,LoanRepayStatus.OVERDUE);
        }
        //查询还款计划信息
        List<LoanRepay> loanRepayList  = postLoanService.queryAllByloanId(id);
        //查询到Loan的信息
        Loan loan = loanService.fetchById(id);

        Map data = new HashMap<>();
        Map postLoan = new HashMap<>();
        List<LoanFee> loanFeeList = new ArrayList<>();

        if (StringUtils.isNotEmpty(repayId)) {
            //查询票据还款计划
            BillLoanRepay billLoanRepay = postLoanService.getBillRepayById(repayId);
            List<LoanFee> loanFeeListByRepayId = postLoanService.fetchLoanFeeByRepayId(repayId, FeeChargeNode.REPAY_NODE);
            String realDate = "";
            long days;
            if (null != postLoanList && postLoanList.size() > 0) {
                postLoan.put("period", postLoanList.get(0).getPeriod());
                postLoan.put("dueDate", postLoanList.get(0).getDueDate());
                if (null != billLoanRepay) {
                    postLoan.put("dueDate", DateUtil.getDateAfter(postLoanList.get(0).getDueDate(),billLoanRepay.getOverdueDays()));
                }
                if (null != postLoanList.get(0).getRepayDate()) {
                    realDate = TimeUtils.formatDate("yyyy-MM-dd", postLoanList.get(0).getRepayDate());
                }
                String dueDate = TimeUtils.formatDate("yyyy-MM-dd", postLoanList.get(0).getDueDate());
                if (StringUtils.isEmpty(realDate)) {
                    days = DateUtil.getDaySub(dueDate, TimeUtils.formatDate("yyyy-MM-dd", new Date()));
                    if (null != billLoanRepay) {
                        days = days - billLoanRepay.getOverdueDays();
                    }
                } else {
                    days = DateUtil.getDaySub(dueDate, realDate);
                    if (null != billLoanRepay) {
                        days = days - billLoanRepay.getOverdueDays();
                    }
                }
                if (null != type && ("OVERDUE").equals(type)) {
                    postLoan.put("dueDays", days > 0 ? days + "天" : "未逾期");
                } else {
                    postLoan.put("dueDays", "未逾期");
                }
                postLoan.put("amount", postLoanList.get(0).getAmount());
                postLoan.put("interest", postLoanList.get(0).getInterest());
            }
            if (null != loanFeeListByRepayId) {
                loanFeeList = loanFeeListByRepayId;
                for (int i = loanFeeList.size() - 1; i >= 0; i--) {
                    if (null != loanFeeList.get(i).getFeeType() && loanFeeList.get(i).getFeeType().equals(FeeType.OVERDUE_FEE) && loanFeeList.get(i).getChargeNode().equals(FeeChargeNode.REPAY_NODE)) {
                        postLoan.put("overdueFee", loanFeeList.get(i).getFeeAmount());
                        loanFeeList.remove(i);
                    }
                }
            }
        }
        Object repayList = loanRepayList;
        if (productInfoTmplService.isBill(id)) {
            repayList = billLoanService.queryBillLoanRepay(id);
        }
        data.put("loanRepayList",repayList);
        data.put("postLoan",postLoan);
        data.put("postLoanFeeList",loanFeeList);
        data.put("loanTime",loan.getLoanTime());
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",data);
        return result;
    }





    @At("/cleared_proof_export")
    @Ok("void")
    @RequiresPermissions("clearedLoan:view")
    public void clearedProofExport(@Param("loanId")String loanId ,HttpServletResponse response )throws Exception {
        postLoanService.clearedProofExport(loanId,response);
    }

    /**
     * 初始化逾期录入信息
     * @return
     */
    @At("/init_overdueRecord")
    @POST
    public Object initOverdueRecord(@Param("repayId") String repayId){
        NutMap result = new NutMap();
        List<OverdueRecord> overdueRecordList = postLoanService.queryByRepayId(repayId);
        result.put("overdueRecordLis",overdueRecordList);
        result.put("loanFormList", LoanForm.values());
        return result;
    }

    /**
     * 新增逾期信息
     * @param overdueRecord
     * @return
     */
    @At("/add")
    @POST
    public Object add(@Param("..")OverdueRecord overdueRecord){
        NutMap result = new NutMap();
        if(null==overdueRecord){
            result.put("ok",false);
            result.put("msg","录入信息错误");
            return result;
        }
        User user = ShiroSession.getLoginUser();
        overdueRecord.setCreateBy(user.getName());
        overdueRecord.setUpdateBy(user.getName());
        overdueRecord.setCreateTime(new Date());
        overdueRecord.setUpdateTime(new Date());
        overdueRecord.setCreaterId(user.getId());
        overdueRecord.setPosition(postLoanService.getMaxCount()+1);
        OverdueRecord resultRecord = postLoanService.add(overdueRecord);
        if(null==resultRecord){
            result.put("ok",false);
            result.put("msg","保存失败");
            return result;
        }
        result.put("ok",true);
        result.put("msg","保存成功");
        result.put("data",resultRecord);
        return result;
    }

    /**
     * 查看信息
     * @return
     */
    @At("/query_overdueRecord")
    @POST
    public Object queryOverdueRecord(@Param("repayId") String repayId){
        NutMap result = new NutMap();
        List<OverdueRecord> overdueRecordList = postLoanService.queryByRepayId(repayId);
        result.put("overdueRecordLis",overdueRecordList);
        return result;
    }
}
