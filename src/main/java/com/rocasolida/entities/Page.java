package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

@Data
public class Page {

	private String name;
	private List<Publication> publications;
	private Long likes;
	private Long followers;
}
