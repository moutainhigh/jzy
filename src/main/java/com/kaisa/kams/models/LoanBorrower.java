package com.kaisa.kams.models;


import com.kaisa.kams.enums.LoanerCertifType;
import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

/**
 * 贷款借款人信息表
 * Created by weid on 2016/12/12.
 */
@Table("sl_loan_borrower")
@Data
@NoArgsConstructor
@TableIndexes({ @Index(name = "INDEX_LOAN_ID", fields = { "loanId" }, unique = false) })
public class LoanBorrower extends BaseModel {

    /**
     * 借款人姓名
     */
    @Column("name")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String name;

    /**
     * 证件类型
     */
    @Column("certifType")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private LoanerCertifType certifType;

    /**
     * 证件号码
     */
    @Column("certifNumber")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String certifNumber;

    /**
     * 手机号码
     */
    @Column("phone")
    @ColDefine(type = ColType.VARCHAR,width=20)
    private String phone;

    /**
     * 家庭住址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR,width=256)
    private String address;

    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width=40)
    private PublicStatus status;

    /**
     * 主借Id
     */
    @Column("loanId")
    private String loanId;

    /**
     * 是否为主借款人
     */
    @Column("master")
    @ColDefine(type = ColType.BOOLEAN)
    private boolean master;

    /**
     * 借款人Id
     */
    @Column("borrowerId")
    private String borrowerId;


    /**
     * 法定代表人
     */
    @Column("legalRepresentative")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String legalRepresentative;

    /**
     * 法定代表人联系方式
     */
    @Column("legalRepresentativePhone")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String legalRepresentativePhone;

    /**
     * 联系人
     */
    @Column("linkman")
    @ColDefine(type=ColType.VARCHAR,width=50)
    private String linkman;

    /**
     * 联系人电话
     */
    @Column("linkmanPhone")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String linkmanPhone;

    /**
     * 住址
     */
    @Column("residence")
    @ColDefine(type=ColType.VARCHAR,width=200)
    private String residence;




    /**
     * 通过借款人构造
     * @param borrower
     */
    public LoanBorrower(Borrower borrower){
        if(null!=borrower) {
            this.name = borrower.getName();
            this.address = borrower.getAddress();
            this.certifNumber = borrower.getCertifNumber();
            this.certifType = borrower.getCertifType();
            this.phone = borrower.getPhone();
            this.borrowerId = borrower.getId();
        }
    }

}
