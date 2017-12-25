package com.kaisa.kams.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.nutz.dao.entity.annotation.ColDefine;
import org.nutz.dao.entity.annotation.ColType;
import org.nutz.dao.entity.annotation.Column;
import org.nutz.dao.entity.annotation.Table;

/**
 * 配置表
 * Created by liuwen01 on 2017/1/18.
 */
@Table("sl_config")
@Data
@NoArgsConstructor
public class Config extends BaseModel {

    /**
     * 域
     */
    @Column("nameSpace")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String nameSpace;

    /**
     * 键
     */
    @Column("name")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String name;

    /**
     * 值
     */
    @Column("val")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String val;

    /**
     * 描述
     */
    @Column("description")
    @ColDefine(type = ColType.VARCHAR,width = 300)
    private String description;

}
