package com.kaisa.kams.models;

/**
 * Created by luoyj on 2017/4/17.
 */

import com.kaisa.kams.enums.PublicStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

import java.math.BigDecimal;

/**
 * Created by luoyj on 2017/4/17..
 * 自营合作方信息
 */
@Table("sl_proprietary_partner")
@Data
@NoArgsConstructor
public class ProprietaryPartner extends BaseModel{

    /**
     *  合作方简称
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    private String name;


    /**
     * 合作方全称
     */
    @Column("fullName")
    @ColDefine(type = ColType.VARCHAR,width = 60)
    private  String fullName;

    /**
     * 对接人
     */
    @Column("buttMan")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private  String buttMan;

    /**
     * 合同名称
     */
    @Column("contractFileName")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private  String contractFileName;

    /**
     * 合同地址
     */
    @Column("contractFileUrl")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private  String contractFileUrl;

    /**
     * 是否启用
     * 详见PublicStatus
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private PublicStatus status;

}
