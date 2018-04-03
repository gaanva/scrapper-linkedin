package com.rocasolida.entities;

import java.util.List;

import lombok.Data;

public @Data class Page {
	List<Publication> publications;
	int likes;
	int followers;
}
