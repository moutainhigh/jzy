package com.kaisa.kams.components.view.loan;

import lombok.Data;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Created by sunwanchao on 2016/12/12.
 */
@Data
public class Duration implements Comparable<Duration>{
    private int years;

    private int months;

    private int days;

    public Duration(int years,
                    int months,
                    int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    @Override
    public int compareTo(Duration o) {
        if (!(years == o.years)) {
            return years > o.years ? 1 : -1;
        }
        if (!(months == o.months)) {
            return months > o.months ? 1 : -1;
        }
        if (!(days == o.days)) {
            return days > o.days ? 1 : -1;
        }
        return 0;
    }

    @Deprecated
    public int getTotalDays() {
        return getTotalMonths() * TimeConstant.DAYS_PER_MONTH + days;
    }

    public int getTotalMonths() {
        return years * TimeConstant.MONTHS_PER_YEAR + months;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(years).append(months).append(days).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Duration other = (Duration) obj;
        if (this.years != other.years) {
            return false;
        }
        if (this.months != other.months) {
            return false;
        }
        return this.days == other.days;
    }
}
