package com.rocasolida.scrapperfacebook;

public final class FacebookConfig {
	public static String URL = "https://www.facebook.com/";
	public static String URL_POST = "/posts/";
	public static String URL_GROUP = "groups/";
	
	
	//// title[@lang='en']
	public static Integer CANT_PUBLICATIONS_TO_BE_LOAD = 10;

	/** FORM_LOGIN **/
	public static String XPATH_FORM_LOGIN = "//form[contains(@id,'login_form')]";
	public static String XPATH_INPUT_MAIL_LOGIN = ".//input[contains(@id,'email')]";
	public static String XPATH_INPUT_PASS_LOGIN = ".//input[contains(@id,'pass')]";
	public static String XPATH_BUTTON_LOGIN = ".//label[contains(@id,'loginbutton')]//input";

	/** CARGAR PUBLICACIONES **/
	public static String XPATH_TIMELINE_MAINCOLUMN = "//div[@id='pagelet_timeline_main_column']";
	public static String XPATH_SHOW_ALL_PHOTOS_LINK = "//div[@id='page_photos']//div[@class='_4z-w']//a";
	public static String XPATH_SHOW_ALL_VIDEOS_LINK = "//div[@id='videos']//div[@class='_4z-w']//a";
	public static String XPATH_SHOW_ALL_PUB_DETACADAS_LINK = "//div[@class='_4-u2 _3xaf _3-95 _4-u8']//div[@class='_4z-w']//a";

	public static String XPATH_START_PUBLICATIONS_TITLE = "//div[@class='_3-95']//span[text()='Publicaciones']"; // Busqueda de control para saber si ya están cargadas las publicaciones
	public static String XPATH_PPAL_BUTTON_SHOW_MORE = "//a[contains(@class,'uiMorePagerPrimary')]";

	public static String XPATH_CLOSE_BUTTON = "//a[@class='_xlt _418x']";

	public static String XP_MORE_PUBS_GROUP = "//div[@id='pagelet_group_pager']";
	
	/** DATOS DE LA PUBLICACIÓN **/
	public static String XP_GROUP_PUBLICATIONS_CONTAINER = "//div[contains(@class,'userContentWrapper') and not(contains(@style,'hidden'))]";
	public static String XPATH_PUBLICATIONS_CONTAINER = "//div[contains(@class,'userContentWrapper')]";
	public static String XPATH_PUBLICATIONS_TYPE_PHOTO_CONTAINER = "//div[contains(@class,'uiScrollableAreaBody')]";

	public static String XPATH_PUBLICATION_ID = ".//a[contains(@ajaxify,'ft_id')]";// getAttribute("ajaxify")
	public static String XPATH_PUBLICATION_ID_1 = ".//span[contains(@class,'fsm fwn fcg')]//a";

	public static String XPATH_PUBLICATION_OWNER = ".//span[contains(@class,'fwn fcg')]//span[contains(@class,'fwb')]"; // getAttribute("aria-label")
	public static String XPATH_PUBLICATION_PHOTO_OWNER = ".//div[contains(@class,'fbPhotoContributorName')]//a"; // getAttribute("aria-label")

	public static String XPATH_PUBLICATION_TIMESTAMP = ".//abbr[contains(@class,'livetimestamp')]"; // getAttribute("data-utime")
	public static String XPATH_PUBLICATION_TIMESTAMP_1 = ".//abbr//span[contains(@class,'timestamp')]//parent::abbr"; // getAttribute("data-utime")
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 = XPATH_PUBLICATIONS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::a[contains(@href,'";
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 = "')]//descendant::abbr[@data-utime>=";
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3 = " and @data-utime<=";
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART4 = "]";
	//
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION(String facebookPage, Long utimeINI, Long utimeFIN) {
	// return XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 + facebookPage + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeINI + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3 + utimeFIN + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART4;
	// }
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 = XPATH_PUBLICATIONS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime>=";
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 = " and @data-utime<=";
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3 = "]";

	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION(String facebookPage, Long utimeINI, Long utimeFIN) {
		return XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 + utimeINI + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeFIN + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3;
	}

	/**
	 * Controlar que no tome el timestamp de un comentario!!!! Controlar que la cantidad de publicaciones, no sean de solo las destacadas.
	 */
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 = XPATH_PUBLICATIONS_CONTAINER+"//descendant::div[contains(@id,'subtitle')]//descendant::a[contains(@href,'";
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 = "')]//descendant::abbr[@data-utime<";
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART3 = "]";
	//
	// public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(String facebookPage, Long utimeINI) {
	// return XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 + facebookPage + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 + String.valueOf(utimeINI) + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART3;
	// }
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 = XPATH_PUBLICATIONS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<";
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 = "]";

	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(String facebookPage, Long utimeINI) {
		return XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 + String.valueOf(utimeINI) + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2;
	}

	// DATE_TIME: PONE HUSO HORARIO ARGENTINA (GMT+4). Diff de 4hs.
	public static String XPATH_PUBLICATION_DATE_TIME = ".//abbr[contains(@class,'livetimestamp')]"; // getAttribute("title")
	public static String XPATH_PUBLICATION_TITLE = ".//div[contains(@class,'_5pbx userContent')]";
	public static String XPATH_PUBLICATION_TITLE_VER_MAS = ".//div[contains(@class,'_5pbx userContent')]//a[contains(@class,'see_more_link')]";

	public static String XPATH_PUBLICATION_CANT_REPRO = ".//div[contains(@class,'_1t6k')]";
	public static String XPATH_PUBLICATION_CANT_SHARE = ".//a[contains(@class,'UFIShareLink')]";
	public static String XPATH_PUBLICATION_CANT_LIKE = ".//div[contains(@class,'UFILikeSentence')]//a[contains(@class,'_3emk _401_')]";

	public static String XPATH_PUBLICATION_VER_MAS_MSJS = ".//div[contains(@class,'UFILastCommentComponent')]//a[contains(@class,'UFIPagerLink')]";

	public static String XPATH_PUBLICATION_VER_RESPUESTAS = ".//a[contains(@class,'UFICommentLink')]";
	/** DATOS DE LOS MENSAJES **/

	/*
	 * COMENTARIOS
	 */
	public static String XPATH_COMMENTS_CONTAINER = ".//div[contains(@class,'UFIContainer')]"; // Esto agrupa a TODOS los comentarios/Replies
	public static String XPATH_COMMENTS_CONTAINER_NL = ".//a[contains(@href,'comment_tracking')]";
	//// div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]
	// public static String XPATH_COMMENT_ROOT_DIV = "//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]"; // Esto agrupa el Comentario. Es el RAIZ del comentario

	//// div[contains(@class,'UFIContainer')]//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]//div[@class='UFICommentContent']
	public static String XPATH_COMMENT_ROOT_DIV = ".//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]";
	//// div[contains(@class,'userContentWrapper') and .//span[contains(@class,'fsm fwn fcg')]//a[contains(@href,'1955574351421677')]]//div[@class='UFIList']/node()[last()]/node()/node()

	public static String XPATH_REPLY_ROOT_DIV = ".//div[@class=' UFIReplyList']";
	public static String XPATH_COMMENTS_AND_REPLIES_DIVS = "//*[starts-with(@class,'UFIRow UFIComment') or contains(@class,'UFIReplyList')]";

	// public static String XPATH_COMMENTS_BLOCK = ".//div[contains(@class,'UFICommentContentBlock')]";
	// public static String XPATH_COMMENTS = ".//span[contains(@class,' UFICommentActorAndBody') and not(ancestor::div[@class=' UFIReplyList'])]";
	public static String XPATH_COMMENTS = ".//div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList'])]";
	public static String XPATH_COMMENTS_AND_RESPONSES = ".//div[contains(@class,'UFICommentContentBlock')]";
	public static String XPATH_RESPONSES_LINK = ".//span[contains(@class,'UFIReplySocialSentenceLinkText')]";
	// span[contains(@class,'UFIReplySocialSentenceLinkText')]
	/// following-sibling::div[@class=' UFIReplyList'][1]
	// public static String XPATH_COMMENTS = ".//div//*"; //-->Toma como base el
	// CONTAINER.
	public static String XPATH_USER_ID_COMMENT = ".//a[contains(@class,' UFICommentActorName')]"; // getAttribute("data-hovercard")
	public static String XPATH_USER_ID_COMMENT2 = ".//*[@class=' UFICommentActorName']"; // getAttribute("data-hovercard")

	public static String XPATH_USER_COMMENT = ".//span[contains(@class,'UFICommentBody')]/node()";
	public static String XPATH_COMMENT_UTIME = ".//abbr[contains(@class,'UFISutroCommentTimestamp livetimestamp')]";
	public static String XPATH_COMMENT_ID = ".//a[contains(@class,'uiLinkSubtle')]";
	public static String XPATH_USER_COMMENT_ACTIONS = ".//div[cont ains(@class,'UFICommentActions')].//abbr";//// getAttribute("data-utime")

	public static String XPATH_COMMENT_REPLY_LINKS = "//span[@class='UFIReplySocialSentenceLinkText UFIReplySocialSentenceVerified']";

	// public static String XPATH_COMMENTS_TIMESTAMP_CONDITION_SATISFIED(Long utimeINI) {
	// return XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 + String.valueOf(utimeINI) + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART3;
	// }
	// div[contains(@class,' UFIReplyList')]

	public FacebookConfig() {

	}
}
