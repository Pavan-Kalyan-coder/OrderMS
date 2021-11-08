package com.oms.orderMS.service;

import java.util.List;

import com.oms.orderMS.dto.CartDTO;
import com.oms.orderMS.dto.OrderDTO;
import com.oms.orderMS.dto.OrderPlacedDTO;
import com.oms.orderMS.dto.ProductDTO;
import com.oms.orderMS.exception.OrderMsException;

public interface OrderService {
	
	public List<OrderDTO> viewAllOrders() throws OrderMsException;

	public OrderPlacedDTO placeOrder(List<ProductDTO> productList, List<CartDTO> cartList, OrderDTO order) throws OrderMsException;

	public List<OrderDTO> viewOrdersByBuyer(String buyerId)throws OrderMsException;

	public OrderDTO viewOrder(String orderId) throws OrderMsException;

	public String reOrder(String buyerId, String orderId) throws OrderMsException;

	public String deleteOrder(String buyerID);

	public String deleteProductsOrdered(String buyerID);

	public OrderDTO viewTempOrdersByBuyer(String buyerID);

	public String updateTempOrdersByBuyer(OrderDTO orderDTO);

	public OrderPlacedDTO placeOrder1(List<ProductDTO> productList, List<CartDTO> cartList, OrderDTO orderDTO) throws OrderMsException;

	public List<OrderDTO> viewAllTempOrders() throws OrderMsException;


}
