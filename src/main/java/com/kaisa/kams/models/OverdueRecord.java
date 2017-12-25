package com.kaisa.kams.models;

import com.kaisa.kams.enums.LoanForm;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lw on 2017/12/8.
 */
@Table("sl_overdue_record")
@Data
@NoArgsConstructor
public class OverdueRecord extends BaseModel{

    @Column("loanId")
    @Comment("标的id")
    private String loanId;

    @Column("repayId")
    @Comment("还款计划id")
    private String repayId;

    @Column("loanForm")
    @Comment("贷款形式")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private LoanForm loanForm;


    @Column("overdueReason")
    @Comment("逾期原因")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String overdueReason;


    @Column("trackingRecord")
    @Comment("跟踪记录")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String trackingRecord;

    @Column("position")
    @Comment("位置")
    @ColDefine(type = ColType.INT)
    private int position;

}
