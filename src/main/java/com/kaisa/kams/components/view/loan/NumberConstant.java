package com.kaisa.kams.components.view.loan;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by sunwanchao on 2016/12/12.
 */
public interface NumberConstant {
    int PRECISION = 15;

    int SCALE = 4;

    MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    BigDecimal DEFAULT_ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);

    BigDecimal DEFAULT_ONE = BigDecimal.ONE.setScale(SCALE, ROUNDING_MODE);

    BigDecimal HALF = new BigDecimal(0.5);

    int RATE_FACTOR = 100;

    int MONEY_FACTOR = 100;
}
