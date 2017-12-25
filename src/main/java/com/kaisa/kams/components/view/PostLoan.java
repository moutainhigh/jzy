package com.kaisa.kams.components.view;

import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.ProductType;
import com.kaisa.kams.models.business.BusinessAgency;
import com.kaisa.kams.models.business.BusinessOrganize;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by liuwen01 on 2016/12/12.
 */
@Data
@NoArgsConstructor
public class PostLoan {
    /**
     * 业务单号
     */
    private String code;
    /**
     * 产品类型对象实例
     */
    private ProductType productType;
    /**
     * 申请人
     */
    private String saleName;

    /**
     * 所属组织
     */
    private BusinessOrganize organize;
    /**
     * 所属机构
     */
    private BusinessAgency agency;

    /**
     * 借款人
     */
    private Borrower masterBorrower;

    /**
     * 还款时间
     */
    private Date repayDate;

    /**
     * 需还总金额[需还本金+利息+费用] 精确到两位小数
     */
    private BigDecimal totalAmount;

    /**
     * 期数
     */
    private int period;
    /**
     * 借贷还款状态
     */
    private LoanRepayStatus status;
}
