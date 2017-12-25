package com.kaisa.kams.components.service;

import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.LoanSubject;
import com.kaisa.kams.models.LoanSubjectAccount;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nutz.aop.interceptor.ioc.TransAop;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.aop.Aop;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.service.IdNameEntityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunwanchao on 2016/12/5.
 */
@IocBean(fields = "dao")
public class LoanSubjectService extends IdNameEntityService<LoanSubject> {

    private final static String ACCOUNT_NO_ERROR = "银行账号信息不符合要求（请输入6-30位数字）！";

    @Inject
    private LoanSubjectAccountService loanSubjectAccountService;

    @Aop(TransAop.READ_COMMITTED)
    public NutMap insert(LoanSubject loanSubject, String accountInfo){
        NutMap nutMap = new NutMap();
        String msg = this.checkLoanSubject(loanSubject);
        if(StringUtils.isNotEmpty(msg)){
            return nutMap.setv("ok",false).setv("msg",msg);
        }
        List<LoanSubjectAccount> accountList = this.assembleAccount(accountInfo);
        if (!checkSubjectAccountNo(accountList)) {
            return nutMap.setv("ok",false).setv("msg",ACCOUNT_NO_ERROR);
        }
        msg = this.checkSubjectAccount(accountList);
        if(StringUtils.isNotEmpty(msg)){
            return nutMap.setv("ok",false).setv("msg",msg);
        }
        loanSubject = this.dao().insert(loanSubject);
        for(LoanSubjectAccount account : accountList){
            account.setSubjectId(loanSubject.getId());
        }
        if(accountList==null || accountList.isEmpty()){
            return nutMap.setv("ok",true);
        }
        if(this.dao().insert(accountList)==null){
            return nutMap.setv("ok",false);
        }
        return nutMap.setv("ok",true);
    }

    private String checkLoanSubject(LoanSubject loanSubject) {
        StringBuilder msg = new StringBuilder();
        String idNumber = loanSubject.getIdNumber();
        Cnd cnd = Cnd.where("idNumber","=",idNumber);
        if(org.apache.commons.lang.StringUtils.isNotEmpty(loanSubject.getId())){
            cnd.and("id","!=",loanSubject.getId());
        }
        LoanSubject ls = this.dao().fetch(LoanSubject.class,cnd);
        if(ls!=null){
            msg.append("营业执照/身份证号").append("有重复");
        }
        String phoneNumber = loanSubject.getPhoneNumber();
        cnd = Cnd.where("phoneNumber","=",phoneNumber);
        if(StringUtils.isNotEmpty(loanSubject.getId())){
            cnd.and("id","!=",loanSubject.getId());
        }
        ls = this.dao().fetch(LoanSubject.class,cnd);
        if(ls!=null){
            msg.append("联系电话有重复");
        }
        if ( PublicStatus.DISABLED == loanSubject.getStatus() && hasLoan(loanSubject.getId())) {
            msg.append("此放款主体关联正在流转的业务申请，不能设置失效");
        }

        return msg.toString();
    }

    private String checkSubjectAccountForUpdate(List<LoanSubjectAccount> subjectAccountList,String loanSubjectId){
        StringBuilder msg = new StringBuilder();
        List<String> accountNoList = getRepeatAccountNo(subjectAccountList, msg);
        if(StringUtils.isNotEmpty(msg)){
            return msg.toString();
        }
        for(String l:accountNoList){
            LoanSubjectAccount account = this.dao().fetch(LoanSubjectAccount.class,Cnd.where("accountNo","=",l).and("subjectId","<>",loanSubjectId));
            if(account!=null){
                msg.append("帐号").append(l).append("有重复");
            }
        }
        return msg.toString();
    }

    private List<String> getRepeatAccountNo(List<LoanSubjectAccount> subjectAccountList, StringBuilder msg) {
        List<String> accountNoList = new ArrayList<>();
        Map<String,List<LoanSubjectAccount>> no2Map = subjectAccountList.stream().collect(Collectors.groupingBy(l->l.getAccountNo()));
        for(Map.Entry<String,List<LoanSubjectAccount>> entry : no2Map.entrySet()){
            accountNoList.add(entry.getKey());
            if(entry.getValue().size()>1){
                msg.append("帐号").append(entry.getKey()).append("有重复");
            }
        }
        return accountNoList;
    }

    private String checkSubjectAccount(List<LoanSubjectAccount> subjectAccountList){
        StringBuilder msg = new StringBuilder();
        List<String> accountNoList = getRepeatAccountNo(subjectAccountList, msg);
        if(StringUtils.isNotEmpty(msg)){
            return msg.toString();
        }
        for(String l:accountNoList){
            LoanSubjectAccount account = this.dao().fetch(LoanSubjectAccount.class,Cnd.where("accountNo","=",l));
            if(account!=null){
                msg.append("帐号").append(l).append("有重复");
            }
        }
        return msg.toString();
    }

    private boolean checkSubjectAccountNo(List<LoanSubjectAccount> subjectAccountList) {
        for (LoanSubjectAccount account:subjectAccountList) {
            if (!checkSubjectAccountNoLength(account)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkSubjectAccountNoLength(LoanSubjectAccount account) {
        String accountNo = account.getAccountNo();
        if (null!=accountNo && accountNo.length()>=6 && accountNo.length()<=30) {
            return true;
        }
        return false;
    }

    public List<LoanSubjectAccount> assembleAccount(String accountInfo){
        List<LoanSubjectAccount> accountList = Json.fromJsonAsList(LoanSubjectAccount.class,accountInfo);
        if (CollectionUtils.isNotEmpty(accountList)) {
            int position = 0;
            for (LoanSubjectAccount account:accountList) {
                account.setPosition(position++);
            }
        }
        return accountList;
    }
    public DataTables query(String fuzzName,Pager pager,int draw){
        Condition cnd = Cnd.where("name","like","%"+fuzzName+"%").orderBy("updateTime","desc");
        List<LoanSubject> resultList = this.query(cnd,pager);

        List<LoanSubjectAccount> accountList = loanSubjectAccountService.query();
        Map<String,List<LoanSubjectAccount>> subjectId2Map = accountList.stream().collect(Collectors.groupingBy(l->l.getSubjectId()));
        resultList.stream().forEach(l->{
            if(subjectId2Map.containsKey(l.getId())){
                l.setAccounts(subjectId2Map.get(l.getId()));
            }
        });
        return new DataTables(draw,this.dao().count(LoanSubject.class),this.dao().count(LoanSubject.class,cnd),resultList);
    }
    public int delete(String id){
        super.delete(id);
        return this.dao().clear(LoanSubjectAccount.class,Cnd.where("subjectId","=",id));
    }

    public LoanSubject fetch(String id){
        LoanSubject result = super.fetch(id);
        if(result!=null){
            result.setAccounts(loanSubjectAccountService.queryFormatAccountsBySubjectId(result.getId()));
        }
        return result;
    }

    @Aop(TransAop.READ_COMMITTED)
    public NutMap update(LoanSubject loanSubject, String accountInfo) {
        NutMap nutMap = new NutMap();
        String msg = checkLoanSubject(loanSubject);
        if(StringUtils.isNotEmpty(msg)){
            return nutMap.setv("ok",false).setv("msg",msg);
        }
        this.dao().update(loanSubject);
        List<LoanSubjectAccount> accountList = this.assembleAccount(accountInfo);
        if (!checkSubjectAccountNo(accountList)) {
            return nutMap.setv("ok",false).setv("msg",ACCOUNT_NO_ERROR);
        }
        msg = this.checkSubjectAccountForUpdate(accountList,loanSubject.getId());
        if(StringUtils.isNotEmpty(msg)){
            return nutMap.setv("ok",false).setv("msg",msg);
        }
        this.dao().clear(LoanSubjectAccount.class,Cnd.where("subjectId","=",loanSubject.getId()));
        for(LoanSubjectAccount account : accountList){
            account.setSubjectId(loanSubject.getId());
        }
        if(accountList==null || accountList.isEmpty()){
            return nutMap.setv("ok",true);
        }
        this.dao().insert(accountList);
        return nutMap.setv("ok",true);
    }

    public Object queryByFuzzName(String fuzzName,Pager pager,int draw) {
        return this.query(fuzzName,pager,draw);
    }

    /**
     * 查询有效的放款主体
     * @return
     */
    public List<LoanSubject> queryAble() {
        return dao().query(LoanSubject.class,Cnd.where("status","=", PublicStatus.ABLE));
    }

    /**
     * 根据名字查询有效的放款主体
     * @return
     */
    public List<LoanSubject> queryByName(String name) {
        return dao().query(LoanSubject.class,Cnd.where("status","=", PublicStatus.ABLE).and("name","like", "%" + name + "%"));
    }

    public Object queryByType(String type) {
        return this.dao().query(LoanSubject.class,Cnd.where("type","=",type).and("status","=",PublicStatus.ABLE));
    }

    public Object querySubjectAccount(String id){
        return loanSubjectAccountService.queryFormatAccountsBySubjectId(id);
    }

    public String getLoanSubjectName(String id){
        LoanSubject loanSubject = fetch(id);
        if(loanSubject!=null){
            return loanSubject.getName();
        }
        return "";
    }

    private boolean hasLoan(String loanSubjectId) {
        Sql sql = Sqls.create("select id from sl_loan where loanSubjectId = @loanSubjectId and loanStatus in ('SAVE','SUBMIT','APPROVEEND') limit 1");
        sql.params().set("loanSubjectId",loanSubjectId);
        sql.setCallback(Sqls.callback.str());
        dao().execute(sql);
        String loanId = sql.getString();
        return StringUtils.isNotEmpty(loanId);
    }
}
