package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

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
	/** TODO
	 * agregar: like, love, haha, wow, sad, angry 
	 */
	private Integer cantLikes;
	private Integer cantComments;
	private List<Comment> comments;
	/** TODO
	 * agregar:
	 * Post_type: link, photo, video, live video.
	 * cant_reactions
	 * negative_reactions
	 * sponsored? => ver como lo extraemos...
	 */

}
