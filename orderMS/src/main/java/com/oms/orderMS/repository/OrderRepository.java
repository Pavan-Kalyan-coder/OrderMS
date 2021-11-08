package com.oms.orderMS.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.oms.orderMS.entity.Order;

public interface OrderRepository extends CrudRepository<Order, String>{

	public List<Order> findByBuyerID(String buyerId);

	public Optional<Order> findByOrderID(String orderId);

}
