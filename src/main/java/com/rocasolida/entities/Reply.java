package com.rocasolida.entities;

import lombok.Data;

public @Data class Reply extends Comment{
	private String userId;
	private String mensaje;
	private String uTime;
}
