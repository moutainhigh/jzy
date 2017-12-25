package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DecimalFormatUtils;
import com.kaisa.kams.enums.ChannelUserType;
import com.kaisa.kams.enums.CompanyType;
import com.kaisa.kams.enums.PublicStatus;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.Enterprise;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * 企业管理
 * Created by lw on 2017/4/12.
 */
@IocBean(fields = "dao")
public class EnterpriseService extends IdNameEntityService<Enterprise> {
    private static final Log log = Logs.get();

    @Inject
    private BillLoanService billLoanService;

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
                cnd= Cnd.where("name", "like", "%" + name + "%").orderBy("name","desc");
            }else {
                cnd=Cnd.where("1", "=", "1").orderBy("name","desc");
            }
            List<Enterprise> enterpriseList = dao().query(Enterprise.class, cnd, pager);
            dataTables = new DataTables(param.getDraw(), dao().count(Enterprise.class), dao().count(Enterprise.class, cnd), enterpriseList);
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
     * 修改
     * @param enterprise
     * @return
     */
    public boolean update(Enterprise enterprise) {
        if (null==enterprise) {
            return false;
        }
        //忽略少数字段更新
//        return Daos.ext(dao(), FieldFilter.locked(Enterprise.class, "^id|createBy|createTime$")).update(enterprise)>0;
        return dao().update(enterprise,"^(name|level|price|establishDate|nature|computerNature|creditQuota|year|businessIncome|notNetProfit|cashFlowNet|assetsDebtRatio|computerBasic|executorSituation|dispute|managerOpinion|otherSituation|type|remainderAmount|libraryAmount|companyId|companyName|companies|updateBy|updateTime)$")>0;
    }

    /**
     * 新增
     * @param enterprise
     * @return
     */
    public Enterprise add(Enterprise enterprise) {
        if(null==enterprise) {
            return null;
        }
        return dao().insert(enterprise);
    }

    /**
     * 查询
     * @param id
     * @return
     */
    public Enterprise fetchById(String id) {
        if(null==id) {
            return null;
        }
        return dao().fetch(Enterprise.class,id);
    }

    /**
     * 根据name查询
     *
     * @param name
     * @return
     */
    public List<Enterprise> fetchByName(String name) {
        if (null != name) {
            if(ShiroSession.getLoginUser().getType().equals(ChannelUserType.CHANNEL_USER)){
                return dao().query(Enterprise.class, Cnd.where("name","=",name));
            }else {
                return dao().query(Enterprise.class, Cnd.where("name","like","%"+name+"%"));
            }
        }
        return null;
    }

    /**
     * 根据name、type查询
     *
     * @param name、type
     * @return
     */
    public List<Enterprise> fetchByNameAndType(String name, CompanyType companyType) {
        if (null != name) {
            return dao().query(Enterprise.class, Cnd.where("name","like","%"+name+"%").and("type","=",companyType));
        }
        return null;
    }

    /**
     * 通过name查询
     * @param name
     * @return
     */
    public Enterprise fetchByNameForExit(String name) {
        if (StringUtils.isEmpty(name)){
            return null;
        }
        return dao().fetch(Enterprise.class,Cnd.where("name","=",name));
    }

    /**
     * 通过name查询
     * @param name,id
     * @return
     */
    public Enterprise fetchByNameAndIdForExit(String name,String id) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(id) ){
            return null;
        }
        return dao().fetch(Enterprise.class,Cnd.where("name","=",name).and("id","!=",id));
    }

    /**
     * 根据companyId查询
     *
     * @param companyId
     * @return
     */
    public List<Enterprise> fetchByCompanyId(String companyId) {
        if (null != companyId) {
            return dao().query(Enterprise.class, Cnd.where("companyId","=",companyId));
        }
        return null;
    }

    /**
     * 根据companyId查询
     *
     * @param companyId
     * @return
     */
    public Enterprise fetchCreditByCompanyId(String companyId) {
        if (null != companyId) {
            return dao().fetch(Enterprise.class, Cnd.where("companyId","=",companyId));
        }
        return null;
    }

    public List<Enterprise> queryAll() {

        return dao().query(Enterprise.class, Cnd.where("1","=",1));

    }

    public void setRemaindVal(){
        BigDecimal libraryAmount = BigDecimal.ZERO;
        List<Enterprise> enterpriseList = queryAll();
        for (Enterprise enterprise : enterpriseList){
            if(null != enterprise && CompanyType.CREDITCOMPANY.equals(enterprise.getType())){
                enterpriseList = fetchByCompanyId(enterprise.getId());
                if(CollectionUtils.isNotEmpty(enterpriseList)){
                    for (Enterprise e : enterpriseList){
                        libraryAmount = libraryAmount.add(billLoanService.queryRepayAmount(e.getId()));
                    }
                    enterprise.setLibraryAmount(libraryAmount);
                    if(null == enterprise.getCreditQuota()){
                        enterprise.setRemainderAmount(BigDecimal.ZERO);
                    }else {
                        enterprise.setRemainderAmount(enterprise.getCreditQuota().subtract(libraryAmount));
                    }
                    update(enterprise);
                }else {
                    if(null == enterprise.getCreditQuota()){
                        enterprise.setRemainderAmount(BigDecimal.ZERO);
                    }else {
                        enterprise.setRemainderAmount(enterprise.getCreditQuota().subtract(billLoanService.queryRepayAmount(enterprise.getId())));
                    }
                    enterprise.setLibraryAmount(billLoanService.queryRepayAmount(enterprise.getId()));
                    update(enterprise);
                }
            }else {
                enterprise.setLibraryAmount(billLoanService.queryRepayAmount(enterprise.getId()));
                update(enterprise);
            }
        }

    }
}
