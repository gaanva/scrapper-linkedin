package com.rocasolida.scrapperfacebook;

public final class FacebookConfig {
	public static String URL = "https://www.facebook.com/";
	public static String URL_POST = "/posts/";
	public static String URL_GROUP = "groups/";
	public static String URL_LIKES = "/likes_all";
	public static String XP_HAS_LIKES_CONTENT = "//div[contains(@class,'_5h60 _30f')]";
	public static String XP_USER_LIKES = "//li[contains(@class,'_5rz _5k3a _5rz3 _153f') and not(contains(@style,'hidden'))]";
	public static String XP_LIKES_LOADING = "//div[contains(@class,'_5h60 _30f')]/img[contains(@class, '_359')]";

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
	public static String XP_GROUP_PUBLICATIONS_CONTAINER = "//div[contains(@class,'userContentWrapper')]";

	/**
	 * La búsqueda para grupos la tengo que hacer en dos secciones...
	 */
	public static String XP_GROUP_PUBLICATIONS_LASTNEWS_CONTAINER = "//div[@role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']" + XP_GROUP_PUBLICATIONS_CONTAINER;
	public static String XP_GROUP_PUBLICATIONS_OLD_CONTAINER = "//div[@class='_5pcb']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']" + XP_GROUP_PUBLICATIONS_CONTAINER;

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
	
	
	
	public static String XP_GROUPMAINPUBLICATIONS_ALL = "//div[@class='_5pcb' or @role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']//div[contains(@class,'userContentWrapper')]";
	
	/*
	 * //div[@class='_5pcb' or @role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']//div[contains(@class,'userContentWrapper')]//abbr[(contains(@class,'livetimestamp')) and (@data-utime>=0 and @data-utime<99999999999)]
	 */
	public static String XP_GROUPMAINPUBLICATIONS_TIMESTAMP_CONDITION(Long utimeINI, Long utimeFIN) {
		return "//div[@class='_5pcb' or @role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']//div[contains(@class,'userContentWrapper')]//abbr[(contains(@class,'livetimestamp')) and (@data-utime>="+utimeINI+" and @data-utime<"+utimeFIN+")]//ancestor::div[contains(@class,'userContentWrapper')]";
		//return XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 + utimeINI + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeFIN + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3;
	}
	
	public static String XP_GROUPMAINPUBLICATIONS_TIMESTAMP_FROM_CONDITION(Long utimeINI) {
		return "//div[@class='_5pcb' or @role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']//div[contains(@class,'userContentWrapper')]//abbr[(contains(@class,'livetimestamp')) and (@data-utime<"+utimeINI+")]//ancestor::div[contains(@class,'userContentWrapper')]";
		//return XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 + utimeINI + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeFIN + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3;
	}

	/**
	 * Group publications condition
	 */
	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION_PART1 = XP_GROUP_PUBLICATIONS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime>=";
	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION_PART2 = " and @data-utime<=";
	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION_PART3 = "]";

	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION(String facebookGroup, Long utimeINI, Long utimeFIN) {
		return XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART1 + utimeINI + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeFIN + XPATH_PUBLICATION_TIMESTAMP_CONDITION_PART3;
	}

	/**
	 * Group Last news publications condition
	 */
	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART1 = XP_GROUP_PUBLICATIONS_LASTNEWS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime>=";
	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART2 = " and @data-utime<=";
	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART3 = "]";

	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION(String facebookGroup, Long utimeINI, Long utimeFIN) {
		return XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART1 + utimeINI + XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART2 + utimeFIN + XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_PART3;
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

	/**
	 * group Posts
	 */
	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 = XP_GROUP_PUBLICATIONS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<";
	public static String XP_GROUPPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 = "]";

	public static String XP_GROUP_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(String facebookPage, Long utimeINI) {
		return XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 + String.valueOf(utimeINI) + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2;
	}

	/**
	 * group last news publicactions
	 */
	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 = XP_GROUP_PUBLICATIONS_LASTNEWS_CONTAINER + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<";
	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 = "]";

	public static String XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_SATISFIED(String facebookGroup, Long utimeINI) {
		return XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 + String.valueOf(utimeINI) + XP_LASTNEWSPUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2;
	}

	/**
	 * Spinner load More Group Comments
	 */
	public static String XP_SPINNERLOAD_COMMENTS = "//div[contains(@class,'UFICommentsLoadingSpinnerContainer ')]";

	/**
	 * open Group Publication
	 */
	public static String XP_PUBLICATION_OVERLAY = "//div[@class='_3ixn']/..";
	public static String XP_LOGIN_OVERLAY = "//div[@ID='u_0_c']";
	
	public static String XP_PUBLICATION_LIKES = "//span[@class='_4arz']/span";
	public static String XP_PUBLICATION_LIKES_NL = "//div[@class='UFILikeSentenceText']/span";
	public static String XP_PUBLICATION_CANTCOMMENTS = "//div[@class='_36_q']/a";
	public static String XP_PUBLICATION_COMPARTIDOS = "//div[@class='_36_q']/a[contains(@href,'shares')]";
	public static String XP_PUBLICATION_SALEPRICE = "//div[@class='_l56']/div";
	public static String XP_PUBLICATION_TITLE = "//div[@class='_l53']/span[last()]";
	public static String XP_PUBLICATION_LOCATION = "//div[@class='_l56']/div[last()]";
	/****/

	// DATE_TIME: PONE HUSO HORARIO ARGENTINA (GMT+4). Diff de 4hs.
	public static String XPATH_PUBLICATION_DATE_TIME = ".//abbr[contains(@class,'livetimestamp')]"; // getAttribute("title")
	public static String XPATH_PUBLICATION_TITLE = ".//div[contains(@class,'_5pbx userContent')]";
	public static String XPATH_PUBLICATION_TITLE_VER_MAS = ".//div[contains(@class,'_5pbx userContent')]//a[contains(@class,'see_more_link')]";

	public static String XPATH_PUBLICATION_CANT_REPRO = ".//div[contains(@class,'_1t6k')]";

	public static String XPATH_PUBLICATION_CANT_SHARE = ".//a[contains(@class,'UFIShareLink')]";
	public static String XP_GROUPPUBLICATION_CANT_SHARE = ".//div[@class='_3399 _1f6t _4_dr _20h5']/descendant::div[@class='_ipo']/descendant::a[contains(@data-tooltip-uri,'share')]";

	public static String XPATH_PUBLICATION_CANT_LIKE = ".//div[contains(@class,'UFILikeSentence')]//a[contains(@class,'_3emk _401_')]";
	public static String XP_GROUPPUBLICATION_CANT_LIKES = ".//div[@class='UFILikeSentenceText']/span";

	public static String XPATH_PUBLICATION_VER_MAS_MSJS = ".//div[contains(@class,'UFILastCommentComponent')]//a[contains(@class,'UFIPagerLink')]";
	public static String XPATH_PUBLICATION_VER_MAS_MSJS2 = ".//a[contains(@data-testid,'UFI2CommentsPagerRenderer/pager_depth_0')]";
	public static String XP_GROUPPUBLICATION_VER_MAS_MSJS = "//a[contains(@class,'UFIPagerLink')]";

	public static String XPATH_PUBLICATION_VER_RESPUESTAS = ".//a[contains(@class,'UFICommentLink')]";
	/** DATOS DE LOS MENSAJES **/

	/*
	 * COMENTARIOS
	 */
	public static String XPATH_COMMENTS_CONTAINER = ".//div[contains(@class,'UFIContainer')]"; // Esto agrupa a TODOS los comentarios/Replies
	public static String XPATH_COMMENTS_CONTAINER2 = ".//form[contains(@class,'commentable_item')]//div"; // Esto agrupa a TODOS los comentarios/Replies
	public static String XPATH_COMMENTS_CONTAINER3 = ".//div[contains(@data-testid,'UFI2CommentsList/root_depth_0')]"; // Esto agrupa a TODOS los comentarios/Replies
	public static String XPATH_COMMENTS_CONTAINER4 = ".//div[contains(@class,'_3b-9 _j6a')]"; // Esto agrupa a TODOS los comentarios/Replies
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
	public static String XPATH_COMMENTS = "//div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[contains(@class,' UFIReplyList')]) and not(contains(@style,'hidden'))]";
	public static String XPATH_COMMENTS_AND_RESPONSES = ".//div[contains(@class,'UFICommentContentBlock')]";
	public static String XPATH_RESPONSES_LINK = ".//span[contains(@class,'UFIReplySocialSentenceLinkText')]";
	// span[contains(@class,'UFIReplySocialSentenceLinkText')]
	/// following-sibling::div[@class=' UFIReplyList'][1]
	// public static String XPATH_COMMENTS = ".//div//*"; //-->Toma como base el
	// CONTAINER.

	public static String XPATH_USER_ID_COMMENT = ".//span[contains(@class,' UFICommentActorName')]"; // getAttribute("data-hovercard")
	public static String XPATH_USER_ID_COMMENT2 = ".//*[@class=' UFICommentActorName']"; // getAttribute("data-hovercard")
	public static String XPATH_USER_NAME_COMMENT = ".//div[contains(@data-testid,'UFI2Comment/body')]/div/*[1]"; // getAttribute("data-hovercard")

	//public static String XPATH_USER_COMMENT = ".//span[contains(@class,'UFICommentBody')]/node()";
	public static String XPATH_USER_COMMENT = "./descendant::span[contains(@class,'UFICommentBody')]/node()";
	public static String XPATH_USER_COMMENT2 = ".//div[contains(@data-testid,'UFI2Comment/body')]/div/*[2]/span/span";
	public static String XPATH_SEE_MORE_COMMENT_TEXT = ".//div[contains(@data-testid,'UFI2Comment/body')]//a[@class='_5v47 fss']";
	public static String XPATH_COMMENT_UTIME = ".//abbr[contains(@class,'UFISutroCommentTimestamp livetimestamp')]";
	public static String XPATH_COMMENT_UTIME2 = ".//abbr[contains(@class,'livetimestamp')]";
	public static String XPATH_COMMENT_ID = ".//a[contains(@class,'uiLinkSubtle')]";
	public static String XPATH_COMMENT_ID2 = ".//ul[contains(@data-testid,'UFI2CommentActionLinks/root')]//a";
	public static String XPATH_USER_COMMENT_ACTIONS = ".//div[cont ains(@class,'UFICommentActions')].//abbr";//// getAttribute("data-utime")

	public static String XPATH_COMMENT_REPLY_LINKS = "//span[@class='UFIReplySocialSentenceLinkText UFIReplySocialSentenceVerified']";

	// public static String XPATH_COMMENTS_TIMESTAMP_CONDITION_SATISFIED(Long utimeINI) {
	// return XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART2 + String.valueOf(utimeINI) + XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART3;
	// }
	// div[contains(@class,' UFIReplyList')]
	
	/**
	 * group Posts
	 */
	//Condicion para que continue recorriendo comentarios.
	public static String GROUPPUB_COMMENTS_TIMESTAMP_FROM = XPATH_COMMENTS + "//abbr[@data-utime>=";
	public static String GROUPPUB_COMMENTS_TIMESTAMP_CONDITION(Long utimeINI) {
		return GROUPPUB_COMMENTS_TIMESTAMP_FROM + String.valueOf(utimeINI) + "]";
	}
	
	public static String GROUPPUB_COMMENTS_TIMESTAMP_TO = "@data-utime<=";
	public static String GROUPPUB_COMMENTS_TIMESTAMP_CONDITION_FROMTO(Long utimeINI, Long utimeFIN) {
		return GROUPPUB_COMMENTS_TIMESTAMP_FROM + String.valueOf(utimeINI) + " and " +GROUPPUB_COMMENTS_TIMESTAMP_TO + String.valueOf(utimeFIN) +"]//ancestor::div[contains(@class,'UFICommentContentBlock')]";
	}
	
	public FacebookConfig() {

	}
}
