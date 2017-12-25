package com.kaisa.kams.components.controller.oa;

import com.kaisa.kams.components.controller.BillBusinessApplyController;
import com.kaisa.kams.components.controller.ProfitController;
import com.kaisa.kams.components.controller.base.BusinessApplyBaseController;
import com.kaisa.kams.components.controller.base.FlowBaseController;
import com.kaisa.kams.components.params.common.OaMap;
import com.kaisa.kams.components.params.common.OaParam;
import com.kaisa.kams.components.service.flow.FlowService;
import com.kaisa.kams.components.service.LoanOrderService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.components.service.OaService;
import com.kaisa.kams.components.service.ProductInfoTmplService;
import com.kaisa.kams.components.service.ProductService;
import com.kaisa.kams.components.service.UserService;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.components.utils.LoanCalculator;
import com.kaisa.kams.components.view.loan.LoanRepayPlan;
import com.kaisa.kams.components.view.loan.Repayment;
import com.kaisa.kams.enums.ApprovalType;
import com.kaisa.kams.enums.FeeChargeType;
import com.kaisa.kams.enums.FlowConfigureType;
import com.kaisa.kams.enums.LoanLimitType;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanFeeTemp;
import com.kaisa.kams.models.LoanProfit;
import com.kaisa.kams.models.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouchuang on 2017/8/14.
 */
@IocBean
@At("/api/oa")
public class OaController {
    @Inject
    OaService oaService;
    @Inject
    ProductService productService;
    @Inject
    LoanService loanService;
    @Inject
    ProductInfoTmplService productInfoTmplService;
    @Inject
    UserService userService;
    @Inject
    FlowService flowService;
    @Inject
    LoanOrderService loanOrderService;
    @Inject
    FlowBaseController flowBaseController;
    @Inject
    BusinessApplyBaseController businessApplyBaseController;
    @Inject
    BillBusinessApplyController billBusinessApplyController;
    @Inject
    ProfitController profitController;

    private final static BigDecimal ONE_HUNDRED = new BigDecimal(100);

    /**
     * 流程节点审批
     */
    @At("/node_approval")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public Object nodeApproval(@Param("..") OaParam param) {
        OaMap result = new OaMap();
        String loanId = param.getSearchKeys().get("loanId");
        String orderId = param.getSearchKeys().get("orderId");
        String taskId = param.getSearchKeys().get("taskId");
        String approvalCodeStr = param.getSearchKeys().get("approvalCodeStr");
        boolean enterprise = Boolean.valueOf(param.getSearchKeys().get("enterprise"));
        ApprovalType approvalType = ApprovalType.getApprovalTypeByName(param.getSearchKeys().get("approvalType"));
        String content = param.getSearchKeys().get("content");
        boolean needRepeatFlow = Boolean.valueOf(param.getSearchKeys().get("needRepeatFlow"));
        User user = oaService.getUserByOaUserAccount(param.getOaUserAccount());
        boolean intermediary = null == param.getSearchKeys().get("intermediary") ? true : Boolean.valueOf(param.getSearchKeys().get("intermediary"));
        NutMap nutMap = (NutMap) flowBaseController.nodeApprovalByUser(loanId, orderId, taskId, approvalCodeStr, approvalType, content, needRepeatFlow, enterprise, FlowConfigureType.BORROW_APPLY, user,intermediary);
        result.put("ok", nutMap.get("ok"));
        result.put("msg", nutMap.get("msg"));
        return result;
    }


    /**
     * 业务风控财务待审批列表
     */
    @At("/query_approval_list")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public OaMap queryApprovalList(@Param("..") OaParam param) {
        OaMap result = new OaMap();
        result.setv("data.financeApprovalList", oaService.queryApprovalList(param));
        return result;
    }

    /**
     * 财务待审批的详情
     */
    @At("/finance_approval_detail")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public OaMap financeApprovalDetail(@Param("..") OaParam param) {
        OaMap result = new OaMap();
        String loanId = param.getSearchKeys().get("loanId");
        User user = oaService.getUserByOaUserAccount(param.getOaUserAccount());
        if (StringUtils.isEmpty(loanId)) {
            result.setv("ok", false);
            result.setv("msg", "loanId不能为空");
            return result;
        }
        Boolean isBill = productInfoTmplService.isBill(loanId);
        Loan loan = loanService.fetchById(loanId);
        result.setv("data.baseInfo", processBaseInfo(businessApplyBaseController.fetchBaseByLoanId(loanId)));
        result.setv("data.approval", flowBaseController.getUserApproval(loanId, user, FlowConfigureType.BORROW_APPLY));
        result.setv("data.profit", processProfit(profitController.getLoanProfit(loanId)));
        result.setv("data.loan", processLoan(businessApplyBaseController.fetchLoan(loanId)));
        result.setv("data.risk", businessApplyBaseController.queryRiskediaManifest(loanId));
        LoanRepayPlan repayPlan = LoanCalculator.calcuate(loan.getAmount(), loan.getTermType(), loan.getTerm(), loan.getRepayMethod(),
                loan.getLoanLimitType(), LoanLimitType.FIX_AMOUNT.equals(loan.getLoanLimitType()) ? loan.getInterestAmount() : loan.getInterestRate(),
                loan.getRepayDateType(), loan.getLoanTime() == null ? new Date() : loan.getLoanTime(), loan.getMinInterestAmount(), loan.getCalculateMethodAboutDay());
        result.setv("data.repay", processRepayPlan(repayPlan));
        result.setv("data.businessInfo", businessApplyBaseController.queryBusinessInfoToMap(loanId));
        if (isBill) result.setv("data.bill", billBusinessApplyController.queryBillLoan(loanId));
        return result;
    }

    /**
     * 业务风控财务待审批数量
     */
    @At("/query_approval_list_num")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public OaMap queryApprovalListNum(@Param("..") OaParam param) {
        OaMap result = new OaMap();
        result.setv("data.approvalListNum", oaService.queryApprovalCount(param));
        return result;
    }

    /**
     * 审批流程图接口
     */
    @At("/query_flow_chart")
    @POST
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public Object queryFlowChart(@Param("..") OaParam param) {
        String loanId = param.getSearchKeys().get("loanId");
        return flowBaseController.queryApprovalList(loanId, null, FlowConfigureType.BORROW_APPLY);
    }


    private NutMap processBaseInfo(NutMap baseInfo) {
        convertTermType(baseInfo);
        convertInterest(baseInfo);
        return baseInfo;
    }

    private void convertInterest(NutMap baseInfo) {
        if (null == baseInfo) {
            return;
        }
        Map data = (Map)baseInfo.get("data");
        if (null == data) {
            return;
        }
        String interestAmount = (String)data.get("interestAmount");
        String interestRate = (String)data.get("interestRate");
        String termType = (String)data.get("termType");
        if (StringUtils.isNotEmpty(interestAmount) && !"--".equals(interestAmount) && StringUtils.isNotEmpty(termType)) {
            termType = oaService.convertTermTypePer(termType);
            data.put("interestRate",DecimalFormatUtils.removeZeroFormat(interestAmount)+"元"+termType);
            return;
        }

        if (StringUtils.isNotEmpty(interestRate) && !"--".equals(interestRate) && StringUtils.isNotEmpty(termType)) {
            termType = oaService.convertTermTypePer(termType);
            data.put("interestRate", DecimalFormatUtils.removeZeroFormat(interestRate)+"%"+termType);
            return;
        }

    }

    private NutMap processProfit(NutMap loanProfit) {
        LoanProfit profit = (LoanProfit)loanProfit.get("loanProfit");
        if (null != profit && null != profit.getProfit()) {
            profit.setProfit(profit.getProfit().setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        return loanProfit;
    }

    private void convertRepayMethod(Map loanMap) {
        if (null != loanMap) {
            String repayMethod = (String)loanMap.get("repayMethod");
            if (null != repayMethod && LoanRepayMethod.isInLoanRepayMethod(repayMethod)) {
                loanMap.put("repayMethod", LoanRepayMethod.valueOf(repayMethod).getDescription());
            }
        }
    }

    private NutMap processLoan(NutMap loan) {
        Map data = (Map)loan.get("data");
        List<LoanFeeTemp> feeTempList = (List<LoanFeeTemp>)data.get("loanFeeTemps");
        Map loanMap = (Map) data.get("loan");
        convertRepayMethod(loanMap);
        String amount = (String)loanMap.get("amount");
        if (CollectionUtils.isNotEmpty(feeTempList)) {
            List<Map<String,String>> feeMaps = new ArrayList<>();
            for (LoanFeeTemp fee : feeTempList) {
                feeMaps.add(convertLoanFeeToMap(fee,amount));
            }
            data.put("loanFeeTemps",feeMaps);
        }
        return loan;
    }

    private void convertTermType(NutMap baseInfo) {
        if (null == baseInfo) {
            return;
        }
        Map data = (Map)baseInfo.get("data");
        if (null == data) {
            return;
        }
        String termType = (String)data.get("termType");
        String term = (String)data.get("term");
        if (StringUtils.isNotEmpty(termType) && StringUtils.isNotEmpty(term)) {
            String termTypeStr = oaService.convertTermType(termType);
            if (StringUtils.isEmpty(termTypeStr)) {
                term = "至" + term;
            } else {
                term = term + termTypeStr;
            }
            data.put("term",term);
        }
    }


    private Map<String,String> convertLoanFeeToMap(LoanFeeTemp fee,String amount) {
        Map<String,String> map = new HashMap<>();
        map.put("feeType",fee.getFeeType().getDescription());
        map.put("feeCycle",fee.getFeeCycle().getDescription());
        map.put("chargeType",fee.getChargeType().getDescription());
        map.put("chargeNode",fee.getChargeNode().getDescription());
        map.put("feeAmount",getFeeAmount(fee,amount));
        return map;
    }

    private String getFeeAmount(LoanFeeTemp fee,String amount) {
        if (FeeChargeType.FIXED_AMOUNT.equals(fee.getChargeType()) || FeeChargeType.LOAN_REQUEST_INPUT.equals(fee.getChargeType())) {
            return fee.getFeeAmount().setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        if (StringUtils.isNotEmpty(amount) && !"--".equals(amount)) {
            BigDecimal baseAmount = new BigDecimal(amount);
            return baseAmount.multiply(fee.getFeeRate()).divide(ONE_HUNDRED).
                    setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        return "";
    }

    private BigDecimal setBigDecimal2Scale(BigDecimal source) {
        if (null != source) {
            source = source.setScale(2,BigDecimal.ROUND_HALF_UP);
        }
        return source;
    }

    private LoanRepayPlan processRepayPlan(LoanRepayPlan repayPlan) {
        if (null == repayPlan) {
            return repayPlan;
        }
        List<Repayment> repayments = repayPlan.getRepayments();
        if (CollectionUtils.isNotEmpty(repayments)) {
            for (Repayment repayment : repayments) {
                repayment.setOutstanding(setBigDecimal2Scale(repayment.getOutstanding()));
                repayment.setInterest(setBigDecimal2Scale(repayment.getInterest()));
                repayment.setTotal(setBigDecimal2Scale(repayment.getTotal()));
                repayment.setPrincipal(setBigDecimal2Scale(repayment.getPrincipal()));
            }
        }
        return repayPlan;
    }

}
