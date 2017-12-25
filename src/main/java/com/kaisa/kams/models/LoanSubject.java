package com.kaisa.kams.models;

import com.kaisa.kams.enums.LoanSubjectType;
import com.kaisa.kams.enums.PublicStatus;

import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Many;
import org.nutz.dao.entity.annotation.Table;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2016/11/24.
 * 放款主体表
 */
@Table("sl_loan_subject")
@Data
@NoArgsConstructor
public class LoanSubject extends BaseModel{

    /**
     * 放款主体类型 企业和个人
     */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private LoanSubjectType type;

    /**
     * 放款主体名称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String name;

    /**
     * 身份证号/营业执照 放款主体类型选择企业时显示营业执照，个人显示身份证e
     */
    @Column("idNumber")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String idNumber;

    public String idType(){
        if(LoanSubjectType.ENTERPRISE.equals(type)){
            return "营业执照";
        }else if(LoanSubjectType.PERSONAL.equals(type)){
            return "身份证";
        }else{
            return "";
        }
    }

    /**
     * 手机号码
     */
    @Column("phoneNumber")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String phoneNumber;

    /**
     * 住址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR,width = 254)
    private String address;

    /**
     * 放款主体账户
     */
    @Many(target = LoanSubjectAccount.class, field = "subjectId")
    private List<LoanSubjectAccount> accounts;

    /**
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;
}
