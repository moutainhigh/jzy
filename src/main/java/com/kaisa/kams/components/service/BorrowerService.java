package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.enums.DiscountType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Intermediary;
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
 * Created by weid on 2016/12/12.
 */
@IocBean(fields="dao")
public class BorrowerService extends IdNameEntityService<Borrower> {
    private static final Log log = Logs.get();

    /**
     * 新增借款人
     * @param borrower
     * @return
     */
    public Borrower add(Borrower borrower) {
        if (null==borrower){
            return null;
        }
        return dao().insert(borrower);
    }

    /**
     * 通过证件号码查询
     * @param certifNumber
     * @return
     */
    public Borrower fetchByCertifNumber(String certifNumber) {
        if (StringUtils.isEmpty(certifNumber)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("certifNumber","=",certifNumber).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过证件号码和证件类型查询
     * @param certifNumber
     * @return
     */
    public Borrower fetchByCertifNumberAndcertifType(String certifNumber,String certifType) {
        if (StringUtils.isEmpty(certifNumber)||StringUtils.isEmpty(certifType)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("certifNumber","=",certifNumber).and("certifType","=",certifType).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过证件号码查询(去掉状态限制)
     * @param certifNumber
     * @return
     */
    public Borrower fetchByCertifNumberNotatus(String certifNumber) {
        if (StringUtils.isEmpty(certifNumber)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("certifNumber","=",certifNumber));
    }

    public Borrower fetchByCertifyNumberAndId(String certifNumber,String id) {
        if (StringUtils.isEmpty(certifNumber) || StringUtils.isEmpty(id)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("certifNumber","=",certifNumber).and("id","!=",id));
    }

    /**
     * 通过名称查询(去重)
     * @param name
     * @return
     */
    public Borrower fetchByName(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("name","=",name).and("status","=", PublicStatus.ABLE));
    }


    /**
     * 通过证件号码查询
     * @param certifNumber
     * @return
     */
    public Borrower fetchByCertifNumberAndId(String certifNumber,String id) {
        if (StringUtils.isEmpty(certifNumber) || StringUtils.isEmpty(id)){
            return null;
        }
        return dao().fetch(Borrower.class,Cnd.where("certifNumber","=",certifNumber).and("status","=", PublicStatus.ABLE).and("id","!=",id));
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public Borrower fetchById(String id) {
        return dao().fetch(Borrower.class,Cnd.where("id","=",id).and("status","=", PublicStatus.ABLE));
    }

    public Borrower fetchById1(String id) {
        return dao().fetch(Borrower.class,Cnd.where("id","=",id));
    }

    /**
     * 修改
     * @param borrower
     */
    public boolean updateDiscount(Borrower borrower) {
        if(null==borrower){
            return false;
        }
        int flag = dao().update(borrower,"^(name|certifType|certifNumber|phone|address|bankName|accountName|account|discountType|contractFileUrls|updateBy|updateTime|status|legalRepresentative|legalRepresentativePhone|linkman|linkmanPhone|residence|companyProfiles)$");
        return flag>0;
    }
    /**
     * 修改
     * @param borrower
     */
    public boolean update(Borrower borrower) {
        if(null==borrower){
            return false;
        }
        int flag = dao().update(borrower,"^(name|certifType|certifNumber|phone|address|legalPerson|legalPersonCertifNumber|companyProfiles)$");
        return flag>0;
    }


    /**
     * 通过名称查询
     * @param name
     * @return
     */
    public List<Borrower> queryByName(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        return dao().query(Borrower.class,Cnd.where("name","like","%"+name+"%").and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过名称和贴现类型查询
     * @param name,discountType
     * @return
     */
    public List<Borrower> queryByNameAndType(String name, DiscountType discountType) {
        if (StringUtils.isEmpty(name) && null != discountType){
            return null;
        }
        if(ShiroSession.getLoginUser().getType().equals(ChannelUserType.CHANNEL_USER)){
            return dao().query(Borrower.class,Cnd.where("name","=",name).and("status","=", PublicStatus.ABLE).and("discountType","=",discountType));
        }else {
            return dao().query(Borrower.class,Cnd.where("name","like","%"+name+"%").and("status","=", PublicStatus.ABLE).and("discountType","=",discountType));
        }
    }

    /**
     * 通过证件号查询
     * @param certifNumber
     * @return
     */
    public List<Borrower> queryListByCertifNumber(String certifNumber) {
        return dao().query(Borrower.class,Cnd.where("certifNumber","like","%"+certifNumber+"%").and("status","=", PublicStatus.ABLE));
    }


    /**
     * 贴现企业查询列表
     * @param param
     * @return
     */
    public DataTables queryBorrower(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            name = keys.get("name");
        }
        DataTables dataTables;
        Condition cnd;
        try {
            if(StringUtils.isNotEmpty(name)){
                cnd= Cnd.where("name", "like", "%" + name + "%").and("discountType", "=", "DISCOUNT_COMPANY").orderBy("createTime","desc");
            }else {
                cnd=Cnd.where("discountType", "=", "DISCOUNT_COMPANY").orderBy("createTime","desc");
            }
            List<Borrower> borrowers = dao().query(Borrower.class, cnd, pager);
            dataTables = new DataTables(param.getDraw(), dao().count(Borrower.class), dao().count(Borrower.class, cnd), borrowers);
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

    /**
     * 居间人查询列表
     * @param param
     * @return
     */
    public DataTables query(DataTableParam param){
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String name = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            name = keys.get("name");
        }
        DataTables dataTables;
        Condition cnd;
        try {
            if(StringUtils.isNotEmpty(name)){
                cnd= Cnd.where("name", "like", "%" + name + "%").orderBy("name","desc");
            }else {
                cnd=Cnd.where("1", "=", "1").orderBy("createTime","desc");
            }
            List<Intermediary> intermediaryList = dao().query(Intermediary.class, cnd, pager);
            dataTables = new DataTables(param.getDraw(), dao().count(Intermediary.class), dao().count(Intermediary.class, cnd), intermediaryList);
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


    /**
     * 新增居间人
     * @param intermediary
     * @return
     */
    public Intermediary add(Intermediary intermediary) {
        if (null==intermediary){
            return null;
        }
        return dao().insert(intermediary);
    }

    /**
     * 修改居间人
     * @param intermediary
     */
    public boolean updateIntermediary(Intermediary intermediary) {
        if(null==intermediary){
            return false;
        }
        int flag = dao().update(intermediary,"^(name|idNumber|bank|account|phone|address|contractFileUrls|updateBy|updateTime|status)$");
        return flag>0;
    }

    /**
     * 修改居间人
     * @param intermediary
     */
    public boolean updateIntermediaryForIntermediary(Intermediary intermediary) {
        if(null==intermediary){
            return false;
        }
        //忽略少数字段更新
        return Daos.ext(dao(), FieldFilter.locked(Intermediary.class, "^id|createBy|createTime|status|contractFileUrls$")).update(intermediary)>0;
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public Intermediary fetchIntermediaryById(String id) {
        return dao().fetch(Intermediary.class,Cnd.where("id","=",id).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public Intermediary fetchIntermediaryById1(String id) {
        return dao().fetch(Intermediary.class,Cnd.where("id","=",id));
    }

    /**
     * 通过loanId查找
     * @param loanId
     * @return
     */
    public Intermediary fetchByLoanId(String loanId) {
        return dao().fetch(Intermediary.class,Cnd.where("loanId","=",loanId).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过名称查询
     * @param name
     * @return
     */
    public List<Intermediary> queryIntermediaryByName(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        return dao().query(Intermediary.class,Cnd.where("name","like","%"+name+"%").and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过名称查询
     * @param name
     * @return
     */
    public Intermediary fetchByname(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("name","=",name).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过证件号码查询
     * @param IdNumber
     * @return
     */
    public Intermediary fetchByIdNumber(String IdNumber) {
        if (StringUtils.isEmpty(IdNumber)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("idNumber","=",IdNumber).and("status","=", PublicStatus.ABLE));
    }

    /**
     * 通过证件号码查询-noStatus
     * @param IdNumber
     * @return
     */
    public Intermediary fetchByIdNumberNoStatus(String IdNumber) {
        if (StringUtils.isEmpty(IdNumber)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("idNumber","=",IdNumber));
    }

    /**
     * 通过证件号码查询
     * @param IdNumber
     * @return
     */
    public Intermediary fetchByIdNumberAndId(String IdNumber,String id) {
        if (StringUtils.isEmpty(IdNumber) || StringUtils.isEmpty(id)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("idNumber","=",IdNumber).and("status","=", PublicStatus.ABLE).and("id","!=",id));
    }

    /**
     * 通过证件号码查询-noStatus
     * @param IdNumber
     * @return
     */
    public Intermediary fetchByIdNumberAndIdNoStatus(String IdNumber,String id) {
        if (StringUtils.isEmpty(IdNumber) || StringUtils.isEmpty(id)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("idNumber","=",IdNumber).and("id","!=",id));
    }

    /**
     * 通过名称查询
     * @param name
     * @return
     */
    public Intermediary fetchByNameAndId(String name,String id) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(id)){
            return null;
        }
        return dao().fetch(Intermediary.class,Cnd.where("name","=",name).and("status","=", PublicStatus.ABLE).and("id","!=",id));
    }

}
