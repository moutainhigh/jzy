package com.kaisa.kams.components.controller;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.User;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sunwanchao on 2016/12/5.
 */
@IocBean
@At("/loan_subject")
public class LoanSubjectController {
    @Inject
    private LoanSubjectService loanSubjectService;
    @At
    @Ok("beetl:/loanSubject/list.html")
    @RequiresPermissions("loanSubject:view")
    public Context list(){
        Context ctx = Lang.context();
        return ctx;
    }

    @At("/list_by_fuzz_name")
    @Ok("json")
    @POST
    @AdaptBy(type=JsonAdaptor.class)
    @RequiresPermissions("loanSubject:view")
    public Object listByFuzzName(@Param("..")DataTableParam param){
        Pager pager= DataTablesUtil.getDataTableToPager(param.getStart(),param.getLength());
        String fuzzName = "";
        if (null != param.getSearchKeys()) {
            Map<String, String> keys = param.getSearchKeys();
            fuzzName = keys.get("fuzzName");
        }
        return loanSubjectService.queryByFuzzName(fuzzName,pager,param.getDraw());
    }

    @At
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("loanSubject:delete")
    public Object delete(@Param("id") String id){
        return loanSubjectService.delete(id) > 0;
    }

    @At
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("loanSubject:create")
    public Object save(@Param("::loanSubject.")LoanSubject loanSubject,
                        @Param("accountInfo") String accountInfo){
        User u = ShiroSession.getLoginUser();
        Date now = new Date();
        loanSubject.setUpdateBy(u.getLogin());
        loanSubject.setUpdateTime(now);
        loanSubject.setCreateBy(u.getLogin());
        loanSubject.setCreateTime(now);
        return loanSubjectService.insert(loanSubject,accountInfo);
    }

    @At
    @POST
    @Ok("json")
    @Aop("auditInterceptor")
    @RequiresPermissions("loanSubject:update")
    public Object update(@Param("::loanSubject.")LoanSubject loanSubject,
                         @Param("accountInfo") String accountInfo){
        User u = ShiroSession.getLoginUser();
        Date now = new Date();
        loanSubject.setUpdateBy(u.getLogin());
        loanSubject.setUpdateTime(now);
        LoanSubject ls = loanSubjectService.fetch(loanSubject.getId());
        loanSubject.setCreateBy(ls.getCreateBy());
        loanSubject.setCreateTime(ls.getCreateTime());
        loanSubject.setId(ls.getId());
        return loanSubjectService.update(loanSubject,accountInfo);
    }

    @At("/fetch_by_id")
    @GET
    @Ok("json")
    public Object fetchById(@Param("id") String id){
        return loanSubjectService.fetch(id);
    }

    @At("/query_by_type")
    @GET
    @Ok("json")
    public Object queryByType(@Param("type") String type){
        return loanSubjectService.queryByType(type);
    }

    @At("/query_accounts_by_id")
    @GET
    @Ok("json")
    public Object queryAccountsById(@Param("id") String id){
        return loanSubjectService.querySubjectAccount(id);
    }

    @At("/fetch_by_name")
    @GET
    @Ok("json")
    public Object fetchByName(@Param("name") String name){
        NutMap result = new NutMap();
        if(StringUtils.isEmpty(name)){
            result.put("ok",false);
            result.put("msg","放款主体不能为空");
            return result;
        }

        List<LoanSubject> loanSubjectList = loanSubjectService.queryByName(name);
        result.put("ok",true);
        result.put("msg","查询成功");
        result.put("data",loanSubjectList);
        return result;
    }
}
