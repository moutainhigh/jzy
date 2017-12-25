package com.kaisa.kams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by sunwanchao on 2017/1/4.
 */
@Getter
@AllArgsConstructor
public enum OperationType {

    LOAN_CONFIRM("放款确认"),

    LOAN_REPAY_INSERT("生成还款计划"),
    LOAN_REPAY_CLEAR("还请本期还款计划"),

    LOAN_REPAY_RECORD_INSERT("新增还款记录"),
    LOAN_REPAY_RECORD_DELETE("删除还款记录"),


    LOAN_SUBJECT_INSERT("新增放款主体"),
    LOAN_SUBJECT_UPDATE("修改放款主体"),
    LOAN_SUBJECT_DELETE("删除放款主体"),

    BUSINESS_USER_INSERT("新增业务人员"),
    BUSINESS_USER_UPDATE("修改业务人员"),

    BUSINESS_ORGANIZE_INSERT("新增业务组织"),
    BUSINESS_ORGANIZE_UPDATE("修改业务组织"),

    BUSINESS_AGENCY_INSERT("新增业务机构"),
    BUSINESS_AGENCY_UPDATE("修改业务机构"),

    ROLE_INSERT("新增角色信息"),
    ROLE_UPDATE("修改角色信息"),
    ROLE_DELETE("删除角色信息"),
    ROLE_UPDATE_USER("维护角色用户信息"),
    ROLE_DELETE_MENU("删除角色菜单权限"),

    USER_INSERT("新增用户信息"),
    USER_UPDATE("修改用户信息"),
    USER_FIND_PW("找回密码"),
    USER_RESET_PW("重置密码"),

    MENU_INSERT("新增菜单信息"),
    MENU_UPDATE("修改菜单信息"),
    MENU_DELETE("删除菜单信息"),

    MEDIA_TEMP_INSERT("新增影像配置"),
    MEDIA_TEMP_UPDATE("修改影像配置"),

    LOAN_PUSH_ADD("推单"),
    LOAN_PUSH_END("推单完成"),

    SEAL_USED("确认用印");


    private String description;
}
