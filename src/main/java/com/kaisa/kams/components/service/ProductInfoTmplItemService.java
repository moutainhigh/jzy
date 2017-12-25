package com.kaisa.kams.components.service;

import com.kaisa.kams.models.ProductInfoTmplItem;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.service.IdNameEntityService;

/**
 * Created by weid on 2016/11/29.
 */
@IocBean(fields="dao")
public class ProductInfoTmplItemService extends IdNameEntityService<ProductInfoTmplItem> {
    private static final Log log = Logs.get();

}
