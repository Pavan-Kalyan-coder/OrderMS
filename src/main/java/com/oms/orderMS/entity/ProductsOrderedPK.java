package com.oms.orderMS.entity;

import java.io.Serializable;

public class ProductsOrderedPK implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String buyerID;
    private String productID;
    public ProductsOrderedPK() {}
    public ProductsOrderedPK(String buyerID, String productID) {
		this.buyerID = buyerID;
		this.productID = productID;
	}
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
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
