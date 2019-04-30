package com.rocasolida.scrapperfacebook.scrap.util;

import lombok.Data;

@Data
public class PublicationScrapper {
	private String xpath_publication_container;
	private String xpath_all_comments;
	private String xpath_ver_mas_comments;
	private String xpath_mostrar_comments;
	private String xpath_publication_spinner_loader;
	private String xpath_publication_loaded; //que objeto carga cuando puedo interactuar con la pagina?
}
