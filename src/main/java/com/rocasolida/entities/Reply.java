package com.rocasolida.entities;

import lombok.Data;

@Data
public class Reply extends Comment {

	private String userId;
	private String mensaje;
	private String uTime;
}
