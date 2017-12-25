package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.models.DataTables;
import com.kaisa.kams.models.HouseManage;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.FieldFilter;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.util.Daos;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * 房产管理服务层
 * Created by lw on 2017/8/14.
 */
@IocBean(fields = "dao")
public class HouseManageService extends IdNameEntityService<HouseManage> {

    @Inject
    private LoanService loanService;

    public DataTables query(DataTableParam param){

        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        String code = "";
        String borrower = "";
        String loanDate = "";
        String mortgageDate = "";
        String noMortgageDate = "";
        String storageStatus = "";
        String propertyRightStatus = "";
        String channelType = "";
        String saleName = "";
        String channelId = "";
        if (null != param.getSearchKeys()) {
            Map<String,String> keys = param.getSearchKeys();
            code = keys.get("code");
            borrower = keys.get("borrower");
            loanDate = keys.get("loanDate");
            mortgageDate = keys.get("mortgageDate");
            noMortgageDate = keys.get("noMortgageDate");
            storageStatus = keys.get("storageStatus");
            propertyRightStatus = keys.get("propertyRightStatus");
            channelType = keys.get("channelType");
            saleName = keys.get("saleName");
            channelId = keys.get("channelId");

        }

        String sqlStr = "SELECT  * from sl_house_manage sh where 1=1" ;

        String countSqlStr = "SELECT COUNT(DISTINCT sh.id) AS 'number' from sl_house_manage sh where 1=1";
        Date beginLoanDate = null;
        Date endLoanDate = null;
        if (StringUtils.isNotEmpty(loanDate)){
            beginLoanDate = TimeUtils.getQueryStartDateTime(loanDate);
            endLoanDate = TimeUtils.getQueryEndDateTime(loanDate);
            if (null!=beginLoanDate&&null!=endLoanDate){
                sqlStr+=" AND sh.loanTime>=@beginLoanDate  AND sh.loanTime<=@endLoanDate ";
                countSqlStr+=" AND sh.loanTime>=@beginLoanDate  AND sh.loanTime<=@endLoanDate ";
            }
        }

        if (StringUtils.isNotEmpty(code)){
            sqlStr+=" AND sh.businessCode=@code ";
            countSqlStr+=" AND sh.businessCode=@code ";
        }

        if (StringUtils.isNotEmpty(borrower)){
            sqlStr+=" AND  sh.borrower like @borrower ";
            countSqlStr+=" AND  sh.borrower like @borrower ";
        }

        Date beginMortgageDate = null;
        Date endMortgageDate = null;
        if (StringUtils.isNotEmpty(mortgageDate)){
            beginMortgageDate = TimeUtils.getQueryStartDateTime(mortgageDate);
            endMortgageDate = TimeUtils.getQueryEndDateTime(mortgageDate);
            if (null!=beginMortgageDate&&null!=endMortgageDate){
                sqlStr+=" AND sh.mortgageDate>=@beginMortgageDate  AND sh.mortgageDate<=@endMortgageDate ";
                countSqlStr+=" AND sh.mortgageDate>=@beginMortgageDate  AND sh.mortgageDate<=@endMortgageDate ";
            }
        }

        Date beginNoMortgageDate = null;
        Date endNoMortgageDate = null;
        if (StringUtils.isNotEmpty(noMortgageDate)){
            beginNoMortgageDate = TimeUtils.getQueryStartDateTime(noMortgageDate);
            endNoMortgageDate = TimeUtils.getQueryEndDateTime(noMortgageDate);
            if (null!=beginNoMortgageDate&&null!=endNoMortgageDate){
                sqlStr+=" AND sh.noMortgageDate>=@beginNoMortgageDate  AND sh.noMortgageDate<=@endNoMortgageDate ";
                countSqlStr+=" AND sh.noMortgageDate>=@beginNoMortgageDate  AND sh.noMortgageDate<=@endNoMortgageDate ";
            }
        }

        if (StringUtils.isNotEmpty(propertyRightStatus)) {
            sqlStr += " AND  sh.propertyRightStatus=@propertyRightStatus ";
            countSqlStr += " AND  sh.propertyRightStatus=@propertyRightStatus ";
        }

        if (StringUtils.isNotEmpty(storageStatus)) {
            sqlStr += " AND  sh.storageStatus=@storageStatus ";
            countSqlStr += " AND  sh.storageStatus=@storageStatus ";
        }
        if (StringUtils.isNotEmpty(channelType)) {
            sqlStr += " AND  sh.channelType=@channelType ";
            countSqlStr += " AND  sh.channelType=@channelType ";
        }

        if (StringUtils.isNotEmpty(saleName)) {
            sqlStr += " AND  sh.saleMan like @saleName ";
            countSqlStr += " AND  sh.saleMan like @saleName ";
        }
        if (StringUtils.isNotEmpty(channelId)) {
            sqlStr += " AND  sh.channelId=@channelId ";
            countSqlStr += " AND  sh.channelId=@channelId ";
        }
        sqlStr+=" order by sh.loanTime DESC " ;
        Sql sql = Sqls.create(sqlStr);
        Sql countSql = Sqls.create(countSqlStr);
        sql.setParam("code", code);
        sql.setParam("borrower","%"+borrower+"%");
        sql.setParam("beginLoanDate", beginLoanDate);
        sql.setParam("endLoanDate", endLoanDate);
        sql.setParam("beginMortgageDate", beginMortgageDate);
        sql.setParam("endMortgageDate", endMortgageDate);
        sql.setParam("beginNoMortgageDate", beginNoMortgageDate);
        sql.setParam("endNoMortgageDate", endNoMortgageDate);
        sql.setParam("propertyRightStatus", propertyRightStatus);
        sql.setParam("storageStatus", storageStatus);
        sql.setParam("saleName", '%'+saleName+'%');
        sql.setParam("channelId", channelId);
        sql.setParam("channelType", channelType);
        sql.setPager(pager);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(HouseManage.class));
        dao().execute(sql);
        List<HouseManage> list = sql.getList(HouseManage.class);
        countSql.setParam("code", code);
        countSql.setParam("borrower","%"+borrower+"%");
        countSql.setParam("beginLoanDate", beginLoanDate);
        countSql.setParam("endLoanDate", endLoanDate);
        countSql.setParam("beginMortgageDate", beginMortgageDate);
        countSql.setParam("endMortgageDate", endMortgageDate);
        countSql.setParam("beginNoMortgageDate", beginNoMortgageDate);
        countSql.setParam("endNoMortgageDate", endNoMortgageDate);
        countSql.setParam("propertyRightStatus", propertyRightStatus);
        countSql.setParam("storageStatus", storageStatus);
        countSql.setParam("saleName", '%'+saleName+'%');
        countSql.setParam("channelId", channelId);
        countSql.setParam("channelType", channelType);
        countSql.setCallback(Sqls.callback.integer());
        dao().execute(countSql);
        int count = countSql.getInt();

        return new DataTables(param.getDraw(),count,count,list);

    }

    /**
     * 修改房产
     * @param houseManage
     * @return
     */
    public boolean update(HouseManage houseManage) {
        List param = new ArrayList<>();
        if (null==houseManage) {
            return false;
        }
        if(StringUtils.isEmpty(houseManage.getId())){
            param.add("id");
        }
        if(null == houseManage.getGuaranteeResponsibility()){
            param.add("guaranteeResponsibility");
        }
        if(null == houseManage.getNoMortgageDate()){
            param.add("noMortgageDate");
        }
        if(null == houseManage.getInDate()){
            param.add("inDate");
        }
        if(null == houseManage.getOutDate()){
            param.add("outDate");
        }
        if(null == houseManage.getMortgageType()){
            param.add("mortgageType");
        }
        if(null == houseManage.getMortgageDate()){
            param.add("mortgageDate");
        }
        if(null == houseManage.getFileUrls()){
            param.add("fileUrls");
        }
        param.add("createBy");
        param.add("createTime");

        //忽略少数字段更新
        String paramStr = StringUtils.join(param,"|");
        return Daos.ext(dao(), FieldFilter.locked(HouseManage.class, "^("+paramStr+")$")).update(houseManage)>0;
    }

    /**
     * 通过Id查找
     * @param id
     * @return
     */
    public HouseManage fetchById(String id) {
        return dao().fetch(HouseManage.class, Cnd.where("id","=",id));
    }

}
