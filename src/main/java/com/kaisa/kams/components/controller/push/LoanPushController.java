package com.kaisa.kams.components.controller.push;

import com.kaisa.kams.components.params.push.DataTableLoanPushParam;
import com.kaisa.kams.components.service.LoanSubjectService;
import com.kaisa.kams.components.service.push.LoanPushService;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.push.LoanPush;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;

/**
 * Created by pengyueyang on 2016/12/12.
 */
@IocBean
@At("/loan_push")
@Ok("json")
public class LoanPushController {

    @Inject
    private LoanPushService loanPushService;

    @Inject
    private LoanSubjectService loanSubjectService;

    @GET
    @At("/index")
    @Ok("beetl:/push/list.html")
    public Context index(){
        Context ctx = Lang.context();
        ctx.set("loanSubjectList", loanSubjectService.queryAble());
        return ctx;
    }

    @POST
    @At("/get_list")
    @AdaptBy(type = JsonAdaptor.class)
    public DataTables queryLoanPushList(@Param("..") DataTableLoanPushParam param) {
        int count = loanPushService.countByParam(param);
        DataTables dataTables = new DataTables(param.getDraw(), count, count);
        dataTables.setData(loanPushService.queryListByParam(param));
        dataTables.setOk(true);
        return dataTables;
    }

    @POST
    @At("/get_status")
    public NutMap getStatus(@Param("pushId") String pushId) {
        LoanPush loanPush = loanPushService.getLoanPush(pushId);
        NutMap result = new NutMap();
        result.put("ok",false);
        if (null == loanPush) {
            return result;
        }
        result.put("ok",true);
        result.put("loanCode",loanPush.getLoanCode());
        result.put("isComplete",loanPushService.isComplete(loanPush));
        return result;
    }

    @POST
    @At("/complete_push")
    public NutMap completePush(@Param("pushId") String pushId) {
        LoanPush loanPush = loanPushService.getLoanPush(pushId);
        NutMap result = new NutMap();
        result.put("ok",false);
        if (null == loanPush) {
            return result;
        }
        result.put("ok",true);
        result.put("completePush",loanPushService.completePush(loanPush));
        return result;
    }


}
