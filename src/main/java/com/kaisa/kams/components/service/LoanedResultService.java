package com.kaisa.kams.components.service;

import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.IntermediaryApply;
import com.kaisa.kams.models.Loan;
import com.kaisa.kams.models.flow.ApprovalResult;
import com.kaisa.kams.models.flow.IntermediaryApplyLoanedResult;
import com.kaisa.kams.models.flow.LoanedResult;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * Created by zhouchuang on 2017/8/24.
 */
@IocBean(fields="dao")
public class LoanedResultService extends IdNameEntityService<LoanedResult> {

    /**
     * 新增
     * @param approvalResult
     * @return
     */
    public ApprovalResult add(ApprovalResult approvalResult) {
        return dao().insert(approvalResult);
    }

    public LoanedResult addLoanRecode(Loan loan, Date lastUpdateTime){

        Date now  = new Date();
        LoanedResult approvalResult =  new LoanedResult();
        approvalResult.setLoanId(loan.getId());
        approvalResult.setApprovalTime(now);
        approvalResult.setNodeName(loan.getLoanStatus().equals(LoanStatus.LOANED)?"待放款":(loan.getLoanStatus().equals(LoanStatus.CLEARED)?"还款中":"已结清"));
        approvalResult.setNodeCode(loan.getLoanStatus().equals(LoanStatus.LOANED)?"D1-贷后":(loan.getLoanStatus().equals(LoanStatus.CLEARED)?"D2-贷后":"D3-贷后"));
        approvalResult.setUserId(ShiroSession.getLoginUser().getId());
        approvalResult.setUserName(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateTime(now);
        approvalResult.setUpdateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setUpdateTime(now);
        approvalResult.setStartTime(lastUpdateTime);
        approvalResult.setDuration(DateUtil.daysBetweenTowDate(approvalResult.getStartTime(),approvalResult.getCreateTime()));
        return dao().insert(approvalResult);
    }

    public IntermediaryApplyLoanedResult addIntermediaryApplyLoanRecode(IntermediaryApply intermediaryApply, Date lastUpdateTime){

        Date now  = new Date();
        IntermediaryApplyLoanedResult approvalResult =  new IntermediaryApplyLoanedResult();
        approvalResult.setLoanId(intermediaryApply.getLoanId());
        approvalResult.setApprovalTime(now);
        approvalResult.setNodeName("待放款");
        approvalResult.setNodeCode("D1-贷后");
        approvalResult.setUserId(ShiroSession.getLoginUser().getId());
        approvalResult.setUserName(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateTime(now);
        approvalResult.setUpdateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setUpdateTime(now);
        approvalResult.setStartTime(lastUpdateTime);
        approvalResult.setDuration(DateUtil.daysBetweenTowDate(approvalResult.getStartTime(),approvalResult.getCreateTime()));
        return dao().insert(approvalResult);
    }

    public LoanedResult startLoanRecode(Loan loan ){
        Date now  = new Date();
        LoanedResult approvalResult =  new LoanedResult();
        approvalResult.setLoanId(loan.getId());
        approvalResult.setUserId(ShiroSession.getLoginUser().getId());
        approvalResult.setUserName(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setCreateTime(now);
        approvalResult.setUpdateBy(ShiroSession.getLoginUser().getName());
        approvalResult.setUpdateTime(now);
        approvalResult.setStartTime(now);
        return dao().insert(approvalResult);
    }

    public void endLoanRecode(Loan loan ){
        Date now  = new Date();
        LoanedResult loanedResult =  new LoanedResult();
        Cnd cnd = Cnd.where("loanId","=",loan.getId()).and("status","=","DISABLED");
        cnd.desc("nodeCode");
        loanedResult = this.query(cnd).get(0);
        loanedResult.setNodeName(loan.getLoanStatus().equals(LoanStatus.LOANED)?"待放款":(loan.getLoanStatus().equals(LoanStatus.CLEARED)?"还款中":"已结清"));
        loanedResult.setNodeCode(loan.getLoanStatus().equals(LoanStatus.LOANED)?"D1-贷后":(loan.getLoanStatus().equals(LoanStatus.CLEARED)?"D2-贷后":"D3-贷后"));
        loanedResult.setUpdateBy(ShiroSession.getLoginUser().getName());
        loanedResult.setUpdateTime(now);
        loanedResult.setApprovalTime(now);
        loanedResult.setDuration(DateUtil.daysBetweenTowDate(loanedResult.getStartTime(),loanedResult.getApprovalTime()));
        dao().update(loanedResult);
    }




    public List<LoanedResult> loanedResultList(String loanId){
        Cnd cnd = Cnd.where("loanId","=",loanId);
        cnd.asc("nodeCode");
        return dao().query(LoanedResult.class,cnd);
    }

    public List<IntermediaryApplyLoanedResult> intermediaryApplyLoanedResultList(String loanId){
        Cnd cnd = Cnd.where("loanId","=",loanId);
        cnd.asc("nodeCode");
        return dao().query(IntermediaryApplyLoanedResult.class,cnd);
    }
}
