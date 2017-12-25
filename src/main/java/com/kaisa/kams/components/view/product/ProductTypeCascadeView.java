package com.kaisa.kams.components.view.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by pengyueyang on 2017/6/7.
 * 产品大类产品级联view
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeCascadeView {

    private String productTypeId;

    private String productTypeName;

    private List<ProductCascadeView> products;
}
