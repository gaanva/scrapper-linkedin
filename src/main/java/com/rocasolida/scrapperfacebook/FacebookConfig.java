package com.rocasolida.scrapperfacebook;

public final class FacebookConfig {
	public static String URL = "https://www.facebook.com/";
	public static String URL_POST = "/posts/";
	public static String URL_GROUP = "groups/";
	//Par la comunity page info
	//public static String URL_PAGE_COMM = "/community/";
	//sección de comunidad
	public static String PAGE_COMMUNITY_INFO ="//div[@class='_4-u2 _6590 _3xaf _4-u8']/descendant::div[@class='_2pi9 _2pi2']";
	public static String PAGE_COMMUNITY_LIKES = "./div/div[@class='_4bl7']/img[contains(@src,'AT9YNs6Rbpt.png')]/parent::div/following-sibling::div";
	public static String PAGE_COMMUNITY_FOLLOWERS = "./div/div[@class='_4bl7']/img[contains(@src,'PL1sMLehMAU.png')]/parent::div/following-sibling::div";
	public static String PAGE_COMMUNITY_VISITS = "./div/div[@class='_4bl7']/img[contains(@src,'DiD8WSePDTj.png')]/parent::div/following-sibling::div";
	
	//para page information
	public static String URL_INFO_PAGE ="/about/?ref=page_internal";
	
	public static String URL_INFO_PAGE_SECTIONS = "//div[@class='_4-u2 _3xaf _3-95 _4-u8']";
	public static String URL_INFO_PAGE_TITLES = "./descendant::div[@class='_50f7']";
	public static String URL_INFO_PAGE_CONTENT= "./descendant::div[@class='_4bl9']";
	
	//Para page 
	//separar titulo  y contenido:
	
	//para presonas...
	public static String URL_ABOUT_INFO_BASICA = "/about?section=contact-info";
	public static String URL_ABOUT_INFO_BASICA_1 = "&sk=about&section=contact-info";
	public static String URL_ABOUT_INFO_EDUCACION = "/about?section=education";
	public static String URL_ABOUT_INFO_EDUCACION_1 = "&sk=about&section=education";
	//Education and studies se pueden sacar de la overvie tmb	
	public static String URL_ABOUT_INFO_OVERVIEW = "/about?section=overview";
	public static String URL_ABOUT_INFO_OVERVIEW_1 = "&sk=about&section=overview";
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
	public static String XP_POST_TOTALREACTIONS = "./descendant::a[contains(@data-testid,'UFI2ReactionsCount/root')]";
	//Loged in:
	public static String XP_POST_TOTALREACTIONS_LIVEVIDEOS = "./descendant::div[@class='UFIRow UFILikeSentence _4204']";
	//NL=Not Loggedin
	public static String XP_POST_TOTALREACTIONS_LIVEVIDEOS_NL = "./descendant::div[@class='UFILikeSentenceText']";
	
	//logged In
	public static String XP_POST_TOTALSHARED_LIVEVIDEOS = "./descendant::div[@class='UFIRow UFIShareRow']/a";
	//NL=not logged in
	public static String XP_POST_TOTALSHARED_LIVEVIDEOS_NL = "./descendant::a[contains(@class,'UFIShareLink')]";
	public static String XP_POST_TOTALCOMMENTS_LIVEVIDEOS_NL = "./descendant::a[@class='UFIPagerLink']";
	public static String XP_POST_TOTALCOMMENTS = "./descendant::a[contains(@data-testid,'UFI2CommentsCount/root')]";
	
	public static String XP_POST_TOTALSHARES = "./descendant::a[contains(@data-testid,'UFI2SharesCount/root')]";
	public static String XP_POST_INTERACTIONS_SENTENCE = "//a[contains(@href, 'comment_tracking')]";
	
	public static String XP_POST_TOTALREPRODUCTIONS = "./descendant::span[contains(@data-testid,'UFI2ViewCount/root')]";//div[@class='_1t6k']/descendant::span[@class='fcg']
	public static String XP_POST_LIVEVIDEO_TOTALREPRODUCTIONS = "./descendant::div[@class='_1t6k']/span";
	public static String XP_POST_LOVES = "./descendant::span[contains(@data-testid,'_LOVE')]/a"; //getAttribute('aria-label')
	public static String XP_POST_LIKES = "./descendant::span[contains(@data-testid,'_LIKE')]/a";
	public static String XP_POST_HAHA = "./descendant::span[contains(@data-testid,'_HAHA')]/a";
	public static String XP_POST_WOW = "./descendant::span[contains(@data-testid,'_WOW')]/a";
	public static String XP_POST_SORRY = "./descendant::span[contains(@data-testid,'_SORRY')]/a";
	public static String XP_POST_ANGRY = "./descendant::span[contains(@data-testid,'_ANGER')]/a";
	
	/**
	 * DATOS DEL POST
	 */
	//busco si el post tiene un 'livevideo'
	public static String XP_PUBLICATION_LIVEVIDEO = "./descendant::div[@class='_6a _6b']/following-sibling::div/h5/span/span/span";
	//filtro el tipo de video youtube:
	public static String XP_PUBLICATION_POST_YOUTUBE = "./descendant::a[contains(@*, 'youtu')]";
	//busco si el post tiene un 'link'
	public static String XP_PUBLICATION_LINK = "./descendant::*[contains(@href, 'l.php')]";
	//devuelve el link del contenido compartido: post, photo, albun
	public static String XP_PUBLICATION_CONTENTSHARED = "./descendant::div[@class='_6a _6b']/following-sibling::div/h5/span/span/span/following-sibling::a";
	public static String XP_PUBLICATION_PINEADA = "./descendant::div[@class='_449j']";
	/**
	 * La búsqueda para grupos la tengo que hacer en dos secciones...
	 */
	public static String XP_GROUP_PUBLICATIONS_LASTNEWS_CONTAINER = "//div[@role='feed']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']" + XP_GROUP_PUBLICATIONS_CONTAINER;
	public static String XP_GROUP_PUBLICATIONS_OLD_CONTAINER = "//div[@class='_5pcb']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']" + XP_GROUP_PUBLICATIONS_CONTAINER;
// or @class='fcg'/ancestor::div[@class='_1t6k']
	public static String XPATH_PUBLICATIONS_CONTAINER = "//div[contains(@class,'userContentWrapper')]";
	public static String XPATH_PUBLICATIONS_TYPE_PHOTO_CONTAINER = "//div[contains(@class,'uiScrollableAreaBody')]";

	public static String XPATH_PUBLICATION_ID = ".//a[contains(@ajaxify,'ft_id')]";// getAttribute("ajaxify")
	public static String XPATH_PUBLICATION_ID_1 = ".//span[contains(@class,'fsm fwn fcg')]//a";
	//Levanta los links de las publicaciones que estén listadas en pantalla.
	public static String XPATH_PUBLICATION_LINK = "//div[contains(@class,'f_1jzqrr12pf j_1jzqrqwrre')]//span[contains(@class,'fsm fwn fcg')]//a";

	public static String XPATH_PUBLICATION_OWNER = ".//span[contains(@class,'fwn fcg')]//span[contains(@class,'fwb')]"; // getAttribute("aria-label")
	public static String XPATH_PUBLICATION_PHOTO_OWNER = ".//div[contains(@class,'fbPhotoContributorName')]//a"; // getAttribute("aria-label")

	public static String XPATH_PUBLICATION_HEADER_CONTAINER = "div[@class='_6a _5u5j _6b']";
	public static String XPATH_PUBLICATION_TIMESTAMP = ".//abbr[contains(@class,'livetimestamp')]"; // getAttribute("data-utime")
	public static String XPATH_PUBLICATION_TIMESTAMP_1 = ".//abbr//span[contains(@class,'timestamp')]//parent::abbr"; // getAttribute("data-utime")
	public static String XPATH_PUBLICATION_TIMESTAMP_2 = ".//div[contains(@class,'f_1jzqrr12pf j_1jzqrqwrre')]//span[contains(@class,'fsm fwn fcg')]//abbr//span[contains(@class,'timestamp')]//parent::abbr";
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
	//Agregando para que deje afuera de la condicion a los pineados...
	public static String XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED_PART1 = "//div[contains(@class,'userContentWrapper') and not (descendant::div[@class='_449j'])]" + "//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<";
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
	public static String XP_SPINNERLOAD_COMMENTS_1 ="//span[@class='_4sxg img _55ym _55yn _55yo']";
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
	public static String XPATH_PUBLICATION_TITLE_VER_MAS = "./descendant::div[contains(@class,'_5pbx userContent')]/descendant::a[contains(@class,'see_more_link')]";

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
	//--->
	public static String XPATH_COMMENTS_CONTAINER_NL = ".//a[contains(@href,'comment_tracking')]";
	
	//// div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]
	// public static String XPATH_COMMENT_ROOT_DIV = "//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]"; // Esto agrupa el Comentario. Es el RAIZ del comentario

	//Este contiene los comentarios que se abren en el overlay...
	public static String XPATH_COMMENTS_CONTAINER_1 = "//ul[@class='_77bp']";
	//// div[contains(@class,'UFIContainer')]//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]//div[@class='UFICommentContent']
	public static String XPATH_COMMENT_ROOT_DIV = ".//div[@class='UFIList']/node()[last()]/node()/node()[starts-with(@id,'comment_js')]";
	//// div[contains(@class,'userContentWrapper') and .//span[contains(@class,'fsm fwn fcg')]//a[contains(@href,'1955574351421677')]]//div[@class='UFIList']/node()[last()]/node()/node()

	public static String XPATH_REPLY_ROOT_DIV = ".//div[@class=' UFIReplyList']";
	public static String XPATH_COMMENTS_AND_REPLIES_DIVS = "//*[starts-with(@class,'UFIRow UFIComment') or contains(@class,'UFIReplyList')]";

	// public static String XPATH_COMMENTS_BLOCK = ".//div[contains(@class,'UFICommentContentBlock')]";
	// public static String XPATH_COMMENTS = ".//span[contains(@class,' UFICommentActorAndBody') and not(ancestor::div[@class=' UFIReplyList'])]";
	public static String XPATH_COMMENTS = "//div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[contains(@class,' UFIReplyList')]) and not(contains(@style,'hidden'))]";
	//COmments cuando se abre en overlay
	public static String XPATH_COMMENTS_1 = "//div[contains(@class,' _6qw3') and not(contains(@style,'hidden'))]";
	public static String XPATH_COMMENTS_AND_RESPONSES = ".//div[contains(@class,'UFICommentContentBlock')]";
	public static String XPATH_RESPONSES_LINK = ".//span[contains(@class,'UFIReplySocialSentenceLinkText')]";
	public static String XPATH_VIEW_ALL_PUB_COMMENTS_LINK ="//a[@class='_2xui']";
	// span[contains(@class,'UFIReplySocialSentenceLinkText')]
	/// following-sibling::div[@class=' UFIReplyList'][1]
	// public static String XPATH_COMMENTS = ".//div//*"; //-->Toma como base el
	// CONTAINER.

	public static String XPATH_USER_ID_COMMENT = ".//span[contains(@class,' UFICommentActorName')]"; // getAttribute("data-hovercard")
	public static String XPATH_USER_ID_COMMENT2 = ".//*[@class=' UFICommentActorName']"; // getAttribute("data-hovercard")
	public static String XPATH_USER_URL_PROFILE = "//a[contains(@class,' UFICommentActorName')]";
	public static String XPATH_USER_URL_PROFILE_1 = "//a[contains(@class,'_6qw4')]";
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
	
	/**
	 * Users scarp information
	 */
	//Profile Pic
	public static String USER_PIC = "//a[contains(@class,'profilePicThumb')]/img";
	//Info basica
	public static String USER_GENDER = "//li[@class='_3pw9 _2pi4 _2ge8 _3ms8']/div/div[2]";
	public static String USER_FECHANAC = "//li[@class='_3pw9 _2pi4 _2ge8 _4vs2']/div/div[2]";
	public static String USER_UBICACION = "//li[@class='_3pw9 _2pi4 _2ge8 _3f8a']/div/div[2]";
	//Info de la educacion
	public static String USER_ESTUDIO_CONTAINER = "//div[@class='_4qm1']/ul/li/div/div/div/div/div[2]";
	//public static String USER_ESTUDIO_LUGAR = "//div[@class='_4qm1']/ul/li/div/div/div/div/div[2]/div[1]";
	public static String USER_ESTUDIO_LUGAR = "/div[1]/a";
	//div[@class='_4qm1' and @id='u_0_23']
	//public static String USER_ESTUDIO_DESC = "//div[@class='_4qm1']/ul/li/div/div/div/div/div[2]/div[2]/div";
	public static String USER_ESTUDIO_DESC = "/div";
	
	//Ubicacion
	public static String USER_PLACES = "//div[@data-overviewsection='places']/div//a";
	public static String USER_EDUCATION = "//div[@data-overviewsection='education']";
	
	
	public static String GROUP_NAME = "//a[contains(@href,'group_header')]"; //getText()
	public static String GROUP_PRIVATE = "//i[@class='_3ph1 img sp_r88stLlPn45 sx_73e64b']";
	
	public FacebookConfig() {

	}
}
