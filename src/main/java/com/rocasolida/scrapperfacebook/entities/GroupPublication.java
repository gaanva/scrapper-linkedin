package com.rocasolida.scrapperfacebook.entities;


public class GroupPublication extends Publication{
	boolean salePost;
	long value;
	String ubication;

	public boolean isSalePost() {
		return salePost;
	}

	public void setSalePost(boolean salePost) {
		this.salePost = salePost;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public String getUbication() {
		return ubication;
	}

	public void setUbication(String ubication) {
		this.ubication = ubication;
	}
	
	
}
