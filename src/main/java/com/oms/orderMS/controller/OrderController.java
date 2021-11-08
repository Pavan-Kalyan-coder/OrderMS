package com.oms.orderMS.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.oms.orderMS.dto.CartDTO;
import com.oms.orderMS.dto.OrderDTO;
import com.oms.orderMS.dto.OrderPlacedDTO;
import com.oms.orderMS.dto.ProductDTO;
import com.oms.orderMS.service.OrderService;

@RestController
public class OrderController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private OrderService orderService;
	
//	@Autowired
//	DiscoveryClient client;
	
//	@Autowired
//	RestTemplate restTemplate;
	
	@Value("${user.uri}")
	String userUri;
	
	@Value("${product.uri}")
	String productUri;
	
	@PostMapping(value = "/orderMS/placeOrder/{buyerID}")
	public ResponseEntity<String> placeOrder(@PathVariable String buyerID, @RequestBody OrderDTO order){
		
		try {
//			List<ServiceInstance> userInstances=client.getInstances("USERMS");
//			ServiceInstance userInstance=userInstances.get(0);
//			URI userUri = userInstance.getUri();
			List<ProductDTO> productList = new ArrayList<>();
			
	
//			List<ServiceInstance> instances=client.getInstances("PRODUCTMS");
//			ServiceInstance instance=instances.get(0);
//			URI productUri = instance.getUri();
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<CartDTO[]> response = restTemplate.getForEntity(userUri+"/buyer/cart/getallproducts/" + buyerID, CartDTO[].class);
			CartDTO[] cartlist = response.getBody();
			List<CartDTO> cartList = new ArrayList<CartDTO>();
			for(int i=0;i<cartlist.length;i++)
			{
				cartList.add(cartlist[i]);
			}
			
			for(CartDTO cartDTO: cartList)
			{
				ProductDTO prod = new RestTemplate().getForObject(productUri+"/products/productbyid/" +cartDTO.getProductID(),ProductDTO.class) ; //getByProductID/{productID}
				productList.add(prod);
			}
			logger.info(String.valueOf(order.getOrderID()));
			OrderPlacedDTO orderPlaced = orderService.placeOrder(productList,cartList,order);
			
			cartList.forEach(item->{
				String result1 = new RestTemplate().getForObject(productUri+"/products/updateStock/{productId}/{quantity}", String.class, item.getProductID(), item.getQuantity()) ;
				logger.info(buyerID +" "+item.getProductID() + result1);
				new RestTemplate().delete(userUri+"/buyer/cart/remove/{buyerID}/{productID}", buyerID, item.getProductID());
			});			
	
			new RestTemplate().put(userUri+"/buyer/addrewardpoints/"+"{buyerID}"+"/"+"{rewardPoints}" ,null, buyerID, orderPlaced.getRewardPoints());

			return new ResponseEntity<>(orderPlaced.getOrderID(),HttpStatus.ACCEPTED);

		}
		catch(Exception e)
		{
			String newMsg = "There was some error";
			if(e.getMessage().equals("404 null"))
			{
				newMsg = "Error while placing the order";
			}
			return new ResponseEntity<>(newMsg,HttpStatus.UNAUTHORIZED);
		}		
		
	}
	
	@GetMapping(value = "/orderMS/viewAll")
	public ResponseEntity<List<OrderDTO>> viewAllOrder(){		
		try {
			List<OrderDTO> allOrders = orderService.viewAllOrders();
			return new ResponseEntity<>(allOrders,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	
	@GetMapping(value = "/orderMS/viewOrders/{buyerID}")
	public ResponseEntity<List<OrderDTO>> viewsOrdersByBuyerID(@PathVariable String buyerID){		
		try {
			List<OrderDTO> allOrders = orderService.viewOrdersByBuyer(buyerID);
			return new ResponseEntity<>(allOrders,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	
	@GetMapping(value = "/orderMS/viewOrder/{orderID}")
	public ResponseEntity<OrderDTO> viewsOrderByOrderID(@PathVariable String orderID){		
		try {
			OrderDTO allOrders = orderService.viewOrder(orderID);
			return new ResponseEntity<>(allOrders,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	
	
	@PostMapping(value = "/orderMS/reOrder/{buyerID}/{orderID}")
	public ResponseEntity<String> reOrder(@PathVariable String buyerID, @PathVariable String orderID){
		
		try {
			
			String id = orderService.reOrder(buyerID,orderID);
			return new ResponseEntity<>("Order ID: "+id,HttpStatus.ACCEPTED);
		}
		catch(Exception e)
		{
			return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
		}		
	}
	
	//delete orders placed by a buyer
	@DeleteMapping(value = "/orderMS/deleteorders/{buyerID}")
	public ResponseEntity<String> deleteOrder(@PathVariable String buyerID) {
		logger.info("deleting orders placed by ID {}", buyerID);
		String result;
		try 
		{
			result = orderService.deleteOrder(buyerID);
			return new ResponseEntity<String>(result, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}
	}
	
	//delete previous orders of a buyer 
	@DeleteMapping(value = "/orderMS/deleteproductsordered/{buyerID}")
	public ResponseEntity<String> deleteProductsOrdered(@PathVariable String buyerID) {
		logger.info("deleting a product with previous orders for ID {}", buyerID);
		String result;
		try 
		{
			result = orderService.deleteProductsOrdered(buyerID);
			return new ResponseEntity<String>(result, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}
	}
	
	//*****************************Kafka Consumer Uses These end points**********************************
	@GetMapping(value = "/orderMS/viewTempOrders/{buyerID}")
	public ResponseEntity<OrderDTO> viewsTempOrdersByBuyerID(@PathVariable String buyerID){		
		try {
			OrderDTO tempOrder = orderService.viewTempOrdersByBuyer(buyerID);
			return new ResponseEntity<>(tempOrder,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	
	@PutMapping(value = "/orderMS/updateTempOrders/{buyerID}")
	public ResponseEntity<String> updateTempOrdersByBuyerID(@PathVariable String buyerID, @RequestBody OrderDTO orderDTO){		
		try {
			System.out.println("gg1");
			String message = orderService.updateTempOrdersByBuyer(orderDTO);
			return new ResponseEntity<>(message,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	
	@PostMapping(value = "/orderMS/placeOrder1/{buyerID}")
	public ResponseEntity<String> placeOrder1(@PathVariable String buyerID){
		
		try {
//			List<ServiceInstance> userInstances=client.getInstances("USERMS");
//			ServiceInstance userInstance=userInstances.get(0);
//			URI userUri = userInstance.getUri();
			List<ProductDTO> productList = new ArrayList<>();
			
	
//			List<ServiceInstance> instances=client.getInstances("PRODUCTMS");
//			ServiceInstance instance=instances.get(0);
//			URI productUri = instance.getUri();
			
			OrderDTO order = orderService.viewTempOrdersByBuyer(buyerID);
			
			
			
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<CartDTO[]> response = restTemplate.getForEntity(userUri+"/buyer/cart/getallproducts/" + buyerID, CartDTO[].class);
			CartDTO[] cartlist = response.getBody();
			List<CartDTO> cartList = new ArrayList<CartDTO>();
			for(int i=0;i<cartlist.length;i++)
			{
				cartList.add(cartlist[i]);
			}
			
			for(CartDTO cartDTO: cartList)
			{
				ProductDTO prod = new RestTemplate().getForObject(productUri+"/products/productbyid/" +cartDTO.getProductID(),ProductDTO.class) ; //getByProductID/{productID}
				productList.add(prod);
			}
			logger.info(String.valueOf(order.getOrderID()));
			OrderPlacedDTO orderPlaced = orderService.placeOrder1(productList,cartList,order);
			
			cartList.forEach(item->{
				String result1 = new RestTemplate().getForObject(productUri+"/products/updateStock/{productId}/{quantity}", String.class, item.getProductID(), item.getQuantity()) ;
				logger.info(buyerID +" "+item.getProductID() + result1);
				new RestTemplate().delete(userUri+"/buyer/cart/remove/{buyerID}/{productID}", buyerID, item.getProductID());
			});			
	
			new RestTemplate().put(userUri+"/buyer/addrewardpoints/"+"{buyerID}"+"/"+"{rewardPoints}" ,null, buyerID, orderPlaced.getRewardPoints());

			return new ResponseEntity<>(orderPlaced.getOrderID(),HttpStatus.ACCEPTED);

		}
		catch(Exception e)
		{
			String newMsg = "There was some error";
			if(e.getMessage().equals("404 null"))
			{
				newMsg = "Error while placing the order";
			}
			return new ResponseEntity<>(newMsg,HttpStatus.UNAUTHORIZED);
		}		
		
	}
	
	
	//return all temp orders
	@GetMapping(value = "/orderMS/viewAllTempOrders")
	public ResponseEntity<List<OrderDTO>> viewAllTempOrder(){		
		try {
			List<OrderDTO> allTempOrders = orderService.viewAllTempOrders();
			return new ResponseEntity<>(allTempOrders,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}		
	}
	

}
