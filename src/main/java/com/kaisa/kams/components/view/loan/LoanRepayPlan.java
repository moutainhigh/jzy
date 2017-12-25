package com.kaisa.kams.components.view.loan;

import com.kaisa.kams.enums.LoanRepayMethod;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunwanchao on 2016/12/12.
 */
@Data
public class LoanRepayPlan {
    private BigDecimal principal;
    private BigDecimal interest;
    private Duration duration;
    private LoanRepayMethod method;
    private List<Repayment> repayments;
    public LoanRepayPlan(BigDecimal principal,BigDecimal interest,Duration duration,LoanRepayMethod method){
        this.principal=principal;
        this.interest=interest;
        this.duration=duration;
        this.method=method;
    }
    public List<Repayment> getRepayments(){
        if(this.repayments==null){
            this.repayments = new ArrayList<>();
        }
        return this.repayments;
    }
}
