package com.oms.orderMS.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.oms.orderMS.entity.ProductsOrdered;
import com.oms.orderMS.entity.ProductsOrderedPK;

public interface ProductsOrderedRepository extends CrudRepository<ProductsOrdered, ProductsOrderedPK>{

	List<ProductsOrdered> findByBuyerID(String buyerID);

}

