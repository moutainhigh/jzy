package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.excelUtil.Condition;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.business.BusinessUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Table("sl_mortgage")
@Data
@NoArgsConstructor
public class Mortgage extends BaseModel{

    @Comment("关联的产品ID")
    @Column("productId")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String productId;

    @Condition(condition = "LIKE",sql="%{}%")
    @Column("mortgageCode")
    @Comment("房产抵押编号")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String mortgageCode;

    @Condition(condition = "LIKE",sql="%{}%")
    @Column("channel")
    @Comment("业务类型")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private ChannelType channel;

    private String businessSource;
    public String businessSource(){
        if(this.businessSource.startsWith("渠道")){
            return this.businessSource.replace("渠道|","");
        }else{
            return this.businessSource.substring(this.businessSource.lastIndexOf("-")+1,this.businessSource.length());
        }
    }

    @Comment("业务员ID")
    @Column("businessId")
    @ColDefine(type = ColType.VARCHAR, width=64)
    private String businessId;

    @Condition(condition = "LIKE",sql="%{}%")
    @Column("businessName")
    @Comment("业务员名字")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String businessName;

    @One(target = BusinessUser.class, field = "businessId")
    private BusinessUser businessUser;

//    @One(target = Channel.class, field = "businessId")
//    private Channel channelInfo;

    @Column("houseMortgageType")
    @Comment("房产抵押地")
    @ColDefine(type = ColType.VARCHAR,width = 32)
    private HouseMortgageType houseMortgageType;

    @Comment("放款主体")
    @Column("loanSubjectId")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String loanSubjectId;

    @One(target = LoanSubject.class, field = "loanSubjectId")
    private LoanSubject loanSubject;

    private String loanSubjectName;

    private String equityHolder ;

    @Comment("状态")
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 8)
    private PublicStatus status;

    @Comment("审批状态")
    @Column("approvalStatusType")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private ApprovalStatusType approvalStatusType;

//    @Comment("业务审批状态")
//    @Column("approvalType")
//    @ColDefine(type = ColType.VARCHAR,width = 16)
//    private ApprovalType approvalType;

    @Comment("审批状态")
    @Column("approveStatus")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatus;
    /**
     * 审批状态描述
     */
    @Comment("审批状态描述")
    @Column("approveStatusDesc")
    @ColDefine(type = ColType.VARCHAR, width=120)
    private String approveStatusDesc;


   /* @Comment("借款人名字")
    @Column("borrowerName")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private String borrowerName;

    @Comment("借款人ID")
    @Column("borrowerId")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String borrowerId;


    @One(target =Borrower.class,field ="borrowerId" )
    private Borrower borrower;*/


    private List<MortgageHouse> mortgageHouseList;

    public void addMortgageHouseList(MortgageHouse mortgageHouse){
        if(mortgageHouseList==null){
            mortgageHouseList  = new ArrayList<>();
        }
        mortgageHouseList.add(mortgageHouse);
    }

    public void removeMortgageHouseList(MortgageHouse mortgageHouse){
        mortgageHouseList.remove(mortgageHouse);
    }
}
