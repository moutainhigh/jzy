package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.BorrowerAccount;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.LoanFeeTemp;
import com.kaisa.kams.models.Product;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by weid on 2016/12/15.
 */
@IocBean(fields="dao")
public class LoanFeeTempService  extends IdNameEntityService<LoanFeeTemp> {

    private static final Log log = Logs.get();

    @Inject
    private LoanService loanService;

    /**
     * 根据loanId查询
     * @param loanId
     * @return
     */
    public List<LoanFeeTemp> queryByLoanId(String loanId) {
        return dao().query(LoanFeeTemp.class, Cnd.where("loanId","=",loanId));
    }

    /**
     * 通过loanId删除数据
     * @param loanId
     * @return
     */
    public boolean deleteByLoanId(String loanId) {
        return dao().clear(LoanFeeTemp.class,Cnd.where("loanId","=",loanId))>0;
    }

    /**
     * 新增数据
     * @param loanFeeTemp
     */
    public LoanFeeTemp add(LoanFeeTemp loanFeeTemp) {
        return dao().insert(loanFeeTemp);
    }

    /**
     * 新增数据
     * @param loanFeeTempList
     */
    public List add(List<LoanFeeTemp> loanFeeTempList) {
        return dao().insert(loanFeeTempList);
    }

    /**
     * 标的id，收取节点，收费频率查询
     * @param loanId
     * @param chargeNode
     * @param feeCycle
     * @return
     */
    public List<LoanFeeTemp> queryByLoanIdAndChargeNodeAndFeeCycle(String loanId, FeeChargeNode chargeNode, FeeCycleType feeCycle) {
        return dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanId).and("chargeNode","=",chargeNode).and("feeCycle","=",feeCycle));
    }

    /**
     * 标的id，收取节点查询费用
     * @param loanId
     * @param chargeNode
     * @return
     */
    public List<LoanFeeTemp> queryByLoanIdAndChargeNode(String loanId,FeeChargeNode chargeNode){
        return dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanId).and("chargeNode","=",chargeNode).orderBy("feeType","asc"));
    }

    /**
     * 标的id，收取节点，类型查询费用
     * @param loanId
     * @param feeType
     * @return
     */
    public List<LoanFeeTemp> queryByLoanIdAndFeeType(String loanId,FeeType feeType){
        return dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanId).and("feeType","=",feeType));
    }

    /**
     * 查询
     * @param loanId 标的Id
     * @param loanRepayMethod 还款方式
     * @return
     */
    public List<LoanFeeTemp> queryByLoanIdAndLoanRepayMethod(String loanId, LoanRepayMethod loanRepayMethod) {
        return dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanId).and(Cnd.exps("repayMethod","=",loanRepayMethod).or("repayMethod","=","ALL")));
    }

    public List<LoanFeeTemp> queryByLoanIdAndLoanRepayMethodAndFeeType(String loanId, LoanRepayMethod loanRepayMethod){
        List<LoanFeeTemp>  loanFeeTemps= queryByLoanIdAndLoanRepayMethod(loanId,loanRepayMethod);
        if(null!=loanFeeTemps&&loanFeeTemps.size()>0){
            for (int i=0;i<loanFeeTemps.size(); i++){
                LoanFeeTemp tmp = loanFeeTemps.get(i);
                if(null==tmp){
                    continue;
                }
                //如果是逾期罚息或者提前结清罚息都为0
                if (FeeType.OVERDUE_FEE == tmp.getFeeType() || FeeType.PREPAYMENT_FEE == tmp.getFeeType()) {
                    tmp.setFeeAmount(new BigDecimal(0));
                } else {
                    if (FeeChargeType.LOAN_AMOUNT_RATE == tmp.getChargeType()) {
                        Loan loanTmp = loanService.fetchById(loanId);
                        if (null != loanTmp.getAmount() && null != tmp.getFeeRate()) {
                            tmp.setFeeAmount(loanTmp.getAmount().multiply(tmp.getFeeRate()).divide(new BigDecimal(100)));
                        }
                    } else if (FeeChargeType.FIXED_AMOUNT == tmp.getChargeType() || FeeChargeType.LOAN_REQUEST_INPUT == tmp.getChargeType()) {

                    } else {
                        tmp.setFeeAmount(new BigDecimal(0));
                    }
                }
            }
        }
        return loanFeeTemps;
    }

    /**
     * 查询
     * @param loanId 标的Id
     * @param repayMethod 还款方式
     * @return
     */
    public List<LoanFeeTemp> queryByLoanIdAndLoanRepayMethodAndFeeType(String loanId, List<String>  repayMethod,FeeChargeNode chargeNode) {
        return dao().query(LoanFeeTemp.class,Cnd.where("loanId","=",loanId).and("repayMethod","in",repayMethod).and("chargeNode","=",chargeNode));
    }

    /**
     * 修改数据
     * @param loanFeeTemp
     */
    public boolean update(LoanFeeTemp loanFeeTemp) {
       if(null==loanFeeTemp){
           return false;
       }
        return dao().update(loanFeeTemp, "^(updateTime|updateBy|feeAmount)$")>0;
    }
}
