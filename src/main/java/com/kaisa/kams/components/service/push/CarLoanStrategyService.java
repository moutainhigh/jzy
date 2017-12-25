package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.nutz.ioc.loader.annotation.IocBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 车贷项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/9.
 */
@IocBean(fields = "dao")
public class CarLoanStrategyService extends BaseItemTypeStrategyService {

    private final String KEY_PURPOSE = "purpose";


    private final String TYPE = "\"type\" : {\"type\":\"text\",\"title\":\"借款用途\",\"value\":\"${purpose}\"},";
    private final String MORTGAGE = "\"mortgage\" : {\"type\":\"text\",\"title\":\"抵押物情况\",\"value\":\"车辆品牌：${brand}\\n评估价值：${amount}\\n登记日期：【登记日期】\"},";
    private final String LOAN_DESC = "\"loandesc\" : {\"type\":\"text\",\"title\":\"借款说明\",\"value\":\"借款人现居住${address}，其个人资质已通过我平台风控部审核，个人自愿抵押个人名下${brand}一台，该车辆已使用【根据登记日期（系统缺失）计算得到】年，保费正常缴纳，无重大违章信息，市场评估价约${amount}元，相关抵押手续已办理。借款人本次借款【推单金额】万元用于${purpose}，用途明确。\\n" +
            "因车贷借款特殊性，借款人可能随时提前还款以解押车辆，若借款人申请提前还款，投资人将提前回收本金，并获当期利息收益。\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower masterLoanBorrower = getMasterLoanBorrowerByLoanId(loanId);
        Map<String, String> businessInfo = getBusinessInfo(loanId);
        StringBuilder builder = new StringBuilder();
        builder.append(getCommonBasic(masterLoanBorrower));
        builder.append(getType(businessInfo.get("use_of_loan")));
        builder.append(getMortgage(businessInfo));
        builder.append(getLoanDesc(masterLoanBorrower, businessInfo));
        return builder.toString();
    }

    private String getMortgage(Map<String, String> businessInfo) {
        Map<String, String> valuesMap = new HashMap<>(2);
        valuesMap.put("brand", businessInfo.get("car_type"));
        valuesMap.put("amount", businessInfo.get("car_value"));
        return StringFormatUtils.format(MORTGAGE, valuesMap);
    }

    private String getLoanDesc(LoanBorrower masterLoanBorrower, Map<String, String> businessInfo) {
        Map<String, String> valuesMap = new HashMap<>(4);
        valuesMap.put("address", masterLoanBorrower.getAddress());
        valuesMap.put("brand", businessInfo.get("car_type"));
        valuesMap.put("amount", businessInfo.get("car_value"));
        valuesMap.put(KEY_PURPOSE, getPurposeCH(businessInfo.get("use_of_loan")));
        return StringFormatUtils.format(LOAN_DESC, valuesMap);
    }

    private String getType(String purpose) {
        purpose = getPurposeCH(purpose);
        return StringFormatUtils.format(TYPE, KEY_PURPOSE, purpose);
    }

}
