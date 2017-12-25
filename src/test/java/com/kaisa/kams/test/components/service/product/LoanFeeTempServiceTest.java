package com.kaisa.kams.test.components.service.product;

import com.alibaba.druid.filter.config.ConfigTools;
import com.kaisa.kams.components.service.LoanFeeTempService;
import com.kaisa.kams.components.service.LoanService;
import com.kaisa.kams.enums.FeeChargeNode;
import com.kaisa.kams.test.BaseTest;

import org.junit.Test;
import org.nutz.ioc.loader.annotation.Inject;

/**
 * Created by wangqx on 2017/6/19.
 */
public class LoanFeeTempServiceTest extends BaseTest {
    @Inject
    LoanFeeTempService loanFeeTempService;

    @Test
    public void testQueryByLoanIdAndChargeNode() {
        assertEquals(null,loanFeeTempService.queryByLoanIdAndChargeNode("11", FeeChargeNode.REPAY_NODE));
    }

}
