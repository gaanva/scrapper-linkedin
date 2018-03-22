package com.rocasolida.entities;

import lombok.Data;

public @Data class Credential {
	//para el acceso
	private String user;
	private String pass;
	//a fututo, para diferenciar el contenido de la persona.
	private Long user_id;
	private String user_public_name;
	
	
	public Credential(String user, String pass, Long user_id, String user_public_name) {
		super();
		this.user = user;
		this.pass = pass;
		this.user_id = user_id;
		this.user_public_name = user_public_name;
	}
	
	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getPass() {
		return pass;
	}


	public void setPass(String pass) {
		this.pass = pass;
	}


	public Long getUser_id() {
		return user_id;
	}


	public void setUser_id(Long user_id) {
		this.user_id = user_id;
	}


	public String getUser_public_name() {
		return user_public_name;
	}


	public void setUser_public_name(String user_public_name) {
		this.user_public_name = user_public_name;
	}


	
}
