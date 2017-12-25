package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.lang.StringUtils;
import org.nutz.dao.Sqls;
import org.nutz.dao.sql.Sql;
import org.nutz.ioc.loader.annotation.IocBean;

/**
 * 银票项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/13.
 */
@IocBean(fields = "dao")
public class BankBillStrategyService extends BaseItemTypeStrategyService {

    private final String KEY_PAYER = "payer";
    private final String BASIC = "{\"basic\" : {\"type\":\"text\",\"title\":\"项目信息\",\"value\":\"由${payer}承兑，到期无条件兑付。\"},";
    private final String HONOUR_INFO = "\"honourinfo\" : {\"type\":\"text\",\"title\":\"兑付信息\",\"value\":\"${payer}到期无条件兑付\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String payer = getBanksName(loanPush.getLoanId());
        StringBuilder builder = new StringBuilder();
        builder.append(StringFormatUtils.format(BASIC, KEY_PAYER, payer));
        builder.append(StringFormatUtils.format(HONOUR_INFO, KEY_PAYER, payer));
        return builder.toString();
    }

    private String getBanksName(String loanId) {
        String defaultValue = "【付款人】";
        if (StringUtils.isEmpty(loanId)) {
            return defaultValue;
        }
        Sql sql = Sqls.fetchString("select GROUP_CONCAT(bankName) from sl_bill_loan_repay where loanId=@loanId");
        sql.setParam("loanId", loanId);
        dao().execute(sql);
        String result = sql.getString();
        if (StringUtils.isEmpty(result)) {
            return defaultValue;
        }
        return result;
    }


}
