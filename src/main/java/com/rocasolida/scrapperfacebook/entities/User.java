package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import lombok.Data;

@Data
public class User {
	private String urlPerfil;
	private String urlFotoPerfil;
	
	private String genero;
	private int edad;
	private String fechaNac;
	private String ubicacion;
		
	private List<String> estudios;
	private String empleo;
	@Override
	public String toString() {
		return "User [urlPerfil=" + urlPerfil + ", urlFotoPerfil=" + urlFotoPerfil + ", genero=" + genero + ", edad="
				+ edad + ", fechaNac=" + fechaNac + ", ubicacion=" + ubicacion + ", estudios=" + estudios + ", empleo=" + empleo + "]";
	}
	
	
}
