package com.kaisa.kams.models;

import com.kaisa.kams.enums.DiscountType;
import com.kaisa.kams.enums.LoanerCertifType;
import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 借款人信息
 * Created by weid on 2016/12/12.
 */
@Table("sl_borrower")
@Data
@NoArgsConstructor
public class Borrower extends BaseModel{

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
    @ColDefine(type=ColType.VARCHAR,width=40)
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
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String phone;

    /**
     * 家庭住址
     */
    @Column("address")
    @ColDefine(width=256)
    private String address;

    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private PublicStatus status;

    /**
     * 开户银行名
     */
    @Column("bankName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String bankName;

    /**
     * 账户名
     */
    @Column("accountName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String accountName;

    /**
     * 账户
     */
    @Column("account")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String account;


    /**
     * 贴现类型
     */
    @Column("discountType")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private DiscountType discountType;

    /**
     * 支持多附件保存urls
     */
    @Column("contractFileUrls")
    @ColDefine(type = ColType.TEXT)
    private String contractFileUrls;


    /**
     * 法人姓名（保里产品专用）
     */
    @Column("legalPerson")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String legalPerson;

    /**
     * 法人身份证（保里产品专用）
     */
    @Column("legalPersonCertifNumber")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String legalPersonCertifNumber;


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
     * 公司基本情况
     */
    @Column("companyProfiles")
    @ColDefine(type=ColType.VARCHAR,width=500)
    private String companyProfiles;

}
