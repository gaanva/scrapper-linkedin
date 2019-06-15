package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import lombok.Data;

@Data
public class Group {
	private String name;
	private String description;
	/*
	private int cantMembers;
	private int cantPubsPorDia;
	private int cantPubsUltimosDias;
	private Long dateCreationUTime;
	*/
	private long lastUpdated_utime; 
	//Si es privado o abierto
	private boolean openGroup;
	private List<Publication> publications;
}
