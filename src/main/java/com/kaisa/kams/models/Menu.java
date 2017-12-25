package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;

import org.apache.commons.collections.CollectionUtils;
import org.nutz.dao.entity.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单
 * Created by weid on 2016/11/17.
 */
@Table("sl_menu")
@Data
@NoArgsConstructor
public class Menu extends BaseModel{

    /**
     * 菜单名称
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    /**
     * 菜单别名  alias可以为菜单别名，也可以为按钮的别名。 按钮别名以has***格式，如 hasAdd,hasFlow,hasApproval等格式。
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String alias;

    /**
     * 菜单Url
     */
    @Column("url")
    @ColDefine(type = ColType.VARCHAR, width=256)
    private String url;

    /**
     * 当前菜单深度
     */
    @Column("depth")
    @ColDefine(type = ColType.VARCHAR, width=1)
    private String depth;


    /**
     * 菜单排序
     */
    @Column("draworder")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String draworder;

    /**
     * 父级菜单Id
     */
    @Column("parentId")
    private  String parentId;

    /**
     * 父级菜单实例对象
     */
    @One(target = Menu.class, field ="parentId" )
    private Menu parent;

    /**
     * 有效标志
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 平台标记 PC/H5
     */
    @Column("platform")
    @ColDefine(type = ColType.VARCHAR, width=10)
    private String platform;
    /**
     *
     * 什么类型 01：菜单类型，02：按钮类型 03：tab类型
     * */
    @Column("type")
    @ColDefine(type = ColType.VARCHAR, width=2)
    private String type ;



    private List<Menu> buttonList;
    public void addButton(Menu menu){
        if(buttonList==null){
            buttonList  = new ArrayList<>();
        }
        buttonList.add(menu);
    }

    public String getButtonPermissions(){
        if(CollectionUtils.isNotEmpty(buttonList)){
            StringBuffer stringBuffer  = new StringBuffer(",");
            for(Menu button : buttonList){
                stringBuffer.append(button.getAlias()+",");
            }
            return stringBuffer.toString();
        }
        return "";
    }
}
