package com.oms.orderMS.repository;

import org.springframework.data.repository.CrudRepository;

import com.oms.orderMS.entity.OrderTemp;

public interface OrderTempRepository extends CrudRepository<OrderTemp, String>{

	OrderTemp findByBuyerID(String buyerID);

}
