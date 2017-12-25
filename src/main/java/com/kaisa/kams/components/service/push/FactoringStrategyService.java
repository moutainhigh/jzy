package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.Borrower;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 保理项目推单获取初始信息
 * @author pengyueyang created on 2017/11/13.
 */
@IocBean(fields = "dao")
public class FactoringStrategyService extends BaseItemTypeStrategyService {

    private static final Logger log = LoggerFactory.getLogger(FactoringStrategyService.class);

    private final String PROJECT_DESC = "{\"projectdesc\" : {\"type\":\"text\",\"title\":\"项目简介\",\"value\":\"债权人基本情况：${companyInfo}\\n" +
            "应收账款信息：${projectInfo}共计应收账款${receiveAmount}元，原债权人已与保理公司就此应收账款签订应收账款转让及回购协议，" +
            "保理公司将此应收账款收益权于平台进行转让融资，融资额度${loanAmount}元，折价率为${discountRate}。\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower masterLoanBorrower = getMasterLoanBorrowerByLoanId(loanId);
        Map<String, String> businessInfo = getBusinessInfo(loanId);
        Map<String, String> valuesMap = new HashMap<>(5);
        valuesMap.put("companyInfo", getCompanyInfo(masterLoanBorrower.getBorrowerId()));
        valuesMap.put("projectInfo", businessInfo.get("item_content"));

        String receiveAmount = businessInfo.get("re_amount");
        BigDecimal loanAmount = getLoanAmount(loanId);
        valuesMap.put("receiveAmount", receiveAmount);
        valuesMap.put("loanAmount", getLoanAmountDesc(loanAmount));
        valuesMap.put("discountRate", getDiscountRate(receiveAmount, loanAmount));
        return StringFormatUtils.format(PROJECT_DESC, valuesMap);
    }

    private String getLoanAmountDesc(BigDecimal loanAmount) {
        if (null == loanAmount || BigDecimal.ZERO.equals(loanAmount)) {
            return "【放款金额】";
        }
        return loanAmount.toString();
    }

    private String getDiscountRate(String receiveAmount, BigDecimal loanAmount) {
        String defaultValue = "【放款金额/应收账款金额】";
        if (StringUtils.isEmpty(receiveAmount) || null == loanAmount || BigDecimal.ZERO.equals(loanAmount)) {
            return defaultValue;
        }
        try {
            BigDecimal receiveAmountDecimal = new BigDecimal(receiveAmount);
            if (BigDecimal.ZERO.equals(receiveAmountDecimal)) {
                return defaultValue;
            }
            return loanAmount.divide(receiveAmountDecimal).setScale(2, RoundingMode.HALF_UP).toString();
        } catch (NumberFormatException e) {
            log.error("Convert receiveAmount String to BigDecimal error:{}", e.getMessage());
            return defaultValue;
        }
    }

    private String getCompanyInfo(String borrowerId) {
        String defaultValue = "【公司基本情况】";
        if (StringUtils.isEmpty(borrowerId)) {
            return defaultValue;
        }
        Borrower borrower = dao().fetch(Borrower.class, borrowerId);
        if (null == borrower || StringUtils.isEmpty(borrower.getCompanyProfiles())) {
            return defaultValue;
        }
        return borrower.getCompanyProfiles();
    }

}
