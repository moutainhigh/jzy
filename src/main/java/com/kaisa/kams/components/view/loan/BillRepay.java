package com.kaisa.kams.components.view.loan;

import com.kaisa.kams.enums.RiskRank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by sunwanchao on 2017/2/27.
 */
@Data
public class BillRepay extends AbsRepay{
    private String billNo;
    private Date drawTime;
    private String payer;
    private String payerId;
    private String payerAccount ;
    private RiskRank riskRank;
    private String payee;
    private String payeeAccount ;
    private String payeeBankName;
    private String bankName;
    private String bankAddress;
    private int overdueDays;
    private Date disDate;
    private int disDays;
    private Date actualDueDate;
    private BigDecimal costRate;
    private BigDecimal intermediaryFee;
    private BigDecimal minCost;
    private String depositFlag;
}
