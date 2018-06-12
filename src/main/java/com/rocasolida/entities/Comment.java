package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

@Data
public class Comment {

	private String id;
	private String userId;
	private String mensaje;
	private String uTime;
	private String likes;
	private List<Reply> replies;
}