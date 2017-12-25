package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.Enterprise;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Cnd;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Criteria;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.List;

/**
 * 票据项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/13.
 */
@IocBean(fields = "dao")
public class BillStrategyService extends BaseItemTypeStrategyService {

    private final String KEY_PAYER = "payer";
    private final String KEY_HONOUR_INFO = "honourInfo";
    private final String KEY_REVENUE_PROFIT = "revenueProfit";
    private final String KEY_COMPANY_INFO = "companyInfo";
    private final String BASIC = "{\"basic\" : {\"type\":\"text\",\"title\":\"承兑企业\",\"value\":\"${payer}\"},";
    private final String REVENUE_PROFIT = "\"revenueprofit\" : {\"type\":\"text\",\"title\":\"营收及利润\",\"value\":\"${revenueProfit}。\"},";
    private final String COMPANY_INFO = "\"companyinfo\" : {\"type\":\"text\",\"title\":\"企业简介\",\"value\":\"${companyInfo}\"},";
    private final String HONOUR_INFO = "\"honourinfo\" : {\"type\":\"text\",\"title\":\"兑付信息\",\"value\":\"${honourInfo}到期无条件兑付\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower borrower = getMasterLoanBorrowerByLoanId(loanId);
        String payer = borrower.getName();
        StringBuilder builder = new StringBuilder();
        builder.append(StringFormatUtils.format(BASIC, KEY_PAYER, payer));
        String payers = getPayers(loanId);
        List<Enterprise> enterprises = getEnterpriseListByName(payers);
        builder.append(StringFormatUtils.format(REVENUE_PROFIT, KEY_REVENUE_PROFIT, getRevenueProfit(enterprises)));
        builder.append(StringFormatUtils.format(COMPANY_INFO, KEY_COMPANY_INFO, getCompanyInfo(enterprises)));
        builder.append(StringFormatUtils.format(HONOUR_INFO, KEY_HONOUR_INFO, getHonourInfo(payers)));
        return builder.toString();
    }

    private String getCompanyInfo(List<Enterprise> enterprises) {
        String defaultValue = "【公司基本情况】";
        if (CollectionUtils.isEmpty(enterprises)) {
            return defaultValue;
        }
        StringBuilder companyInfo = new StringBuilder();
        int size = enterprises.size();
        String semicolon = ";";
        for (int i = 0; i < size; i++) {
            Enterprise enterprise = enterprises.get(i);
            if (i == size - 1) {
                semicolon = "";
            }
            if (StringUtils.isNotEmpty(enterprise.getComputerBasic())) {
                companyInfo.append(enterprise.getComputerBasic()).append(semicolon);
            }
        }
        if (StringUtils.isEmpty(companyInfo.toString())) {
            return defaultValue;
        }
        return companyInfo.toString();
    }

    private String getRevenueProfit(List<Enterprise> enterprises) {
        String defaultValue = "【年度】年营业收入【营业收入】亿元，净利润【扣非净利润】万元";
        if (CollectionUtils.isEmpty(enterprises)) {
            return defaultValue;
        }
        StringBuilder revenueProfit = new StringBuilder();
        String year = "年营业收入";
        String revenue = "亿元，净利润";
        String profit = "万元";
        int size = enterprises.size();
        String semicolon = ";";
        for (int i = 0; i < size; i++) {
            Enterprise enterprise = enterprises.get(i);
            if (i == size - 1) {
                semicolon = "";
            }
            if (StringUtils.isNotEmpty(enterprise.getYear())) {
                revenueProfit.append(enterprise.getYear()).append(year);
            }
            if (null != enterprise.getBusinessIncome()) {
                revenueProfit.append(enterprise.getBusinessIncome()).append(revenue);
            }
            if (null != enterprise.getNotNetProfit()) {
                revenueProfit.append(enterprise.getNotNetProfit()).append(profit).append(semicolon);
            }
        }
        if (StringUtils.isEmpty(revenueProfit.toString())) {
            return defaultValue;
        }
        return revenueProfit.toString();
    }

    private String getPayers(String loanId) {
        String defaultValue = "";
        if (StringUtils.isEmpty(loanId)) {
            return defaultValue;
        }
        Sql sql = Sqls.fetchString("select GROUP_CONCAT(payer) from sl_bill_loan_repay where loanId=@loanId order by payer");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        String result = sql.getString();
        if (StringUtils.isEmpty(result)) {
            return defaultValue;
        }
        return result;
    }

    private String getHonourInfo(String payers) {
        String defaultValue = "【开票企业/付款人】";
        if (StringUtils.isEmpty(payers)) {
            return defaultValue;
        }
        return payers.replaceAll(",", ";");
    }

    private List<Enterprise> getEnterpriseListByName(String payers) {
        if (StringUtils.isEmpty(payers)) {
            return null;
        }
        Criteria cri = Cnd.cri();
        cri.where().andIn("name", payers.split(","));
        cri.getOrderBy().asc("name");
        return dao().query(Enterprise.class, cri);
    }
}
