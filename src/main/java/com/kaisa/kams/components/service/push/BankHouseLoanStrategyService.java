package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.House;
import com.kaisa.kams.models.HouseInfo;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.IocBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 赎楼贷项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/10.
 */
@IocBean(fields = "dao")
public class BankHouseLoanStrategyService extends BaseItemTypeStrategyService {

    private final Logger log = LoggerFactory.getLogger(BankHouseLoanStrategyService.class);
    private final String PROJECT_DESC = "\"projectdesc\" : {\"type\":\"text\",\"title\":\"项目说明\",\"value\":\"本项目是一种垫资服务，还款来源为银行房产抵押贷款。抵押房产位于" +
            "${houseInfo}，评估价约${houseAmount}万。借款人${borrowers}个人征信良好，" +
            "房产查档正常，已作全权委托公证，风控措施齐全。此项目第一还款来源为${approvalBank}已预授信的房产抵押贷款${approvalAmount}万元（审批文件已出）。\"},";
    private final String RISK_RULE = "\"riskrule\" : {\"type\":\"text\",\"title\":\"风控措施\",\"value\":\"一：${approvalBank}已批复的房产抵押贷款为第一还款来源；\\n二：合作方深圳市富昌小额贷款有限公司承诺到期回购此债权；\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower masterLoanBorrower = getMasterLoanBorrowerByLoanId(loanId);
        Map<String, String> businessInfo = getBusinessInfo(loanId);
        StringBuilder builder = new StringBuilder();
        builder.append(getCommonBasic(masterLoanBorrower));
        List<HouseInfo> houseInfoList = getHouseList(loanId);
        builder.append(getBankHouseInfo(houseInfoList));
        builder.append(getProjectDesc(houseInfoList, loanId, businessInfo));
        builder.append(getRiskRule(businessInfo.get("approval_bank")));
        return builder.toString();
    }

    private String getProjectDesc(List<HouseInfo> houseInfoList, String loanId, Map<String, String> businessInfo) {
        HashMap<String, String> valueMap = new HashMap<>(5);
        valueMap.put("houseInfo", getProjectDescHouseInfo(houseInfoList));
        valueMap.put("houseAmount", getHouseAmount(houseInfoList));
        valueMap.put("borrowers", getAllBorrowers(loanId));
        valueMap.put("approvalBank", businessInfo.get("approval_bank"));
        valueMap.put("approvalAmount", businessInfo.get("approval_amount"));
        return StringFormatUtils.format(PROJECT_DESC, valueMap);
    }

    private String getRiskRule(String approvalBank) {
        if (StringUtils.isEmpty(approvalBank)) {
            approvalBank = "【审批银行】";
        }
        return StringFormatUtils.format(RISK_RULE, "approvalBank", approvalBank);
    }

    private String getHouseAmount(List<HouseInfo> houseInfoList) {
        String defaultValue = "【房产估值】";
        if (CollectionUtils.isEmpty(houseInfoList)) {
            return defaultValue;
        }
        BigDecimal houseAmount = BigDecimal.ZERO;
        for (HouseInfo houseInfo : houseInfoList) {
            String price = houseInfo.getPrice();
            if (StringUtils.isEmpty(price)) {
                continue;
            }
            try {
                houseAmount = houseAmount.add(new BigDecimal(price));
            } catch (Exception e) {
                log.error("Convert String to BigDecimal error",e.getMessage());
            }

        }
        if (BigDecimal.ZERO.equals(houseAmount)) {
            return defaultValue;
        }
        return houseAmount.toString();
    }

}
