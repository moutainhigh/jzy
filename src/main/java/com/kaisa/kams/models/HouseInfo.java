package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * Created by lw on 2017/4/21.
 * 房产信息表
 */
@Table("sl_house_info")
@Data
@NoArgsConstructor
public class HouseInfo extends BaseModel{
    @Column("loanId")
    private String loanId;

    /**
     * 房产证号
     */
    @Column("code")
    @ColDefine(type = ColType.VARCHAR, width=128)
    private String code;

    /**
     * 权属人
     */
    @Column("ower")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String ower;

    /**
     * 房产面积
     */
    @Column("area")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String area;

    /**
     * 房产地址
     */
    @Column("address")
    @ColDefine(type = ColType.VARCHAR,width = 200)
    private String address;

    /**
     * 房产估值（万元）
     */
    @Column("price")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String price;

    /**
     * 估值渠道
     */
    @Column("channel")
    @ColDefine(type = ColType.VARCHAR,width = 100)
    private String channel;

    /**
     * 顺序位置
     */
    @Column("position")
    @ColDefine(type = ColType.INT,width = 10)
    private int position;


    /**
     * 房产名称
     */
    @Column("houseName")
    @ColDefine(type = ColType.VARCHAR,width = 100)
    private String houseName;

    /**
     * 与借款人关系
     */
    @Column("relation")
    @ColDefine(type = ColType.VARCHAR,width = 20)
    private String relation;
    public String relation(){
        return "1".equals(this.relation)?"本人":"";
    }

    /**
     * 与借款人其他关系
     */
    @Column("relationElse")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String relationElse;

    /**
     * 权属人id以逗号隔开（A,B,C）
     */
    @Column("owerId")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String owerId;

    /**
     * 权证号或者登记编号
     */
    @Column("warrantNumber")
    @ColDefine(type = ColType.VARCHAR,width = 50)
    private String warrantNumber;

    /**
     * 权属人jsonStr（A,B,C）
     */
    @Column("owerStr")
    @ColDefine(type = ColType.VARCHAR,width = 256)
    private String owerStr;

    private List<Map>  owerList;

    /**
     * 估值系数
     */
    @Column("budgetPercentage")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String budgetPercentage ;

    /**
     * 估值金额（万元）
     */
    @Column("budgetPrice")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String budgetPrice;


    /**
     * 房产估值总金额（万元）
     */
    @Column("budgetAllPrice")
    @ColDefine(type = ColType.VARCHAR, width=32)
    private String budgetAllPrice;

}
