package com.kaisa.kams.components.params.report;


import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.Date;

/**
 * Created by wangqx on 2017/10/18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableBusinessReportParam extends DataTableBaseParam {

    private String submitter;

    private String organizeId;

    private String agencyId;

    private String productName;

    private String saleName;

    private String reportTime;

    private String submitDate;

    private String clearedDate;

    private String actualLoanDate;

    private String expireDate;

    private String channelId;

    private Date beginDateTime;

    private Date endDateTime;

    private String loanSubjectId;

    private String billType;

    private String status;

    private String loanStatus;

    private String[] loanedStatus;

    private String source;

    public Date getBeginDateTime() {
        return TimeUtils.getQueryStartDateTime(reportTime);
    }

    public Date getEndDateTime() {
        return TimeUtils.getQueryEndDateTime(reportTime);
    }

    public Date getBeginSubmitDate() {
        return TimeUtils.getQueryStartDateTime(submitDate);
    }

    public Date getEndSubmitDate() {
        return TimeUtils.getQueryEndDateTime(submitDate);
    }

    public Date getBeginClearedDate() {
        return TimeUtils.getQueryStartDateTime(clearedDate);
    }

    public Date getEndClearedDate() {
        return TimeUtils.getQueryEndDateTime(clearedDate);
    }

    public Date getBeginActualLoanDate() {
        return TimeUtils.getQueryStartDateTime(actualLoanDate);
    }

    public Date getEndActualLoanDate() {
        return TimeUtils.getQueryEndDateTime(actualLoanDate);
    }

    public Date getBeginExpireDate() {
        return TimeUtils.getQueryStartDateTime(expireDate);
    }

    public Date getEndExpireDate() {
        return TimeUtils.getQueryEndDateTime(expireDate);
    }




}
