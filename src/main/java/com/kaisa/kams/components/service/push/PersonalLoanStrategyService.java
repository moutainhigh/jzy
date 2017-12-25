package com.kaisa.kams.components.service.push;

import com.kaisa.kams.components.utils.StringFormatUtils;
import com.kaisa.kams.models.LoanBorrower;
import com.kaisa.kams.models.push.LoanPush;

import org.apache.commons.lang.StringUtils;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.Map;

/**
 * 个人贷项目获取推单初始化信息
 * @author pengyueyang created on 2017/11/13.
 */
@IocBean(fields = "dao")
public class PersonalLoanStrategyService extends BaseItemTypeStrategyService {

    private final String KEY_LOAN_USE = "loanUse";
    private final String KEY_PROJECT_DESC = "projectDesc";
    private final String LOAN_USE = "\"loanuse\" : {\"type\":\"text\",\"title\":\"借款用途\",\"value\":\"${loanUse}\"},";
    private final String PROJECT_DESC = "\"projectdesc\" : {\"type\":\"text\",\"title\":\"项目说明\",\"value\":\"${projectDesc}\"}}";

    @Override
    public String getInitContent(LoanPush loanPush) {
        String loanId = loanPush.getLoanId();
        LoanBorrower masterLoanBorrower = getMasterLoanBorrowerByLoanId(loanId);
        Map<String, String> businessInfo = getBusinessInfo(loanId);
        StringBuilder builder = new StringBuilder();
        builder.append(getCommonBasic(masterLoanBorrower));
        builder.append(getLoanUse(businessInfo.get("use_of_loan")));
        builder.append(getProjectDesc(businessInfo.get("person_content")));
        return builder.toString();
    }

    private String getLoanUse(String purpose) {
        purpose = getPurposeCH(purpose);
        return StringFormatUtils.format(LOAN_USE, KEY_LOAN_USE, purpose);
    }

    private String getProjectDesc(String projectDesc) {
        if (StringUtils.isEmpty(projectDesc)) {
            projectDesc = "【项目说明】";
        }
        return StringFormatUtils.format(PROJECT_DESC, KEY_PROJECT_DESC, projectDesc);
    }
}
