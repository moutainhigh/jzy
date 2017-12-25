package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.enums.LoanRepayStatus;
import com.kaisa.kams.enums.LoanStatus;
import com.kaisa.kams.models.*;
import com.kaisa.kams.models.report.RiskControlReport;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.dao.sql.SqlCallback;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by zhouchuang on 2017/5/25.
 */
@IocBean(fields="dao")
public class RiskControlReportService  extends IdNameEntityService<RiskControlReport> {
    public DataTables riskControlReportPage(DataTableParam param){


        //handleGeneralRiskControlByDay();

        Date date =   DateUtil.getStringToDate( param.getSearchKeys().get("report_time")+" 00:00:00");
       // Cnd cnd = Cnd.where("createTime",">", DateUtil.get0OClock(date)).and("createTime","<", DateUtil.get0OClock(DateUtils.addDays(date,1)));
        Cnd cnd = Cnd.where("createTime","like","%"+param.getSearchKeys().get("report_time")+"%");
        cnd.desc("productType").asc("code");
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        List<RiskControlReport> riskControlReports= query(cnd,pager);
        return new DataTables(param.getDraw(),dao().count(RiskControlReport.class),dao().count(RiskControlReport.class,cnd),riskControlReports);
    }


    public boolean checkGeneralable(){
        Object obj =  dao().fetch(RiskControlReport.class, Cnd.where("createTime","like", "%"+DateUtil.formatDateToString(new Date())+"%"));
        return obj==null;
    }
    public void handleGeneralRiskControlByDay(){
        //如果今天已经生成过了，不用生成了
        if(!checkGeneralable()) {
            return;
        }

        String sqlStr = "select  spt.name as productType, sblr.id as 'billId',sp.code,sp.name,slr.amount,slr.interest," +
                "sl.loanStatus,sl.id,sl.actualAmount,slr.status,slr.overdueDays  " +
                "from sl_loan_repay slr " +
                "left join sl_loan sl on slr.loanId = sl.id " +
                "left join sl_product sp on sl.productId = sp.id  " +
                "left join sl_product_type spt on spt.id = sl.productTypeId " +
                "left join sl_bill_loan_repay sblr on sblr.repayId = slr.id \n" +
                " where sl.loanStatus IN ('LOANED', 'CLEARED', 'OVERDUE') and sl.status = 'ABLE' order by spt.name,sp.code,sl.code,slr.status";
        Sql sql = Sqls.create(sqlStr);
        sql.setCallback(new SqlCallback() {
            public Object invoke(Connection conn, ResultSet rs, Sql sql) throws SQLException {
                List<RiskControlReport> list = new LinkedList<>();
                RiskControlReport billReport = null;
                String tmpOldLoanId = "";
                while (rs.next()) {
                    if(rs.getString("name")==null) {
                        continue;
                    }
                    BigDecimal amount  = rs.getBigDecimal("amount");
                    amount = (amount!=null?amount:BigDecimal.ZERO);
                    BigDecimal  actualAmount = rs.getBigDecimal("actualAmount");
                    actualAmount=  (actualAmount!=null)?actualAmount:BigDecimal.ZERO;
                    BigDecimal interest  = rs.getBigDecimal("interest");
                    interest = (interest!=null?interest:BigDecimal.ZERO);
                    int overdueDays  = rs.getInt("overdueDays");
                    String status = rs.getString("status");
                    String loanStatus = rs.getString("loanStatus");
                    String loanId = rs.getString("id");
                    String oldLoanId = loanId;
                    String billId = rs.getString("billId");
                    if(StringUtils.isNotEmpty(billId)){
                        loanId  = billId;
                    }

                    if(billReport==null||StringUtils.isEmpty(billReport.getProductName())||!billReport.getProductName().equals(rs.getString("name"))){
                        billReport = new RiskControlReport();
                        billReport.setProductName(rs.getString("name"));
                        billReport.setProductType(rs.getString("productType"));
                        billReport.setCode(rs.getString("code"));
                        list.add(billReport);
                    }

                    if(!billReport.getLastljfkCountLoanId().equals(loanId)){
                        billReport.setLjfkCount(billReport.getLjfkCount()+1);
                        billReport.setLastljfkCountLoanId(loanId);
                        if (oldLoanId.equals(loanId)) {
                            billReport.setLjfkMoney(billReport.getLjfkMoney().add(actualAmount));
                        } else if(!oldLoanId.equals(tmpOldLoanId)) {
                            billReport.setLjfkMoney(billReport.getLjfkMoney().add(actualAmount));
                            tmpOldLoanId = oldLoanId;
                        }
                    }

                    if(LoanStatus.LOANED.name().equals(loanStatus)||LoanStatus.OVERDUE.name().equals(loanStatus)){
                        if (LoanRepayStatus.LOANED.name().equals(status)||LoanRepayStatus.OVERDUE.name().equals(status)) {
                            if (!billReport.getLastDsdkCountLoanId().equals(loanId)) {
                                billReport.setDsdkCount(billReport.getDsdkCount() + 1);
                                billReport.setLastDsdkCountLoanId(loanId);
                            }
                        }
                    }
                    if(LoanStatus.LOANED.name().equals(loanStatus)||LoanStatus.OVERDUE.name().equals(loanStatus)){
                        if(LoanRepayStatus.LOANED.name().equals(status)||LoanRepayStatus.OVERDUE.name().equals(status)){
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setDsdkInterest(billReport.getDsdkInterest().add(interest));
                            }
                            billReport.setDsdkPrincipal(billReport.getDsdkPrincipal().add(amount));
                        }
                    }
                    if(LoanRepayStatus.OVERDUE.name().equals(status)){
                        if(overdueDays<=30){
                            if(!billReport.getLastyq030CountLoanId().equals(loanId)){
                                billReport.setYq030Count(billReport.getYq030Count()+1);
                                billReport.setLastyq030CountLoanId(loanId);
                            }
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setYq030Interest(billReport.getYq030Interest().add(interest));
                            }
                            billReport.setYq030Principal(billReport.getYq030Principal().add(amount));
                        }else if(overdueDays<=60){
                            if(!billReport.getLastyq3160CountLoanId().equals(loanId)){
                                billReport.setYq3160Count(billReport.getYq3160Count()+1);
                                billReport.setLastyq3160CountLoanId(loanId);
                            }
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setYq3160Interest(billReport.getYq3160Interest().add(interest));
                            }
                            billReport.setYq3160Principal(billReport.getYq3160Principal().add(amount));
                        }else if(overdueDays<=90){
                            if(!billReport.getLastyq6190CountLoanId().equals(loanId)){
                                billReport.setYq6190Count(billReport.getYq6190Count()+1);
                                billReport.setLastyq6190CountLoanId(loanId);
                            }
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setYq6190Interest(billReport.getYq6190Interest().add(interest));
                            }
                            billReport.setYq6190Principal(billReport.getYq6190Principal().add(amount));
                        }else if(overdueDays<=180){
                            if(!billReport.getLastyq91180CountLoanId().equals(loanId)){
                                billReport.setYq91180Count(billReport.getYq91180Count()+1);
                                billReport.setLastyq91180CountLoanId(loanId);
                            }
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setYq91180Interest(billReport.getYq91180Interest().add(interest));
                            }
                            billReport.setYq91180Principal(billReport.getYq91180Principal().add(amount));
                        }else {
                            if(!billReport.getLastyq180CountLoanId().equals(loanId)){
                                billReport.setYq180Count(billReport.getYq180Count()+1);
                                billReport.setLastyq180CountLoanId(loanId);
                            }
                            if(StringUtils.isEmpty(billId)) {
                                billReport.setYq180Interest(billReport.getYq180Interest().add(interest));
                            }
                            billReport.setYq180Principal(billReport.getYq180Principal().add(amount));
                        }
                        if(!billReport.getLastyqTotalCountLoanId().equals(loanId)){
                            billReport.setYqTotalCount(billReport.getYqTotalCount()+1);
                            billReport.setLastyqTotalCountLoanId(loanId);
                        }
                    }


                }
                return list;
            }
        });
        dao().execute(sql);
        List<RiskControlReport> list = sql.getList(RiskControlReport.class);
        RiskControlReport total  = new RiskControlReport();
        for(RiskControlReport riskControlReport : list){
            total.setCode("99999999");
            total.setProductName("合计");
            total.setProductType("_合计");
            total.setLjfkCount(total.getLjfkCount()+riskControlReport.getLjfkCount());
            total.setLjfkMoney(total.getLjfkMoney().add(riskControlReport.getLjfkMoney()));
            total.setDsdkCount(total.getDsdkCount()+riskControlReport.getDsdkCount());
            total.setDsdkPrincipal(total.getDsdkPrincipal().add(riskControlReport.getDsdkPrincipal()));
            total.setDsdkInterest(total.getDsdkInterest().add(riskControlReport.getDsdkInterest()));

            total.setYq030Count(total.getYq030Count()+riskControlReport.getYq030Count());
            total.setYq030Principal(total.getYq030Principal().add(riskControlReport.getYq030Principal()));
            total.setYq030Interest(total.getYq030Interest().add(riskControlReport.getYq030Interest()));

            total.setYq3160Count(total.getYq3160Count()+riskControlReport.getYq3160Count());
            total.setYq3160Principal(total.getYq3160Principal().add(riskControlReport.getYq3160Principal()));
            total.setYq3160Interest(total.getYq3160Interest().add(riskControlReport.getYq3160Interest()));

            total.setYq6190Count(total.getYq6190Count()+riskControlReport.getYq6190Count());
            total.setYq6190Principal(total.getYq6190Principal().add(riskControlReport.getYq6190Principal()));
            total.setYq6190Interest(total.getYq6190Interest().add(riskControlReport.getYq6190Interest()));

            total.setYq91180Count(total.getYq91180Count()+riskControlReport.getYq91180Count());
            total.setYq91180Principal(total.getYq91180Principal().add(riskControlReport.getYq91180Principal()));
            total.setYq91180Interest(total.getYq91180Interest().add(riskControlReport.getYq91180Interest()));

            total.setYq180Count(total.getYq180Count()+riskControlReport.getYq180Count());
            total.setYq180Principal(total.getYq180Principal().add(riskControlReport.getYq180Principal()));
            total.setYq180Interest(total.getYq180Interest().add(riskControlReport.getYq180Interest()));

            total.setYqTotalCount(total.getYqTotalCount()+riskControlReport.getYqTotalCount());
        }
        list.add(total);

        List<RiskControlReport> listsub = new ArrayList<RiskControlReport>();
        RiskControlReport totalsub = null;
        for(RiskControlReport riskControlReport : list){
            if(riskControlReport.getProductType().contains("合计"))continue;
            if(totalsub==null||!totalsub.getProductType().equals(riskControlReport.getProductType())){
                totalsub = new RiskControlReport();
                listsub.add(totalsub);
                totalsub.setProductType(riskControlReport.getProductType());
            }
            totalsub.setCode("99999999");
            totalsub.setProductName("合计");
            totalsub.setLjfkCount(totalsub.getLjfkCount()+riskControlReport.getLjfkCount());
            totalsub.setLjfkMoney(totalsub.getLjfkMoney().add(riskControlReport.getLjfkMoney()));
            totalsub.setDsdkCount(totalsub.getDsdkCount()+riskControlReport.getDsdkCount());
            totalsub.setDsdkPrincipal(totalsub.getDsdkPrincipal().add(riskControlReport.getDsdkPrincipal()));
            totalsub.setDsdkInterest(totalsub.getDsdkInterest().add(riskControlReport.getDsdkInterest()));

            totalsub.setYq030Count(totalsub.getYq030Count()+riskControlReport.getYq030Count());
            totalsub.setYq030Principal(totalsub.getYq030Principal().add(riskControlReport.getYq030Principal()));
            totalsub.setYq030Interest(totalsub.getYq030Interest().add(riskControlReport.getYq030Interest()));

            totalsub.setYq3160Count(totalsub.getYq3160Count()+riskControlReport.getYq3160Count());
            totalsub.setYq3160Principal(totalsub.getYq3160Principal().add(riskControlReport.getYq3160Principal()));
            totalsub.setYq3160Interest(totalsub.getYq3160Interest().add(riskControlReport.getYq3160Interest()));

            totalsub.setYq6190Count(totalsub.getYq6190Count()+riskControlReport.getYq6190Count());
            totalsub.setYq6190Principal(totalsub.getYq6190Principal().add(riskControlReport.getYq6190Principal()));
            totalsub.setYq6190Interest(totalsub.getYq6190Interest().add(riskControlReport.getYq6190Interest()));

            totalsub.setYq91180Count(totalsub.getYq91180Count()+riskControlReport.getYq91180Count());
            totalsub.setYq91180Principal(totalsub.getYq91180Principal().add(riskControlReport.getYq91180Principal()));
            totalsub.setYq91180Interest(totalsub.getYq91180Interest().add(riskControlReport.getYq91180Interest()));

            totalsub.setYq180Count(totalsub.getYq180Count()+riskControlReport.getYq180Count());
            totalsub.setYq180Principal(totalsub.getYq180Principal().add(riskControlReport.getYq180Principal()));
            totalsub.setYq180Interest(totalsub.getYq180Interest().add(riskControlReport.getYq180Interest()));

            totalsub.setYqTotalCount(totalsub.getYqTotalCount()+riskControlReport.getYqTotalCount());
        }

        list.addAll(listsub);

        for(RiskControlReport riskControlReport : list){
            riskControlReport.setCreateTime(new Date());
            riskControlReport.setCreateBy("定时任务生成");
            dao().insert(riskControlReport);
        }
    }
}
