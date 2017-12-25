package com.kaisa.kams.components.service;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.security.ShiroSession;
import com.kaisa.kams.components.service.base.ReportUtilsService;
import com.kaisa.kams.components.utils.CoverUtil;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.DecimalUtils;
import com.kaisa.kams.components.utils.TimeUtils;
import com.kaisa.kams.components.utils.report.ReportUtils;
import com.kaisa.kams.components.view.loan.Duration;
import com.kaisa.kams.components.view.report.BusinessReportLastRepayData;
import com.kaisa.kams.components.view.report.HouseBusinessHongBenReport;
import com.kaisa.kams.components.view.report.HouseBusinessShuLouReport;
import com.kaisa.kams.enums.*;
import com.kaisa.kams.models.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Chain;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sunwanchao on 2017/3/20.
 */
@IocBean(fields = "dao")
public class HouseInfoService extends IdNameEntityService<HouseInfo> {

    public List<HouseInfo> queryByLoanId(String loanId){
        return dao().query(HouseInfo.class, Cnd.where("loanId","=",loanId).orderBy("position","asc"));
    }

    public HouseInfo fetchById(String id){
        return this.dao().fetch(HouseInfo.class, Cnd.where("id","=",id));
    }

    public boolean deleteByLoanId(String loanId){
        return dao().clear(HouseInfo.class, Cnd.where("loanId","=",loanId))>0;
    }

    public void save(List<HouseInfo> houseInfoList){
        this.dao().insert(houseInfoList);
    }

    /**
     * 根据loanId更新
     * @param id
     * @param warrantNumber
     * @return
     */
    public  boolean  updateWarrantNumber(String id ,String warrantNumber){
        User user= ShiroSession.getLoginUser();
        return dao().update(HouseInfo.class, Chain.make("warrantNumber",warrantNumber).add("updateBy",user.getName()).add("updateTime",new Date()),Cnd.where("id","=",id))>0;
    }


    private  List<Map<String,Object>> JsonToListMap(String houseJson) {
        List list = new ArrayList();
        JSONArray jsonArray = JSONArray.fromObject(houseJson);
        for (int i = 0; i < jsonArray.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            JSONArray jsonArray1 = JSONArray.fromObject(jsonArray.get(i));
            for (int j = 0; j < jsonArray1.size(); j++) {
                JSONObject jsonObject = jsonArray1.getJSONObject(j);
                String key = jsonObject.get("keyName").toString();
                if(StringUtils.isNotEmpty(key)){
                    if (null != jsonObject.get("dataValue")) {
                        map.put(key, jsonObject.get("dataValue").toString());
                    } else {
                        map.put(key, "");
                    }
                }
            }
            list.add(map);
        }
        return list;
    }

    public void saveHouseInfoHistory(){
        String sqlStr = "SELECT " +
                " spit.dataValue, " +
                " spit.loanId " +
                "FROM " +
                " sl_product_info_item spit " +
                "LEFT JOIN sl_loan sl ON spit.loanId = sl.id " +
                "AND spit.keyName = 'house' " +
                "LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                "AND spt.productType = 'SHULOU' " +
                "WHERE " +
                " sl.id NOT IN ( " +
                "  SELECT " +
                "   loanId " +
                "  FROM " +
                "   sl_house_info " +
                " )";
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(ProductInfoItem.class));
        dao().execute(sql);
        List<ProductInfoItem> list = sql.getList(ProductInfoItem.class);
        for(ProductInfoItem productInfoItem : list){
            String str = productInfoItem.getDataValue();
            if(StringUtils.isNotEmpty(str)){
                List<Map<String,Object>> maplist = JsonToListMap(str);
                for(Map<String,Object> map : maplist){
                    HouseInfo houseInfo = new HouseInfo();
                    for(Field field : HouseInfo.class.getDeclaredFields()){
                        String  keyName = field.getName().toUpperCase();
                        map.forEach((k,v)->{
                            String  mapKey = k.toUpperCase().replace("_","");
                            if(mapKey.equals("USER"))mapKey = "OWER";
                            if(keyName.equals(mapKey)||mapKey.contains(keyName)){
                                try {
                                    field.setAccessible(true);
                                    field.set(houseInfo,v);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                return ;
                            }
                        });
                    }
                    houseInfo.updateOperator();
                    houseInfo.setLoanId(productInfoItem.getLoanId());
                    dao().insert(houseInfo);
                }
            }
        }

    }
}
