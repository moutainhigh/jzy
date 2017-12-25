package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.enums.historyRelationType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by luoyj on 2017/9/14.
 * 票据信息表
 */
@Table("sl_history_loan_relation")
@Data
@NoArgsConstructor
public class HistoryLoanRelation extends BaseModel {

    /**
     * loaId
     */
    @Column("loanId")
    private String loanId;
    /**
     *  关联历史loanId
     */
    @Column("relationLoanId")
    private String relationLoanId;
    /**
     *  关联历史单状态
     */
    @Column("approveStatus")
    private String approveStatus;


    private Loan loan;

    /**
     * 关联类型
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private historyRelationType type;

}
