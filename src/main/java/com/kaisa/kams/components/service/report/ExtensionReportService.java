package com.kaisa.kams.components.service.report;

import com.kaisa.kams.components.params.report.DataTableFinanceReportParam;
import com.kaisa.kams.components.utils.DataTablesUtil;
import com.kaisa.kams.components.utils.DateUtil;
import com.kaisa.kams.components.view.report.ExtensionReportRepayData;
import com.kaisa.kams.components.view.report.ExtensionReportView;
import com.kaisa.kams.enums.LoanRepayMethod;
import com.kaisa.kams.enums.LoanTermType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.service.Service;

import java.util.List;

/**
 * @author pengyueyang created on 2017/12/11.
 */
@IocBean(fields = "dao")
public class ExtensionReportService extends Service {

    public int getCount(DataTableFinanceReportParam param) {
        String querySql = "select count(*) from sl_loan_extension e left join sl_loan l on e.loanId=l.id";
        Sql sql = Sqls.fetchInt(querySql);
        sql.setCondition(constructQueryParam(param));
        dao().execute(sql);
        return sql.getInt();
    }

    public List<ExtensionReportView> getList(DataTableFinanceReportParam param) {
        String querySql = "select e.loanId,l.code code,p.name productName,c.name source,l.saleName salesman," +
                "b.name borrower,b.certifNumber idNumber,l.actualAmount loanAmount," +
                "l.loanTime,e.position+1 extensionCount,l.repayMethod loanRepayMethod,\n" +
                "e.term extensionTerm,e.termType extensionTermType,l.clearDate actualClearDate," +
                "e.repayMethod,s.name loanSubject,l.loanStatus,olr.maxPosition\n" +
                "from sl_loan_extension e\n" +
                "left join sl_loan l on e.loanId=l.id\n" +
                "left join sl_product p on l.productId=p.id\n" +
                "left join sl_channel c on l.channelId=c.id\n" +
                "left join sl_loan_borrower b on l.masterBorrowerId=b.id\n" +
                "left join sl_loan_subject s on s.id=l.loanSubjectId\n" +
                "left join (select loanId,max(position)+1 maxPosition from sl_old_loan_repay GROUP BY loanId) olr\n" +
                "on olr.loanId=e.loanId";
        Sql sql = Sqls.queryEntity(querySql);
        Entity<ExtensionReportView> entity = dao().getEntity(ExtensionReportView.class);
        sql.setEntity(entity);
        sql.setPager(DataTablesUtil.getDataTableToPager(param.getStart(), param.getLength()));
        Criteria cnd = constructQueryParam(param);
        cnd = setQueryOrder(cnd);
        sql.setCondition(cnd);
        dao().execute(sql);
        return convertList(sql.getList(ExtensionReportView.class));
    }

    private List<ExtensionReportView> convertList(List<ExtensionReportView> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        list.forEach(extensionReportView -> convert(extensionReportView));
        return list;
    }

    private void convert(ExtensionReportView extensionReportView) {
        String querySql = "select sum(r.amount) extensionAmount,sum(r.repayAmount) extensionRepayAmount," +
                "sum(r.interest) extensionInterest,sum(r.repayInterest) extensionRepayInterest,max(r.dueDate) extensionDueDate from sl_loan_repay r";
        Criteria cnd = Cnd.cri();
        cnd.where().andEquals("r.loanId", extensionReportView.getLoanId());
        if (extensionReportView.getMaxPosition() > extensionReportView.getExtensionCount()) {
            querySql = "select sum(r.amount) extensionAmount,sum(r.repayAmount) extensionRepayAmount," +
                    "sum(r.interest) extensionInterest,sum(r.repayInterest) extensionRepayInterest,max(r.dueDate) extensionDueDate  from sl_old_loan_repay r";
            cnd.where().andEquals("r.position", extensionReportView.getExtensionCount());
        }
        Sql sql = Sqls.fetchEntity(querySql);
        Entity<ExtensionReportRepayData> entity = dao().getEntity(ExtensionReportRepayData.class);
        sql.setEntity(entity);
        sql.setCondition(cnd);
        dao().execute(sql);
        ExtensionReportRepayData data = sql.getObject(ExtensionReportRepayData.class);
        richExtensionReportView(extensionReportView, data);
    }

    private void richExtensionReportView(ExtensionReportView extensionReportView, ExtensionReportRepayData data) {
        extensionReportView.setExtensionAmount(data.getExtensionAmount());
        extensionReportView.setExtensionRepayAmount(data.getExtensionRepayAmount());
        extensionReportView.setExtensionRemainAmount(data.getExtensionRemainAmount());
        extensionReportView.setExtensionInterest(data.getExtensionInterest());
        extensionReportView.setExtensionRepayInterest(data.getExtensionRepayInterest());
        extensionReportView.setExtensionRemainInterest(data.getExtensionRemainInterest());
        extensionReportView.setExtensionDueDate(data.getExtensionDueDate());
        long totalDays = DateUtil.daysBetweenTowDate(extensionReportView.getLoanTime(), extensionReportView.getActualClearDate());
        if (totalDays>0l) {
            extensionReportView.setTotalDays(String.valueOf(totalDays));
        }
        extensionReportView.setExtensionTermStr(LoanTermType.getTermStr(extensionReportView.getExtensionTermType(),extensionReportView.getExtensionTerm()));
        if (StringUtils.isEmpty(extensionReportView.getRepayMethod())) {
            extensionReportView.setRepayMethod(extensionReportView.getLoanRepayMethod().getDescription());
        } else {
            if (LoanRepayMethod.isInLoanRepayMethod(extensionReportView.getRepayMethod())) {
                extensionReportView.setRepayMethod(LoanRepayMethod.valueOf(extensionReportView.getRepayMethod()).getDescription());
            }
        }
        extensionReportView.setLoanStatusStr(extensionReportView.getLoanStatus().getDescription());
    }

    private Criteria constructQueryParam(DataTableFinanceReportParam param) {
        Criteria cri = Cnd.cri();
        if (!ArrayUtils.isEmpty(param.getLoanedStatus())) {
            cri.where().andIn("l.loanStatus", param.getLoanedStatus());
        }
        if (StringUtils.isNotEmpty(param.getReportTime())) {
            cri.where().andBetween("e.createTime", param.getBeginDateTime(), param.getEndDateTime());
        }
        if (StringUtils.isEmpty(param.getLoanSubject())) {
            cri.where().andEquals("l.loanSubjectId", param.getLoanSubject());
        }
        return cri;
    }

    public Criteria setQueryOrder(Criteria cri) {
        cri.getOrderBy().desc("e.createTime");
        return cri;
    }
}
