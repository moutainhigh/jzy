package com.kaisa.kams.models;

import com.kaisa.kams.enums.PublicStatus;
import org.nutz.dao.entity.annotation.*;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色
 * Created by weid on 2016/11/17.
 */
@Table("sl_role")
@Data
@NoArgsConstructor
public class Role extends BaseModel{


    /**
     * 角色名称
     */
    @Column
    @ColDefine(type = ColType.VARCHAR, width=40)
    private String name;

    @ManyMany(target = User.class, relation = "sl_roleuser", from = "role", to = "user")
    private List<User> users;

    /**
     * 有效标志
     */
    @Column("status")
    @ColDefine(type = ColType.VARCHAR, width=40)
    private PublicStatus status;

    /**
     * 角色菜单名称数组
     */
    private List<Map> menus;
}
