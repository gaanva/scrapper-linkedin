package com.rocasolida.entities;

import java.util.Date;
import java.util.List;

import lombok.Data;

public @Data class Publication {
	private String Id;
	private String owner;
	private Long timeStamp;
	private String dateTime;
	private String titulo;
	private Integer cantShare;
	private Integer cantReproducciones;
	private List<Comment> comments;

}
