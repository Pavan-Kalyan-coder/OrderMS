package com.oms.orderMS.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oms.orderMS.dto.CartDTO;
import com.oms.orderMS.dto.OrderDTO;
import com.oms.orderMS.dto.OrderPlacedDTO;
import com.oms.orderMS.dto.ProductDTO;
import com.oms.orderMS.entity.Order;
import com.oms.orderMS.entity.OrderTemp;
import com.oms.orderMS.entity.ProductsOrdered;
import com.oms.orderMS.exception.OrderMsException;
import com.oms.orderMS.repository.OrderRepository;
import com.oms.orderMS.repository.OrderTempRepository;
import com.oms.orderMS.repository.ProductsOrderedRepository;
import com.oms.orderMS.utility.OrderStatus;
import com.oms.orderMS.validator.OrderValidator;

@Service(value = "orderService")
@Transactional
public class OrderServiceImpl implements OrderService {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderTempRepository orderTempRepository;
	
	@Autowired
	private ProductsOrderedRepository prodOrderedRepository;
	

	@Override
	public List<OrderDTO> viewAllOrders() throws OrderMsException {
		Iterable<Order> orders = orderRepository.findAll();
		List<OrderDTO> dtoList = new ArrayList<>();
		orders.forEach(order -> {
			OrderDTO odto = new OrderDTO();
			odto.setOrderID(order.getOrderID());
			odto.setBuyerID(order.getBuyerID());
			odto.setAmount(order.getAmount());
			odto.setAddress(order.getAddress());
			odto.setDate(order.getDate());
			odto.setStatus(order.getStatus());
			dtoList.add(odto);			
		});
		if(dtoList.isEmpty()) throw new OrderMsException("No orders available");
		return dtoList;
	}

	@Override
	public OrderPlacedDTO placeOrder(List<ProductDTO> productList, List<CartDTO> cartList, OrderDTO orderDTO)
			throws OrderMsException {
		
		Order order = new Order();
		OrderValidator.validateOrder(orderDTO);
		order.setOrderID(orderDTO.getOrderID());
		order.setAddress(orderDTO.getAddress());
		order.setBuyerID(cartList.get(0).getBuyerID());
		order.setDate(LocalDate.now());
		order.setStatus(OrderStatus.ORDER_PLACED.toString());	
		order.setAmount(0f);
		
		List<ProductsOrdered> productsOrdered = new ArrayList<>();
		for(int i = 0; i<cartList.size();i++) {
			OrderValidator.validateStock(cartList.get(i), productList.get(i));			
			order.setAmount(order.getAmount()+(cartList.get(i).getQuantity()*productList.get(i).getPrice()));
			
			ProductsOrdered prodO = new ProductsOrdered();
			prodO.setBuyerID(cartList.get(i).getBuyerID());
			prodO.setProductID(productList.get(i).getProductID());
			prodO.setSellerID(productList.get(i).getSellerID());
			prodO.setQuantity(cartList.get(i).getQuantity());
			productsOrdered.add(prodO);				
		}		
		
		prodOrderedRepository.saveAll(productsOrdered);
		orderRepository.save(order);
		
		OrderPlacedDTO orderPlaced = new OrderPlacedDTO();
		orderPlaced.setBuyerID(order.getBuyerID());
		orderPlaced.setOrderID(order.getOrderID());
		Integer rewardPts = (int) (order.getAmount()/100);		
		orderPlaced.setRewardPoints(rewardPts);
		
		
		return orderPlaced;
	}

	@Override
	public OrderPlacedDTO placeOrder1(List<ProductDTO> productList, List<CartDTO> cartList, OrderDTO orderDTO)
			throws OrderMsException {
		
		Order order = new Order();
		OrderTemp orderTemp = new OrderTemp();
		
		OrderValidator.validateOrder(orderDTO);
		
		order.setOrderID(orderDTO.getOrderID()); orderTemp.setOrderID(orderDTO.getOrderID());
		order.setAddress(orderDTO.getAddress()); orderTemp.setAddress(orderDTO.getAddress());
		order.setBuyerID(cartList.get(0).getBuyerID()); orderTemp.setBuyerID(orderDTO.getBuyerID());
		order.setDate(LocalDate.now()); orderTemp.setDate(orderDTO.getDate());
		order.setStatus(OrderStatus.ORDER_PLACED.toString()); orderTemp.setStatus(orderDTO.getStatus());	
		order.setAmount(orderDTO.getAmount()); orderTemp.setAmount(orderDTO.getAmount());
		
		List<ProductsOrdered> productsOrdered = new ArrayList<>();
		for(int i = 0; i<cartList.size();i++) {
			OrderValidator.validateStock(cartList.get(i), productList.get(i));			
			//order.setAmount(order.getAmount()+(cartList.get(i).getQuantity()*productList.get(i).getPrice()));
			
			ProductsOrdered prodO = new ProductsOrdered();
			prodO.setBuyerID(cartList.get(i).getBuyerID());
			prodO.setProductID(productList.get(i).getProductID());
			prodO.setSellerID(productList.get(i).getSellerID());
			prodO.setQuantity(cartList.get(i).getQuantity());
			productsOrdered.add(prodO);				
		}		
		
		prodOrderedRepository.saveAll(productsOrdered);
		orderRepository.save(order);
		orderTempRepository.delete(orderTemp);
		
		OrderPlacedDTO orderPlaced = new OrderPlacedDTO();
		orderPlaced.setBuyerID(order.getBuyerID());
		orderPlaced.setOrderID(order.getOrderID());
		Integer rewardPts = (int) (order.getAmount()/100);		
		orderPlaced.setRewardPoints(rewardPts);
		
		
		return orderPlaced;
	}
	
	@Override
	public List<OrderDTO> viewOrdersByBuyer(String buyerId) throws OrderMsException {
		List<Order> orders = orderRepository.findByBuyerID(buyerId);
		if(orders.isEmpty()) throw new OrderMsException("No orders available for given BuyerID");
		List<OrderDTO> dtoList = new ArrayList<>();
		orders.forEach(order->{
			OrderDTO odto = new OrderDTO();
			odto.setOrderID(order.getOrderID());
			odto.setBuyerID(order.getBuyerID());
			odto.setAmount(order.getAmount());
			odto.setAddress(order.getAddress());
			odto.setDate(order.getDate());
			odto.setStatus(order.getStatus());
			dtoList.add(odto);
		});
		return dtoList;
	}

	@Override
	public OrderDTO viewOrder(String orderId) throws OrderMsException {
		Optional<Order> optional = orderRepository.findByOrderID(orderId);
		Order order = optional.orElseThrow(()->new OrderMsException("Order does not exist"));
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderID(order.getOrderID());
		orderDTO.setBuyerID(order.getBuyerID());
		orderDTO.setAmount(order.getAmount());
		orderDTO.setAddress(order.getAddress());
		orderDTO.setDate(order.getDate());
		orderDTO.setStatus(order.getStatus());		
		return orderDTO;
	}

	@Override
	public String reOrder(String buyerId, String orderId) throws OrderMsException {
		Optional<Order> optional = orderRepository.findByOrderID(orderId);
		Order order = optional.orElseThrow(()->new OrderMsException("Order does not exist for the given buyer"));
		Order reorder = new Order();
		reorder.setOrderID(order.getOrderID());
		reorder.setBuyerID(order.getBuyerID());
		reorder.setAmount(order.getAmount());
		reorder.setAddress(order.getAddress());
		reorder.setDate(LocalDate.now());
		reorder.setStatus(order.getStatus());
		
		orderRepository.save(reorder);		
		return reorder.getOrderID();
	}

	@Override
	public String deleteOrder(String buyerID)
	{
		List<Order> orders = orderRepository.findByBuyerID(buyerID); 
		for(Order order : orders)
		{
			orderRepository.delete(order);
		}
		return "deleted orders placed by buyer (if any)";
		
	}
	
	@Override
	public String deleteProductsOrdered(String buyerID)
	{
		List<ProductsOrdered> productsOrderedList = prodOrderedRepository.findByBuyerID(buyerID); 
		for(ProductsOrdered  productsOrdered: productsOrderedList)
		{
			prodOrderedRepository.delete(productsOrdered);
		}
		return "deleted previous orders of buyer (if any)";
		
	}
	
	@Override
	public OrderDTO viewTempOrdersByBuyer(String buyerID)
	{
		OrderTemp order = orderTempRepository.findByBuyerID(buyerID);
		if(order == null)
		{
			return null;
		}
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderID(order.getOrderID());
		orderDTO.setBuyerID(order.getBuyerID());
		orderDTO.setAmount(order.getAmount());
		orderDTO.setAddress(order.getAddress());
		orderDTO.setDate(order.getDate());
		orderDTO.setStatus(order.getStatus());

		return orderDTO;
	}
	
	@Override
	public String updateTempOrdersByBuyer(OrderDTO orderDTO)
	{
		//Order fromRepo = orderTempRepository.findByBuyerID(orderDTO.getBuyerID());
		OrderTemp orderTemp = new OrderTemp();
		orderTemp.setOrderID(orderDTO.getOrderID());
		orderTemp.setBuyerID(orderDTO.getBuyerID());
		orderTemp.setAmount(orderDTO.getAmount());
		orderTemp.setAddress(orderDTO.getAddress());
		orderTemp.setDate(orderDTO.getDate());
		orderTemp.setStatus(orderDTO.getStatus());
		orderTempRepository.save(orderTemp);
		
		return null;
	}
	
	
	// return all temp orders
	
	@Override
	public List<OrderDTO> viewAllTempOrders() throws OrderMsException {
		Iterable<OrderTemp> orders = orderTempRepository.findAll();
		
		List<OrderDTO> dtoList = new ArrayList<>();
		orders.forEach(orderTemp -> {
			OrderDTO odto = new OrderDTO();
			odto.setOrderID(orderTemp.getOrderID());
			odto.setBuyerID(orderTemp.getBuyerID());
			odto.setAmount(orderTemp.getAmount());
			odto.setAddress(orderTemp.getAddress());
			odto.setDate(orderTemp.getDate());
			odto.setStatus(orderTemp.getStatus());
			dtoList.add(odto);			
		});
		
//		if(dtoList.isEmpty()) throw new OrderMsException("No orders available");
		return dtoList;
	}
	
	
}
