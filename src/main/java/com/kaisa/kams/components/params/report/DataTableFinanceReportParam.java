package com.kaisa.kams.components.params.report;


import com.kaisa.kams.components.params.base.DataTableBaseParam;
import com.kaisa.kams.components.utils.TimeUtils;

import org.apache.commons.lang.StringUtils;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wangqx on 2017/10/18.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataTableFinanceReportParam extends DataTableBaseParam {

    private String reportName;

    private String productTypeName;

    private String reportTime;

    private Date beginDateTime;

    private Date endDateTime;

    private String loanSubject;

    private String status;

    private String[] loanedStatus;

    public Date getBeginDateTime() {
        return TimeUtils.getQueryStartDateTime(reportTime);
    }

    public Date getEndDateTime() {
        return TimeUtils.getQueryEndDateTime(reportTime);
    }

    public String[] getLoanedStatus() {
        if (StringUtils.isNotEmpty(status)) {
            return status.split(",");
        }
        return null;
    }



}
