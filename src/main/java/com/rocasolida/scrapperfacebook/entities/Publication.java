package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import com.rocasolida.scrapperfacebook.scrap.util.FacebookPostType;

import lombok.Data;

@Data
public class Publication {

	private String id;
	private String url;
	private String owner; //Si es distinto al perfil o pagina => "Posts" sino "User Post" 
	private Long uTime; //con esto ya se puede calcular hora y dia del post.
	private String titulo;
	private Integer cantShare;
	private Integer cantReproducciones;
	private Integer cantLikes;
	private Integer cantLoves;
	private Integer cantHahas;
	private Integer cantWows;
	private Integer cantSads;
	private Integer cantAngries;
	private Integer cantReactions;
	private Integer cantComments;
	
	private List<Comment> comments;
	
	/*
	 * cacmpos nuevos agregados para los reportes
	 */
	private FacebookPostType type;
	
	/** TODO
	 * agregar:
	 * 
	 * negative_reactions
	 * sponsored? => ver como lo extraemos...
	 */

}
