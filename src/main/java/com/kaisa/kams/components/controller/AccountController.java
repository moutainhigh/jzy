package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.PaymentAccountService;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.PaymentAccount;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by liuwen01 on 2017/4/12.
 */
@IocBean
@At("/account")
public class AccountController {

    private static final Log log = Logs.get();

    @Inject
    private PaymentAccountService paymentAccountService;

    /**
     * 跳转到回款账户页面
     *
     * @return
     */
    @At
    @Ok("beetl:/paymentAccount/list.html")
    @RequiresPermissions("account:view")
    public Context list() {
        Context ctx = Lang.context();
        return ctx;
    }


    @At("/account_list")
    @Ok("json")
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("account:view")
    public Object list(@Param("..")DataTableParam param) {
        //Pager pager = DataTablesUtil.getDataTableToPager(start, length);
        return paymentAccountService.query(param);
    }

    /**
     * 编辑跳转方法
     * @param id
     * @return
     */
    @At("/edit_init")
    @Ok("json")
    @POST
    @RequiresPermissions("account:update")
    public Object editInit(@Param("id") String id) {
        NutMap result = new NutMap();
        try {
            PaymentAccount paymentAccount = paymentAccountService.fetchById(id);
            result.put("paymentAccount", paymentAccount);
            result.put("ok", true);
        }catch (Exception e) {
            result.put("ok", false);
            result.put("msg", "获取数据异常！");
        }
        return result;
    }

    /**
     * 修改账户
     */
    @At("/update_account")
    @POST
    @Ok("json")
    @RequiresPermissions("account:update")
    public Object update(@Param("..") PaymentAccount paymentAccount) {

        NutMap result = new NutMap();
        if (null == paymentAccount) {
            result.put("ok", false);
            result.put("msg", "回款账户信息错误");
            return result;
        }

        paymentAccount.setUpdateBy(ShiroSession.getLoginUser().getName());
        paymentAccount.setUpdateTime(new Date());
        PaymentAccount exitPaymentAccount = paymentAccountService.fetchByAccountAndIdForExit(paymentAccount.getAccount(),paymentAccount.getId());
        if(null != exitPaymentAccount){
            result.put("ok", false);
            result.put("msg", "回款账号不能重复");
            return result;
        }
        //修改数据
        boolean flag = paymentAccountService.update(paymentAccount);


        if (flag) {
            result.put("ok", true);
            result.put("msg", "修改回款账户信息成功");
        } else {
            result.put("ok", false);
            result.put("msg", "修改回款账户信息失败");
        }
        return result;
    }

    /**
     * 添加回款账户
     */
    @At("/add_account")
    @POST
    @Ok("json")
    @RequiresPermissions("account:create")
    public Object add(@Param("..") PaymentAccount paymentAccount) {

        NutMap result = new NutMap();
        paymentAccount.setUpdateBy(ShiroSession.getLoginUser().getName());
        paymentAccount.setUpdateTime(new Date());
        paymentAccount.setCreateBy(ShiroSession.getLoginUser().getName());
        paymentAccount.setCreateTime(new Date());
        paymentAccount.setStatus(PublicStatus.ABLE);
        PaymentAccount exitPaymentAccount = paymentAccountService.fetchByAccountForExit(paymentAccount.getAccount());
        if(null != exitPaymentAccount){
            result.put("ok", false);
            result.put("msg", "回款账号不能重复");
            return result;
        }
        //新增数据
        paymentAccount = paymentAccountService.add(paymentAccount);


        if (null != paymentAccount) {
            result.put("ok", true);
            result.put("msg", "添加回款账户信息成功");
        } else {
            result.put("ok", false);
            result.put("msg", "添加回款账户信息失败");
        }
        return result;
    }

    /**
     * 根据名称查询回款账户
     * @param name
     * @return
     */
    @At("/query_account_by_name")
    @POST
    @Ok("json")
    public Object queryByName(@Param("name")String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","回款账户名称不能为空");
            return result;
        }

        List<PaymentAccount> paymentAccountList = paymentAccountService.fetchByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",paymentAccountList);
        return result;
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @At("/delete_account")
    @POST
    @Ok("json")
    public Object delete(@Param("id")String id){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(id)){
            result.put("ok",false);
            result.put("msg","回款账户不能为空");
            return result;
        }
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setId(id);
        paymentAccount.setUpdateBy(ShiroSession.getLoginUser().getName());
        paymentAccount.setUpdateTime(new Date());
        paymentAccount.setCreateBy(ShiroSession.getLoginUser().getName());
        paymentAccount.setCreateTime(new Date());
        paymentAccount.setStatus(PublicStatus.DISABLED);
        Boolean flag = paymentAccountService.update(paymentAccount);
        if(flag){
            result.put("ok",true);
            result.put("msg","删除成功");
            return result;
        }else {
            result.put("ok",false);
            result.put("msg","删除失败");
            return result;
        }

    }
}
