package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

@Data
public class Comment {

	private String id;
	private String userId;
	private String userName;
	private String mensaje;
	private Long uTime;
	private Integer cantLikes;
	private List<Reply> replies;
}