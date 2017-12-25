package com.kaisa.kams.models;

import com.kaisa.kams.enums.HolderIdentityType;
import com.kaisa.kams.enums.LoanerCertifType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Table("sl_equity_holder")
@Data
@NoArgsConstructor
public class EquityHolder extends BaseModel{



    @Column("houseId")
    @Comment("房产证ID")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String houseId;


    @Column("name")
    @Comment("权益人姓名")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private String name;


    @Column("homeAddress")
    @Comment("家庭地址")
    @ColDefine(type = ColType.VARCHAR,width = 128)
    private String homeAddress;

    @Column("certifType")
    @Comment("证件类型")
    @ColDefine(type=ColType.VARCHAR,width=40)
    public LoanerCertifType certifType;

    public String getCertifTypeCN(){
        return certifType.getDescription();
    }


    @Column("certificateNo")
    @Comment("证件号")
    @ColDefine(type = ColType.VARCHAR,width = 64)
    private String certificateNo;


    @Column("holderIdentity")
    @Comment("权益人身份")
    @ColDefine(type=ColType.VARCHAR,width=40)
    private HolderIdentityType holderIdentity;

    @Column("sortNo")
    @Comment("排序")
    private int sortNo;

}