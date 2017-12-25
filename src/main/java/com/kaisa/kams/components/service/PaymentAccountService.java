package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.TextFormatUtils;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.BankInfo;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.PaymentAccount;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.util.List;
import java.util.Map;

/**
 * Created by sunwanchao on 2017/3/20.
 */
@IocBean(fields = "dao")
public class PaymentAccountService extends IdNameEntityService<PaymentAccount> {
    private static final Log log = Logs.get();

    public DataTables query(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            name = keys.get("s_name");
        }
        DataTables dataTables = null;
        Condition cnd=null;
        try {
            if(StringUtils.isNotEmpty(name)){
                cnd= Cnd.where("name", "like", "%" + name + "%").and("status", "=", PublicStatus.ABLE).orderBy("updateTime","desc");
            }else {
                cnd=Cnd.where("status", "=", PublicStatus.ABLE).orderBy("updateTime","desc");
            }
            //Pager pager = DataTablesUtil.getDataTableToPager(start, length);
            List<PaymentAccount> paymentAccountList = dao().query(PaymentAccount.class, cnd, pager);
            if (CollectionUtils.isNotEmpty(paymentAccountList)) {
                paymentAccountList.forEach(paymentAccount -> paymentAccount.setAccount(TextFormatUtils.formatAccount(paymentAccount.getAccount())));
            }
            dataTables = new DataTables(param.getDraw(), dao().count(PaymentAccount.class), dao().count(PaymentAccount.class, cnd), paymentAccountList);
            dataTables.setOk(true);
            dataTables.setMsg("成功");
        } catch (Exception e) {
            log.error(e.getMessage());
            dataTables = new DataTables(0, 0, 0, null);
            dataTables.setOk(false);
            dataTables.setMsg("获取数据失败.");
        }
        return dataTables;

    }

    public List<PaymentAccount> queryById(String id){
        return this.dao().query(PaymentAccount.class, Cnd.where("id","=",id).and("status", "=", PublicStatus.ABLE).orderBy("updateTime","desc"));
    }

    public boolean deleteById(String id){
        return dao().clear(PaymentAccount.class, Cnd.where("id","=",id))>0;
    }

//    public void save(List<BankInfo> bankInfoList){
//        this.dao().insert(bankInfoList);
//    }

    /**
     * 修改
     * @param paymentAccount
     * @return
     */
    public boolean update(PaymentAccount paymentAccount) {
        if (null==paymentAccount) {
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(PaymentAccount.class, "^id|createBy|createTime$")).update(paymentAccount)>0;
    }

    /**
     * 新增
     * @param paymentAccount
     * @return
     */
    public PaymentAccount add(PaymentAccount paymentAccount) {
        if(null==paymentAccount) {
            return null;
        }
        return dao().insert(paymentAccount);
    }

    /**
     * 查询
     * @param id
     * @return
     */
    public PaymentAccount fetchById(String id) {
        if(null==id) {
            return null;
        }
        return dao().fetch(PaymentAccount.class,id);
    }

    /**
     * 根据name查询
     *
     * @param name
     * @return
     */
    public List<PaymentAccount> fetchByName(String name) {
        if (null != name) {
            List<PaymentAccount> paymentAccountList= dao().query(PaymentAccount.class, Cnd.where("name","like","%"+name+"%").and("status", "=", PublicStatus.ABLE));
            paymentAccountList.forEach(paymentAccount -> paymentAccount.setAccount(TextFormatUtils.formatAccount(paymentAccount.getAccount())));
            return paymentAccountList;
        }
        return null;
    }

    /**
     * 通过account查询
     * @param account
     * @return
     */
    public PaymentAccount fetchByAccountForExit(String account) {
        if (StringUtils.isEmpty(account)){
            return null;
        }
        return dao().fetch(PaymentAccount.class,Cnd.where("account","=",account).and("status", "=", PublicStatus.ABLE));
    }

    /**
     * 通过account查询
     * @param account,id
     * @return
     */
    public PaymentAccount fetchByAccountAndIdForExit(String account,String id) {
        if (StringUtils.isEmpty(account) || StringUtils.isEmpty(id) ){
            return null;
        }
        return dao().fetch(PaymentAccount.class,Cnd.where("account","=",account).and("id","!=",id).and("status", "=", PublicStatus.ABLE));
    }
}
