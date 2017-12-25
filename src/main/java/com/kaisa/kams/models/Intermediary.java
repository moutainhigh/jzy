package com.kaisa.kams.models;

import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 居间人信息
 * Created by lw on 2017/6/12.
 */
@Table("sl_intermediary")
@Data
@NoArgsConstructor
public class Intermediary extends BaseModel{

    /**
     * 居间人名称
     */
    @Column("name")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String name;

    /**
     * 身份证号码
     */
    @Column("idNumber")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String idNumber;

    /**
     * 电话
     */
    @Column("phone")
    @ColDefine(type=ColType.VARCHAR,width=20)
    private String phone;

    /**
     * 家庭住址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR,width=256)
    private String address;

    /**
     * 开户行
     */
    @Column("bank")
    @ColDefine(type=ColType.VARCHAR,width=100)
    private String bank;

    /**
     * 支持多附件保存urls
     */
    @Column("contractFileUrls")
    @ColDefine(type = ColType.TEXT)
    private String contractFileUrls;

    /**
     * 账户
     */
    @Column("account")
    @ColDefine(type = ColType.VARCHAR,width = 30)
    private String account;

    /**
     * 状态
     */
    @Column("status")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private PublicStatus status;

    /**
     * 支持多附件保存urls-居间服务协议
     */
    @Column("serviceContractFileUrls")
    @ColDefine(type = ColType.TEXT)
    private String serviceContractFileUrls;

    public String account(){
        return TextFormatUtils.formatAccount(this.account);
    }

}
