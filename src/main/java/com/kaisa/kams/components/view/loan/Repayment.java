package com.kaisa.kams.components.view.loan;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Created by sunwanchao on 2016/12/12.
 */
@Data
@AllArgsConstructor
public class Repayment {
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal outstanding;
    private Date dueDate;

    private BigDecimal total;
    private int period;
    public BigDecimal getTotal() {
        return interest.add(principal);
    }
    public Repayment(BigDecimal principal, BigDecimal interest, BigDecimal outstanding, Date dueDate, int period){
        this.principal=principal;
        this.interest=interest;
        this.outstanding=outstanding;
        this.dueDate=dueDate;
        this.period=period;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.principal);
        hash = 53 * hash + Objects.hashCode(this.interest);
        hash = 53 * hash + Objects.hashCode(this.outstanding);
        hash = 53 * hash + Objects.hashCode(this.dueDate);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Repayment other = (Repayment) obj;
        if (!Objects.equals(this.principal, other.principal)) {
            return false;
        }
        if (!Objects.equals(this.interest, other.interest)) {
            return false;
        }
        if (!Objects.equals(this.outstanding, other.outstanding)) {
            return false;
        }
        return Objects.equals(this.dueDate, other.dueDate);
    }

    public String toPlainString() {
        return getPrincipal().toPlainString().concat(":")
                .concat(getInterest().toPlainString()).concat(":")
                .concat(getOutstanding().toPlainString().concat(":"))
                .concat(getDueDate().toString());
    }
}
