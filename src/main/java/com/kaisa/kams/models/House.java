package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouchuang on 2017/9/15.
 */
@Table("sl_house")
@Data
@NoArgsConstructor
public class House extends BaseModel{

    @Column("housePropertyNumber")
    @Comment("房产证号")
    @ColDefine(type = ColType.VARCHAR,width = 128)
    private String housePropertyNumber;


    @Column("address")
    @Comment("地址")
    @ColDefine(type = ColType.VARCHAR,width = 128)
    private String address;


    @Column("area")
    @Comment("面积")
    @ColDefine(type = ColType.VARCHAR,width = 16)
    private String area;

    private List<EquityHolder> equityHolderList;
    public void addEquityHolderList(EquityHolder equityHolder){
        if(equityHolderList==null){
            equityHolderList  = new ArrayList<EquityHolder>();
        }
        equityHolderList.add(equityHolder);
    }
}
