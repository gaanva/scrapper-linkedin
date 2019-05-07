package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import lombok.Data;

@Data
public class Page {
	private String name;
	private List<Publication> publications;
	private Long likes;
	private Long followers; //fans
	private long lastUpdated_utime; //last updated utime.
}
