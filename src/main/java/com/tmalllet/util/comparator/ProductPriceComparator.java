package com.tmalllet.util.comparator;


import java.util.Comparator;

import com.tmalllet.entity.Product;

public class ProductPriceComparator implements Comparator<Product> {

    @Override
    public int compare(Product p1, Product p2) {
        return (int) (p1.getPromotePrice() - p2.getPromotePrice());
    }
}
