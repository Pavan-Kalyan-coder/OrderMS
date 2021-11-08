package com.oms.orderMS.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(ProductsOrderedPK.class)
@Table(name = "products_ordered")
public class ProductsOrdered {
	
	@Id
	@Column(name = "buyer_id", nullable = false)
	String buyerID;
	@Id
	@Column(name = "product_id", nullable = false)
	String productID;
	@Column(name = "seller_id", nullable = false)
	String SellerID;
	@Column(nullable = false)
	Integer quantity;
	public String getBuyerID() {
		return buyerID;
	}
	public void setBuyerID(String buyerID) {
		this.buyerID = buyerID;
	}
	public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	public String getSellerID() {
		return SellerID;
	}
	public void setSellerID(String sellerID) {
		SellerID = sellerID;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
}
