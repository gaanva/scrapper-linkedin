package com.rocasolida.scrap;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.FacebookConfig;
import com.rocasolida.entities.Comment;
import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Publication;
import com.rocasolida.scrap.util.DriverType;

import lombok.Data;

public @Data class FacebookScrap extends Scrap {

	private Long timeStampCorte; // Un mínimo de fecha en la que tiene que correr.

	public FacebookScrap(DriverType driverType) {
		super(driverType);
	}

	public boolean login(Credential access) {
		if (this.navigateTo(FacebookConfig.URL)) {
			if (this.existElement(null, FacebookConfig.XPATH_BUTTON_LOGIN)) {
				WebElement formLogin = this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_FORM_LOGIN));
				formLogin.findElement(By.xpath(FacebookConfig.XPATH_INPUT_MAIL_LOGIN)).sendKeys(access.getUser());
				formLogin.findElement(By.xpath(FacebookConfig.XPATH_INPUT_PASS_LOGIN)).sendKeys(access.getPass());
				formLogin.findElement(By.xpath(FacebookConfig.XPATH_BUTTON_LOGIN)).click();
				if (loggedIn()) {
					super.setAccess(access);
					System.out.println("[SUCCESS]Login Successfull! " + "usr: " + this.getAccess().getUser());
					return true;
				} else {
					System.out.println("[ERROR]Check Login Credentials! " + "usr: " + access.getUser());
					return false;
				}
			}
			System.out.println("[ERROR] No se cargó el botón de Login. Expression: " + FacebookConfig.XPATH_FORM_LOGIN);
			return false;
		} else {
			System.out.println("[ERROR] AL INTENTAR ACCEDER A LA PÁGINA DE LOGIN: " + FacebookConfig.URL);
			return false;
		}
	}

	public String facebookLinkType() {
		// Primero. Determinar si es una PAGINA o un PERFIL.
		// Por lo visto: Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
		// XPATH: //div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a
		// getAttribute('data-tab-key') == 'timeline' (Biografía)
		// getAttribute('data-tab-key') == 'friends'
		// ASUMO QUE SI (Encuentro: INICIO || PUBLICACIONES || COMUNIDAD EN MENU DE LA IZQ) ES UNA PÁGINA.
		if (this.existElement(null, "//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts' or @data-key='tab_community' or @data-key='tab_home']//descendant::a")) {
			return "PAGE";
		}
		// Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
		if (this.existElement(null, "//div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a")) {
			return "PROFILE";
		}

		return "";

	}

	public List<Publication> obtainPublicationsLoggedIn(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {
		List<WebElement> publicationsElements = this.inicializePublicationsToBeLoad(facebookPage, uTIME_INI, uTIME_FIN);
		if (publicationsElements != null) {
			List<Publication> publicationsImpl = new ArrayList<Publication>();
			for (int i = 0; i < publicationsElements.size(); i++) {
				System.out.println("[INFO] EXTRAYENDO DATOS DE LA PUBLICACION NRO#" + i);
				// Extraigo los datos de las publicaciones.
				publicationsImpl.add(this.extractPublicationData(publicationsElements.get(i)));
			}

			for (int i = 0; i < publicationsImpl.size(); i++) {
				// for (int i = 0; i < 1; i++) {
				System.out.println("[INFO] RELOAD GHOST WEBDRIVER...");
				this.refresh();
				System.out.println("[INFO] FIN RELOAD GHOST WEBDRIVER...");
				System.out.println("[INFO] ME DIRIJO A: " + FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST + publicationsImpl.get(i).getId());
				this.getDriver().navigate().to(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST + publicationsImpl.get(i).getId());

				List<WebElement> pubsNew = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
				System.out.println("[INFO] PUBLICATION TITLE: " + publicationsImpl.get(i).getTitulo());
				// if(this.existElement(publicationsElements.get(i),
				// FacebookConfig.XPATH_COMMENTS_CONTAINER)) {

				System.out.println("[INFO] EXTRAYENDO COMENTARIOS DE LA PUBLICACIÓN");
				if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)) {
					try {
						pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();

					} catch (Exception e) {
						System.out.println("[ERROR] Click en comment_tracking not logged in!");
						System.out.println("error desc: ");
						this.saveScreenShot("clickTckMsg");
					}
				}
				if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")) {
					publicationsImpl.get(i).setComments(this.obtainAllPublicationComments(pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")), FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS));
				} else {
					System.out.println("[INFO] la publicacion no tiene actividad.");
				}
			}

			return publicationsImpl;
		} else {
			System.out.println("[INFO] NO SE ENCONTRARON PUBLICACIONES PARA PROCESAR.");
			return null;
		}
	}

	public List<WebElement> inicializePublicationsToBeLoad(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {

		if (this.navigateTo(FacebookConfig.URL + facebookPage)) { // SI NO TIRA ERROR DE CONEXIÓN O DE PAGINA INEXISTENTE...
			System.out.println("[INFO] LINK LOADED. " + FacebookConfig.URL + facebookPage);
			String linkType = this.facebookLinkType(); // POR AHORA CHEQUEA SI ES PAGINA O PERFIL

			switch (linkType) {
			case "PROFILE":
				System.out.println("[INFO] Es un Perfil.");
				return null;
			case "PAGE":
				System.out.println("[INFO] Es una Página.");
				return this.processPagePosts(facebookPage, uTIME_INI, uTIME_FIN);
			default:
				System.out.println("[WARNING] No se reconoce el tipo de página para hacer SCRAP");
				return null;
			}
		}
		return null;
	}

	public List<WebElement> processPagePosts(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {
		try {
			this.getDriver().findElement(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")).click();
			if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
				System.out.println("TOTAL PUBS:" + String.valueOf(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()));
				while (!((this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookPage, uTIME_INI))).size()) > 0)) {
					/**
					 * TODO Buscar una manera de que espere a que termine el scroll para evitar poner el sleep del proceso arbitrariamente.
					 */
					if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
						System.out.println("[INFO] INTENTANDO SCROLL...");
						this.scrollMainPublicationsPage();
					} else {
						System.out.println("[ERROR] Se esperaba encontrar el botón de Show More. Expression: " + FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
						break;
					}
				}
			} else {
				System.out.println("[INFO] LA PAGINA NO TIENE NUNGUNA PUBLICACION PARA MOSTRAR");
				return null;
			}
		} catch (Exception e) {
			System.out.println("[ERROR]");
			e.printStackTrace();
		}

		System.out.println("TOTAL PUBS para FILTRAR:" + String.valueOf(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()));

		int match = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN) + "//ancestor::div[contains(@class,'userContentWrapper')]")).size();
		if (match > 0) {
			System.out.println("[INFO] SE ENCONTRARON " + String.valueOf(match) + " PUBLICACIONES ENTRE LAS FECHAS > a " + uTIME_INI + " y < a " + uTIME_FIN);
			return this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN) + "//ancestor::div[contains(@class,'userContentWrapper')]"));
		} else {
			System.out.println("[ERROR] NO SE ENCONTRARON PUBLICACIONES EN LA FECHA INDICADA.");
			return null;
		}
	}

	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		this.waitForJStoLoad();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("[ERROR] NO SE PUDO HACER LA ESPERA THREAD.SLEEP");
		}
	}

	/**
	 * 
	 * @param pubsLoaded
	 * @param posIni
	 * @return true si al menos 1 publicación es menor a la fecha inicial.
	 */
	public boolean continueScroll(List<WebElement> pubsLoaded, int posIni, Long uTIME_INI) {
		for (int i = posIni; i < pubsLoaded.size(); i++) {
			if (pubsLoaded.get(i).findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED("", uTIME_INI))).size() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Si existe el botón de show more, entonces lo clickea, hasta que se cargaron todos los mensajes para luego obtenerlos con un XPATH query y extraerle los datos. Me servirá para las replies y para los comentarios.
	 */
	public List<Comment> obtainAllPublicationComments(WebElement container, String xPathExpression) {
		List<WebElement> comentarios = new ArrayList<WebElement>();
		List<Comment> comments = new ArrayList<Comment>();

		// Si existe el botón de "Ver Más mensajes"
		if (container.findElements(By.xpath(xPathExpression)).size() > 0) {
			int cantIniComentarios = container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size();
			// int cantIniComentarios = container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size();
			System.out.println("[INFO] CANTIDAD DE COMENTARIOS INICIAL = " + cantIniComentarios);

			WebElement showMoreLink = container.findElement(By.xpath(xPathExpression));
			this.moveTo(showMoreLink);
			showMoreLink.click();
			int cantReintentos = 0;

			while (cantReintentos < 2) {
				try {
					showMoreLink = container.findElement(By.xpath(xPathExpression));
					this.moveTo(showMoreLink);
					showMoreLink.click();
					if (this.ctrlClickHasEffect(container, cantIniComentarios)) {
						cantReintentos = 0;
						cantIniComentarios = (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
						// cantIniComentarios = (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size());
						if (cantIniComentarios > 2200) {
							break;
						}
					} else {
						cantReintentos++;
					}
				} catch (Exception e) {
					cantReintentos++;
				}
			}
		} else {
			System.out.println("NO HAY MÁS MENSAJES PARA CARGAR.");
		}

		System.out.println("[INFO] se cargaron todos los mensajes.");
		// this.saveScreenShot("SCREEN_SCRAWLED_"+String.valueOf(System.currentTimeMillis()));

		comentarios = container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_BLOCK));
		// System.out.println("Comentarios: "+this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size());
		// comentarios = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV));
		// container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size();
		System.out.println("[INFO] PROCESANDO: " + comentarios.size() + " COMENTARIOS.");
		for (int j = 0; j < comentarios.size(); j++) {
			comments.add(this.extractCommentData(comentarios.get(j)));
			System.out.print("--COMENTARIO " + j + "||");
		}
		System.out.println("[INFO] CANTIDAD TOTAL DE COMENTARIOS PROCESADOS: " + comments.size());

		System.out.println("[TIME] Extract COMMENT FIN: " + System.currentTimeMillis());
		return comments;
	}

	/**
	 * Controla que el link_click al "Ver Más" en Comentarios, devuelva algo.
	 * 
	 * @param container:
	 *            es el container de los Cometnarios de la publicación.
	 * @param cantIniComentarios:
	 *            sirve para comparar cantidades de mensajes.
	 * @return
	 * 
	 * 		TODO: Espero arbitrariamente a la carga de los mensajes... debería encontrar la forma de esperar a que el REACTJS y el render del DOM finalicen.
	 */
	public boolean ctrlClickHasEffect(WebElement container, int cantIniComentarios) {
		if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size() > cantIniComentarios)) {
			// if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size() > cantIniComentarios)) {
			try {
				// System.out.println("[INFO]Esperando carga de
				// comentarios..."+System.currentTimeMillis());
				Thread.sleep(800);
				// System.out.println("[INFO]FIN espera"+System.currentTimeMillis());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size() > cantIniComentarios)) {
			// if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size() > cantIniComentarios)) {
			System.out.println("[INFO] El Click no descargó nuevos comentarios");
			return false;
		} else {
			System.out.println("[INFO] El click trajo nuevos comments: +" + ((container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size()) - cantIniComentarios));
			// System.out.println("[INFO] El click trajo nuevos comments: +" + ((container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size()) - cantIniComentarios));
			try {
				// System.out.println("[INFO]Esperando carga de
				// comentarios..."+System.currentTimeMillis());
				Thread.sleep(800);
				System.out.println("[INFO] TOTAL comments: +" + (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size()));
				// System.out.println("[INFO] TOTAL comments: +" + (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size()));
				// System.out.println("[INFO]FIN espera"+System.currentTimeMillis());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}
	}

	/**
	 * Se cargan todas las publicaciones, haciendo scrolls, del timestamp definido en las variables del CONFIG.
	 */
	public Comment extractCommentData(WebElement comentario) {
		Comment auxComment = new Comment();

		// Mensaje
		if (comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size() > 0) {
			auxComment.setMensaje(comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).getText());
		} else {
			// Puede ser porque postea solo una imagen...
			auxComment.setMensaje("");
		}
		// Usuario
		if (this.getAccess() != null) {
			String ini = "id=";
			String fin = "&";
			String pathUserID = comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT)).getAttribute("data-hovercard");
			// System.out.println("USERID CORTADO: " +
			// pathUserID.substring(pathUserID.indexOf(ini)+(ini.length()+1),pathUserID.indexOf(fin)));
			auxComment.setUserId(pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));

		}

		// Utime
		// System.out.println("USTIME: " +
		// comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime"));
		auxComment.setUTime(comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime"));

		// System.out.println("[INFO] COmentario procesado:"+auxComment.toString());
		return auxComment;
	}

	public boolean waitForJStoLoad() {
		System.out.println("[INFO] Waiting for JS Load!");
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete")) {
					System.out.println("[INFO] DocumentReadyState COMPLETE");
					return true;
				} else {
					System.out.println("[INFO] DocumentReadyState UNCOMPLETE");
					return false;
				}

				// return (Boolean)((JavascriptExecutor)driver).executeScript("return
				// jQuery.active == 0");
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(30, TimeUnit.SECONDS).pollingEvery(5, TimeUnit.SECONDS).ignoring(NoSuchElementException.class);

		return wait.until(jsLoad);
	}

	public void moveTo(WebElement element) {
		this.getActions().moveToElement(element);
		this.getActions().perform();
	}

	public Publication extractPublicationData(WebElement publication) {
		Publication aux = new Publication();
		/**
		 * Extraigo ID del post
		 */
		String anchor = publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href");
		// POST ID: https://www.facebook.com/mauriciomacri/videos/10156385274043478/
		String[] stringArray = anchor.split("/");
		// System.out.println("POST ID: " + stringArray[stringArray.length-1]);
		aux.setId(stringArray[stringArray.length - 1]);

		/**
		 * TIMESTAMP El timestamp viene en GMT.
		 */
		aux.setTimeStamp(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));

		/**
		 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO... SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
		 */
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TITLE)) {
			// puede ser que una publicación no tenga título y puede ser que tenga un link
			// de "ver más", al cual hacerle click.
			this.clickViewMoreTextContent(publication, FacebookConfig.XPATH_PUBLICATION_TITLE_VER_MAS);
			aux.setTitulo(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TITLE)).getText());
		} else {
			aux.setTitulo(null);
		}

		/**
		 * OWNER La pubicación siempre tiene un OWNER.
		 */
		aux.setOwner(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER)).getText());// .getAttribute("aria-label"));

		/**
		 * DATETIME
		 */
		String d = (publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP))).getAttribute("title");
		// martes, 6 de marzo de 2018 a las 6:59 --> 03/06/2018 06:59
		d = this.dateFormat(d);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		try {
			Date date = simpleDateFormat.parse(d);
			aux.setDateTime(date);
		} catch (ParseException ex) {
			System.out.println("NO SE PUDO CONVERTIR EN DATE EL STRING DEL POST " + d + " Stack Error" + ex);
		}

		/**
		 * CANTIDAD DE REPRODUCCIONES
		 */
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)) {
			aux.setCantReproducciones(Integer.parseInt(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)).getText().replaceAll("\\D+", "")));
		} else {
			aux.setCantReproducciones(null);
		}
		/**
		 * CANTIDAD DE SHARES
		 */
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)) {
			aux.setCantShare(Integer.parseInt(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).getText().replaceAll("\\D+", "")));
		} else {
			aux.setCantShare(0);
		}
		return aux;
	}

	/**
	 * SIN LOGIN por el momento sin uso.
	 */
	public void obtainPublicationsAndCommentsNotLoggedIn(String facebookPage) {
		this.getDriver().navigate().to(FacebookConfig.URL + facebookPage);

		List<WebElement> publicationsElements;
		// Busco todas las publicaciones que se cargaron. (Si entras sin usuario
		// logueado, te carga 16 publicaciones de una vez).
		if (this.existElement(null, FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)) {
			publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
			List<Publication> publicationsImpl = new ArrayList<Publication>();

			for (int i = 0; i < publicationsElements.size(); i++) {
				System.out.println(" =============== " + i + " DATOS PUBLICACIÓN ================= ");
				Publication aux = new Publication();

				/**
				 * TIMESTAMP El timestamp viene en GMT.
				 */
				aux.setTimeStamp(Long.parseLong(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));

				/**
				 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO... SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_TITLE)) {
					// puede ser que una publicación no tenga título y puede ser que tenga un link
					// de "ver más", al cual hacerle click.
					this.clickViewMoreTextContent(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_TITLE_VER_MAS);
					aux.setTitulo(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TITLE)).getText());
				} else {
					aux.setTitulo(null);
				}

				/**
				 * OWNER La pubicación siempre tiene un OWNER.
				 */
				aux.setOwner(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER)).getText());// .getAttribute("aria-label"));
				/**
				 * DATETIME Tener en cuenta que es GMT+4, porque es el del usuario. (controlar cuando la cuenta a scrapear sea de otro país, qué muestra? la del usuario que consulta o la del owner de la cuenta?.) TODO Si son posts, anteriores al día de la fecha, el formato del String cambia a: martes, 6 de marzo de 2018 a las 6:59
				 */
				String d = (publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP))).getAttribute("title");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
				try {
					Date date = simpleDateFormat.parse(d);
					aux.setDateTime(date);
				} catch (ParseException ex) {
					System.out.println("Exception " + ex);
				}

				/**
				 * CANTIDAD DE REPRODUCCIONES
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)) {
					aux.setCantReproducciones(Integer.parseInt(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)).getText().replaceAll("\\D+", "")));
				} else {
					aux.setCantReproducciones(null);
				}
				/**
				 * CANTIDAD DE SHARES
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)) {
					aux.setCantShare(Integer.parseInt(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).getText().replaceAll("\\D+", "")));
				} else {
					aux.setCantShare(0);
				}

				// Lo almaceno en un array.
				System.out.println("CAPTURADOS_ " + aux.toString());
				publicationsImpl.add(aux);

			}
			this.printPublications(publicationsImpl);
		} else {
			System.out.println("[ERROR] No se encontraron las publicaciones.");
		}

		// Si momento 0 al cargar la página no hay publicaciones, entonces busco el
		// botón más:
		/*
		 * while(publicationsElements.size()==0) {
		 * 
		 * if(this.getDriver().findElements(By.xpath(FacebookConfig. XPATH_PPAL_BUTTON_SHOW_MORE)).size()==1) { System.out.println("Show more"); WebDriverWait wait = new WebDriverWait(this.getDriver(), 10); WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(FacebookConfig. XPATH_PPAL_BUTTON_SHOW_MORE))); System.out.println("CLICK!"); element.click(); } if(this.getDriver().findElements(By.xpath(FacebookConfig. XPATH_PUBLICATIONS_CONTAINER)).size()>0) {
		 * publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig. XPATH_PUBLICATIONS_CONTAINER)); }
		 * 
		 * }
		 */
		/*
		 * publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig. XPATH_PUBLICATIONS_CONTAINER));
		 * 
		 * File scrFile2 = ((TakesScreenshot)this.getDriver()).getScreenshotAs(OutputType.FILE);
		 * 
		 * try { FileUtils.copyFile(scrFile2, new File("c:\\tmp\\screenshot8887.png")); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		/*
		 * System.out.println(publications.get(i).getText()); //La publicacion tiene para ver más comentarios? //this.loadAllPublicationComments(publications.get(i));
		 * 
		 * 
		 * //Por ahora solo me fijo 1 vez si tiene el boton de VER MAS COMENTARIOS try { publications.get(i).findElement(By.xpath("//a[@class='UFIPagerLink']")).click (); this.getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); } catch (NoSuchElementException e) { System.out.println("Element Not Found");
		 * 
		 * }
		 * 
		 * List<WebElement> comments = publications.get(i).findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)); this.obtainPublicationComments(comments); System.out.println(" ==============="+i+" FIN================= ");
		 */
		// this.obtainPublicationComments(publications.get(i));
		/*
		 * ESTE ES EL FORMATO DE EXTRACCIÓN: Mauricio Macri 17 h · CON BOLSAS DE COMIDA PARA PERRO FABRICA MOCHILAS También usa carteles de la vía pública para fabricar bolsos, cartucheras y fundas de skate y surf, mientras les enseña un oficio a vecinos de Melchor Romero. Hoy recibí a Iván en Olivos. Swahili fundas 320.202 reproducciones
		 */
		/*
		 * }
		 */
	}

	private boolean loggedIn() {
		try {
			this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_FORM_LOGIN));
			System.out.println("[ERROR]Login error! check credentials provided");
			return false;
		} catch (NoSuchElementException e) {
			return true;
		}
	}

	private boolean existElement(WebElement element, String xpathExpression) {
		if (element == null)
			return ((this.getDriver().findElements(By.xpath(xpathExpression))).size() > 0);
		else
			return ((element.findElements(By.xpath(xpathExpression))).size() > 0);

	}

	/**
	 * Es el 'more text' que puede aparecer en el titulo de una publicación cuando es muy larga...
	 * 
	 * @param element
	 * @param xpathExpression
	 */
	private void clickViewMoreTextContent(WebElement element, String xpathExpression) {
		boolean verMasClicked = false;
		while (this.existElement(element, xpathExpression) && (!verMasClicked)) {
			WebElement we = element.findElement(By.xpath(xpathExpression));
			if (we.isDisplayed()) {
				we.click();
				verMasClicked = true;
			} else {
				System.out.println("[ERROR] VER MAS TITLE NOT DISPLAYED");

			}
		}

	}

	public void printPublications(List<Publication> lista) {
		if (lista != null) {
			System.out.println("SE ENCONTRARON UN TOTAL DE " + lista.size() + "PUBLICACIONES");
			for (int j = 0; j < lista.size(); j++) {
				System.out.println("============== PUBLICATION " + j + " INICIO	===============");
				System.out.println(lista.get(j).toString());
				System.out.println("************** PUBLICATION " + j + " FIN	***************");
			}
		} else {
			System.out.println("[INFO] PrintPublications():LA LISTA DE PUBLICACIONES PARA IMPRIMIR ESTÁ VACÍA.");
		}
	}

	private void saveScreenShot(String name) {
		File scrFile = ((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File("c:\\tmp\\" + name + System.currentTimeMillis() + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean navigateTo(String URL) {
		this.getDriver().navigate().to(URL);

		// Si por algún motivo se carga una URL que no existe, ej: https://www.facebook2342.com/
		if (this.existElement(null, "//body[@class='neterror']")) {
			System.out.println("[ERROR] NET ERROR ACCESS: " + this.getDriver().findElement(By.xpath("//body[@class='neterror']//div[@id='main-message']")).getText());
			return false;
		}

		if (this.existElement(null, "//div[contains(@id,'globalContainer')]//a[contains(@href,'ref=404')]")) {
			/**
			 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente, ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost inexistente) - id post válido, pero URL inválida https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
			 */
			System.out.println("[ERROR] NO EXISTE LINK " + URL + ": " + this.getDriver().findElement(By.xpath("//div[contains(@id,'globalContainer')]//h2")).getText());
			return false;
		}

		return true;
	}

	private String monthHelper(String month) {
		String mes = "";
		switch (month) {
		case "january":
		case "enero":
		case "jan":
			mes = "01";
			break;

		case "febuary":
		case "febrero":
		case "feb":
			mes = "02";
			break;

		case "march":
		case "marzo":
		case "mar":
			mes = "03";
			break;

		case "april":
		case "apr":
		case "abril":
			mes = "04";
			break;

		case "may":
		case "mayo":
			mes = "05";
			break;

		case "june":
		case "jun":
		case "junio":
			mes = "06";
			break;

		case "july":
		case "jul":
		case "julio":
			mes = "07";
			break;

		case "august":
		case "aug":
		case "agosto":
			mes = "08";
			break;

		case "september":
		case "sep":
		case "sept":
		case "septiembre":
			mes = "09";
			break;

		case "october":
		case "oct":
		case "octubre":
			mes = "10";
			break;

		case "november":
		case "nov":
		case "noviembre":
			mes = "11";
			break;

		case "december":
		case "dec":
		case "diciembre":
			mes = "12";
			break;
		}

		return mes;

	}

	private String dateFormat(String d) {
		/**
		 * DATETIME Tener en cuenta que es GMT+4, porque es el del usuario. (controlar cuando la cuenta a scrapear sea de otro país, qué muestra? la del usuario que consulta o la del owner de la cuenta?.) HECHO: Si son posts, anteriores al día de la fecha, el formato del String cambia a: martes, 6 de marzo de 2018 a las 6:59 De lo contrario el formato que se parsea bien es este: 24/03/2018 22:33
		 */

		if (d.length() > "MM/dd/yyyy HH:mm".length()) {
			String[] stringArrayDate = d.split("de ");

			String dia = (stringArrayDate[0].substring((stringArrayDate[0].indexOf(" ") + 1), (stringArrayDate[0].indexOf(" ") + 3)));
			dia = (dia.length() == 1) ? "0" + dia : dia;

			String mes = this.monthHelper(stringArrayDate[1].trim());
			String anio = stringArrayDate[2].substring(0, stringArrayDate[2].indexOf(" "));
			String HHmm = stringArrayDate[2].substring(stringArrayDate[2].length() - 5).trim();

			d = mes + "/" + dia + "/" + anio + " " + HHmm;
		}

		return d;
	}
}
