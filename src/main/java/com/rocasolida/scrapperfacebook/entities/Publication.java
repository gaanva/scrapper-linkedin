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
	/** TODO
	 * agregar: like, love, haha, wow, sad, angry 
	 */
	
	/**
	 * new.add_xpath('likes',"//a[contains(@href,'reaction_type=1')]/span/text()")
        new.add_xpath('ahah',"//a[contains(@href,'reaction_type=4')]/span/text()")
        new.add_xpath('love',"//a[contains(@href,'reaction_type=2')]/span/text()")
        new.add_xpath('wow',"//a[contains(@href,'reaction_type=3')]/span/text()")
        new.add_xpath('sigh',"//a[contains(@href,'reaction_type=7')]/span/text()")
        new.add_xpath('grrr',"//a[contains(@href,'reaction_type=8')]/span/text()")     
	 */
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
	 * cant_reactions
	 * negative_reactions
	 * sponsored? => ver como lo extraemos...
	 */

}
