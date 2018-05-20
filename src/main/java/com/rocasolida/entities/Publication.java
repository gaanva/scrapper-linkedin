package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

@Data
public class Publication {

	private String id;
	private String owner;
	private Long timeStamp;
	private String dateTime;
	private String titulo;
	private Integer cantShare;
	private Integer cantReproducciones;
	private Integer cantLikes;
	private List<Comment> comments;

}
