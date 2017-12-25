package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.HouseInfo;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.nutz.ioc.loader.annotation.IocBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 红本项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/10.
 */
@IocBean(fields = "dao")
public class HouseMortgageLoanStrategyService extends BaseItemTypeStrategyService {

    private final String KEY_LOAN_USE = "loanUse";
    private final String LOAN_USE = "\"loanuse\" : {\"type\":\"text\",\"title\":\"借款用途\",\"value\":\"${loanUse}\"},";
    private final String PROJECT_DESC = "\"projectdesc\" : {\"type\":\"text\",\"title\":\"项目说明\",\"value\":\"本项目为红本抵押贷款，" +
            "借款人自愿抵押其房产位于${houseInfo}。借款人申请${loanAmount}万元借款用于资金周转.借款人${borrowers}" +
            "个人征信良好，房产查档正常，已作全权委托公证。房产已办理抵押给深圳市富昌小额贷款有限公司，风控措施齐全。\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower masterLoanBorrower = getMasterLoanBorrowerByLoanId(loanId);
        Map<String, String> businessInfo = getBusinessInfo(loanId);
        StringBuilder builder = new StringBuilder();
        builder.append(getCommonBasic(masterLoanBorrower));
        List<HouseInfo> houseInfoList = getHouseList(loanId);
        builder.append(getMortgageHouseInfo(houseInfoList));
        builder.append(getLoanUse(LOAN_USE, businessInfo.get("use_of_loan")));
        builder.append(getProjectDesc(houseInfoList, loanId));
        return builder.toString();
    }

    private String getLoanUse(String source, String useOfLoan) {
        useOfLoan = getPurposeCH(useOfLoan);
        return StringFormatUtils.format(source, KEY_LOAN_USE, useOfLoan);
    }

    private String getProjectDesc(List<HouseInfo> houseInfoList, String loanId) {
        HashMap<String, String> valueMap = new HashMap<>(3);
        valueMap.put("houseInfo", getProjectDescHouseInfo(houseInfoList));
        valueMap.put("loanAmount", getLoanAmountWithTenThousands(loanId, "【放款金额（转换成万元）】"));
        valueMap.put("borrowers", getAllBorrowers(loanId));
        return StringFormatUtils.format(PROJECT_DESC, valueMap);
    }

}
