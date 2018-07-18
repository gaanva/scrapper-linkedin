package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import lombok.Data;

@Data
public class Publication {

	private String id;
	private String url;
	private String owner;
	private Long uTime;
	private String titulo;
	private Integer cantShare;
	private Integer cantReproducciones;
	private Integer cantLikes;
	private Integer cantComments;
	private List<Comment> comments;

}
