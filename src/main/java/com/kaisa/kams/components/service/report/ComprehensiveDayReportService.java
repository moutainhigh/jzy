package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.common.DataTableParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.utils.ProductTypesUtils;
import com.kaisa.kams.models.report.ComprehensiveDayReport;
import com.kaisa.kams.models.DataTables;

import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.IdNameEntityService;

import java.util.Date;
import java.util.List;

/**
 * Created by zhouchuang on 2017/6/9.
 */
@IocBean(fields="dao")
public class ComprehensiveDayReportService  extends IdNameEntityService<ComprehensiveDayReport> {
    public boolean checkComprehensiveReportGeneralable(String report_time){

        Object obj = dao().fetch(ComprehensiveDayReport.class, Cnd.where("createTime","like", "%"+ report_time+"%"));
        //Object obj =  dao().fetch(ComprehensiveDayReport.class, Cnd.where("createTime","like", "%"+ DateUtil.getDatetoString(DateUtil.getDateAfter(new Date(),-1))+"%"));
        return obj==null;
    }


    public void handleGeneralComprehensiveReportByDay(String report_time){
        //如果今天已经生成过了，不用生成了
        if(!checkComprehensiveReportGeneralable(report_time)) {
            return;
        }
        //获取昨天的日期
        String busiDate = report_time;
        //String busiDate  = DateUtil.getDatetoString(DateUtil.getDateAfter(new Date(),-1));
        String productType = ProductTypesUtils.getName("PIAOJU");
        String sqlStr = "SELECT " +
                " spt. NAME AS productType, " +
                " count(DISTINCT case when sl.createTime like concat('%',@busiDate,'%') then (case  when spt.name = @productType then slr.id else sl.id end ) else null end) AS businessSubmissionNumber, " +
                " count(DISTINCT case when sl.loanStatus = 'APPROVEREJECT' and EXISTS(select sar.loanId from  view_approval_result sar where  sar.loanId = sl.id and sar.approvalCode = 'DISAGREE' and sar.approvalTime like concat('%',@busiDate,'%')  )  then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS approvalRejectNumber, " +
                " count(DISTINCT case when (sl.loanStatus = 'APPROVEEND' or sl.loanStatus = 'LOANED' or sl.loanStatus = 'CLEARED' or sl.loanStatus = 'OVERDUE' ) and EXISTS(select sar.loanId from  view_approval_result sar where sar.loanId = sl.id  and sar.approvalCode = 'AGREE' and sar.approvalTime like concat('%',@busiDate,'%')  )  then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS approvalPassNumber, " +
                " count(DISTINCT case when sl.loanTime is not null and sl.loanTime  like concat('%',@busiDate,'%')   then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS loanNumber, " +
               /* " sum(  " +
                "  IF( " +
                "   sl.loanTime is not null and sl.loanTime like concat('%',@busiDate,'%') , " +
                "   slr.amount, " +
                "   0 " +
                "  ) " +
                " ) AS loanMoney, " +*/
                " count(DISTINCT case when  (case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end  )   then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)AS repaymentNumber, " +
                " sum( " +
                " " +
                "  IF (  " +
                "   case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end , " +
                "   slr.totalAmount  , " +
                "   0 " +
                "  ) " +
                " ) AS repaymentMoney, " +
               "count(DISTINCT CASE WHEN slr.dueDate IS NOT NULL AND slrr.repayDate IS NOT NULL AND slr.dueDate = @busiDate AND slrr.repayDate = @busiDate THEN(slrr.id) ELSE NULL END) AS paymentRecordNum, " +
               " sum(  " +
                "  IF( " +
                "   slr.dueDate IS NOT NULL AND slrr.repayDate IS NOT NULL AND slr.dueDate = @busiDate AND slrr.repayDate = @busiDate, " +
                "   slrr.repayTotalAmount, " +
                "   0 " +
                "  ) " +
                " ) AS paymentRecordMoney, " +

                " count(DISTINCT case when  slr.repayDate like concat('%',@busiDate,'%') then (case  when spt.name = @productType then slr.id else sl.id end ) else null end) AS actualRepaymentNumber, " +
                " sum( " +
                " " +
                "  IF ( " +
                "   slr.repayDate like concat('%',@busiDate,'%'), " +
                "   slr.repayTotalAmount, " +
                "   0 " +
                "  ) " +
                " ) AS actualRepaymentMoney ," +
                " count(DISTINCT case when sl.loanStatus = 'OVERDUE' and (case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end) then (case  when spt.name = @productType then slr.id else sl.id end ) else null end) AS overdueNumber, " +
                " sum( " +
                "  IF ( " +
                "   slr. STATUS = 'OVERDUE' and   (case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end ), " +
                "   slr.totalAmount, " +
                "   0 " +
                "  ) " +
                " ) AS overdueMoney, " +
                " count(DISTINCT case when sl.loanStatus = 'OVERDUE' and (case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end) then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)  /  " +
                " count(DISTINCT case when  (case when sblr.actualDueDate is not null then sblr.actualDueDate like concat('%',@busiDate,'%') else slr.dueDate like concat('%',@busiDate,'%') end  )   then (case  when spt.name = @productType then slr.id else sl.id end ) else null end)  AS overdueRate"+
               /* " ROUND(" +
                " sum(" +
                "  IF (" +
                "   sl.loanTime is not null and  sl.loanTime like concat('%',@busiDate,'%')," +
                "   CASE" +
                "  WHEN sl.termType = 'DAYS' THEN" +
                "   sl.term" +
                "  WHEN sl.termType = 'MOTHS' THEN" +
                "   sl.term * 30" +
                "  ELSE" +
                "   TIMESTAMPDIFF(DAY, sl.loanTime,  sl.term)+1" +
                "  END," +
                "  0" +
                "  )" +
                " ) / sum(" +
                "" +
                "  IF (" +
                "   sl.loanTime is not null and  sl.loanTime like concat('%',@busiDate,'%')," +
                "   1," +
                "   0" +
                "  )" +
                " )," +
                " 0" +
                ") AS averageLoan"+*/
                " FROM " +
                " sl_loan sl " +
                " LEFT JOIN sl_loan_repay slr ON sl.id = slr.loanId " +
                " LEFT JOIN sl_bill_loan_repay sblr on sblr.repayId = slr.id"+
                " LEFT JOIN sl_loan_repay_record slrr ON slrr.repayId = slr.id " +
                " LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                " where sl.status='ABLE'  " +
                " GROUP BY " +
                " sl.productTypeId";
        String sqlStr1 = "SELECT " +
                "  spt. NAME AS productType , " +
                "  sum(IF ( " +
                "   sl.loanTime IS NOT NULL " +
                "   AND sl.loanTime like concat('%',@busiDate,'%') , " +
                "   sl.actualAmount, " +
                "   0 " +
                "  )) as loanMoney," +
                " sum( " +
                " " +
                "  IF ( " +
                "   sl.loanTime IS NOT NULL " +
                "   AND sl.loanTime like concat('%',@busiDate,'%')," +
                "   ( " +
                "    CASE " +
                "    WHEN spt. NAME =  @productType  THEN " +
                "     ( " +
                "      SELECT " +
                "       sum(sblr.disDays) " +
                "      FROM " +
                "       sl_bill_loan_repay sblr " +
                "      WHERE " +
                "       sblr.loanId = sl.id " +
                "     ) " +
                "    ELSE " +
                "     ( " +
                "      CASE " +
                "      WHEN sl.termType = 'DAYS' THEN " +
                "       sl.term " +
                "      WHEN sl.termType = 'MOTHS' THEN " +
                "       sl.term * 30 " +
                "      ELSE " +
                "       TIMESTAMPDIFF(DAY, sl.loanTime, sl.term) + 1 " +
                "      END " +
                "     ) " +
                "    END " +
                "   ), " +
                "   0 " +
                "  ) " +
                " ) AS 'totalDays' " +
                " FROM " +
                " sl_loan sl " +
                " LEFT JOIN sl_product_type spt ON sl.productTypeId = spt.id " +
                " where sl. STATUS = 'ABLE' " +
                " GROUP BY " +
                " spt.id";
        Sql sql = Sqls.create(sqlStr);
        sql.setParam("busiDate",busiDate);
        sql.setParam("productType",productType);
        sql.setCallback(Sqls.callback.entities());
        sql.setEntity(dao().getEntity(ComprehensiveDayReport.class));
        dao().execute(sql);
        List<ComprehensiveDayReport> list = sql.getList(ComprehensiveDayReport.class);


        Sql sql1 = Sqls.create(sqlStr1);
        sql1.setParam("busiDate",busiDate);
        sql1.setParam("productType",productType);
        sql1.setCallback(Sqls.callback.entities());
        sql1.setEntity(dao().getEntity(ComprehensiveDayReport.class));
        dao().execute(sql1);
        List<ComprehensiveDayReport> list1 = sql1.getList(ComprehensiveDayReport.class);


        for(ComprehensiveDayReport comprehensiveReport : list){
            for(ComprehensiveDayReport comprehensiveReport1 : list1){
                if(comprehensiveReport.getProductType().equals(comprehensiveReport1.getProductType())){
                    comprehensiveReport.setLoanMoney(comprehensiveReport1.getLoanMoney());
                    if(comprehensiveReport.getLoanNumber()>0)
                        comprehensiveReport.setAverageLoan((int)(Math.round((double)comprehensiveReport1.getTotalDays()/(double)comprehensiveReport.getLoanNumber())));
                    else
                        comprehensiveReport.setAverageLoan(0);
                    break;
                }
            }
        }


        for(ComprehensiveDayReport comprehensiveDayReport : list){
            comprehensiveDayReport.setCreateTime(DateUtil.getStringToDate(report_time));
            //comprehensiveDayReport.setCreateTime(DateUtil.getDateAfter(new Date(),-1));
            comprehensiveDayReport.setCreateBy("定时任务生成");
            dao().insert(comprehensiveDayReport);
        }
    }

    public DataTables comprehensiveDayReportPage(DataTableParam param){
        handleGeneralComprehensiveReportByDay(param.getSearchKeys().get("report_time"));

        Date date =   DateUtil.getStringToDate( param.getSearchKeys().get("report_time")+" 00:00:00");
        Cnd cnd = Cnd.where("createTime","like","%"+param.getSearchKeys().get("report_time")+"%");
        cnd.asc("productType");
        Pager pager = DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength());
        List<ComprehensiveDayReport> comprehensiveDayReports =query(cnd,pager);
        return new DataTables(param.getDraw(),dao().count(ComprehensiveDayReport.class),dao().count(ComprehensiveDayReport.class,cnd),comprehensiveDayReports);
    }
}
