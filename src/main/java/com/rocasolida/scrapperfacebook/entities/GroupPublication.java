package com.rocasolida.scrapperfacebook.entities;


public class GroupPublication extends Publication{
	boolean salePost;

	public boolean isSalePost() {
		return salePost;
	}

	public void setSalePost(boolean salePost) {
		this.salePost = salePost;
	}
}
