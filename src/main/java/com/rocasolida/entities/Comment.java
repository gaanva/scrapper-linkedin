package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

public @Data class Comment {
	private String userId;
	private String mensaje;
	private String uTime;
	private List<Reply> replies;
}