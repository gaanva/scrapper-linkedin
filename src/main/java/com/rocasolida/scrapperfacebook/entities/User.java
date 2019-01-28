package com.rocasolida.scrapperfacebook.entities;

import lombok.Data;

@Data
public class User {
	private String genero;
	private int edad;
	private String fechaNac;
	private String nivelSocioEducativo;
	private String urlFotoPerfil;
	//la url que extraigo del comment.
	private String urlPerfil;
}
