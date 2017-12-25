package com.kaisa.kams.models;

import com.kaisa.kams.components.security.ShiroSession;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.nutz.dao.entity.annotation.*;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


import lombok.Data;


/**
 * Created by pengyueyang on 2016/11/24.
 */
@Data
@PK("id")
public class BaseModel implements Serializable{

    public String uuid(){
        return UUID.randomUUID().toString();
    }


    @Prev(els=@EL("$me.uuid()"))
    private String id;
    /**
     * 创建人
     */
    @Column("createBy")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String createBy;

    /**
     * 创建时间
     */
    @Column("createTime")
    private Date createTime;

    /**
     * 更新人
     */
    @Column("updateBy")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String updateBy;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private Date updateTime;

    /**
     * 创建人ID
     */
    @Prev(els=@EL("$me.loginUserId()"))
    @Column("createrId")
    @ColDefine(type = ColType.VARCHAR,width = 40)
    private String createrId;

    public String loginUserId(){
        try{
            SecurityUtils.getSecurityManager();
            return ShiroSession.getLoginUser()!=null?ShiroSession.getLoginUser().getId():"system";
        }catch (UnavailableSecurityManagerException e){
            return "system";
        }
    }

    public void updateOperator(){
        if(StringUtils.isEmpty(this.createBy)){
            this.setCreateBy(ShiroSession.getLoginUser().getName());
            this.setCreateTime(new Date());
        }
        this.setUpdateBy(ShiroSession.getLoginUser().getName());
        this.setUpdateTime(new Date());

    }
}
