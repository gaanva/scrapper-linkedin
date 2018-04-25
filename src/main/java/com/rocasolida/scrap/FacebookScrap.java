package com.rocasolida.scrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.FacebookConfig;
import com.rocasolida.entities.Comment;
import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Page;
import com.rocasolida.entities.Publication;
import com.rocasolida.scrap.util.Driver;

public class FacebookScrap extends Scrap {

	private Page page;
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);

	public FacebookScrap(Driver driver) throws MalformedURLException {
		super(driver);
		this.page = new Page();
	}

	public boolean login(Credential access) {
		if (this.navigateTo(FacebookConfig.URL)) {
			//this.saveScreenShot("LOGIN");
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
		// Por lo visto: Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES
		// UN PERFIL
		// XPATH:
		// //div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a
		// getAttribute('data-tab-key') == 'timeline' (Biografía)
		// getAttribute('data-tab-key') == 'friends'
		// ASUMO QUE SI (Encuentro: INICIO || PUBLICACIONES || COMUNIDAD EN MENU DE LA
		// IZQ) ES UNA PÁGINA.
		try {
			if (this.existElement(null,
					"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts' or @data-key='tab_community' or @data-key='tab_home']//descendant::a")) {
				return "PAGE";
			}
			// Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
			if (this.existElement(null,
					"//div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a")) {
				return "PROFILE";
			}
		} catch (Exception e) {
			System.out.println("[ERROR] AL COMPROBAR TIPO DE LINK (PAGINA | PERFIL)");
			this.saveScreenShot("ERR_COMPR_LINK");
		}

		return "";

	}

	public Page obtainPageInformation(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Long COMMENTS_uTIME_INI,
			Long COMMENTS_uTIME_FIN) {
		// Obtengo todas las publicaciones que tienen fecha del post mayor a la fecha
		// inicial...
		List<WebElement> publicationsElements = this.inicializePublicationsToBeLoad(facebookPage, uTIME_INI, uTIME_FIN);
		if (publicationsElements != null) {
			List<Publication> publicationsImpl = new ArrayList<Publication>();
			// Se extraen datos del POST
			for (int i = 0; i < publicationsElements.size(); i++) {
				if (this.waitForJStoLoad()) {
					this.moveTo(publicationsElements.get(i));
					publicationsImpl.add(this.extractPublicationData(publicationsElements.get(i)));
				} else {
					System.out.println("[ERROR] PROBLEMAS AL EXTRAER DATOS DEL POST.");
					this.saveScreenShot("PROBLEMA_EXTRAER_DATOSPOST");
				}

			}

			for (int i = 0; i < publicationsImpl.size(); i++) {

				// Acá debería no ir a este post, e ir al listado.
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION NRO#" + (i + 1) + ": "
						+ FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST
						+ publicationsImpl.get(i).getId());
				try {
					this.getDriver().navigate().to(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST
							+ publicationsImpl.get(i).getId());
				} catch (Exception e) {
					System.out.println("[ERROR] NO SE PUDO ACCEDER AL LINK DEL POST");
					this.saveScreenShot("ERR_ACCESO_POST");
				}

				
				// Hago una espera para que cargue la página
				this.waitForPageLoaded();
				//if (!((this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER + "[1]")))
					//	.size() > 0)) {
					// System.out.println("El post no está visble en la página... se hace una
					// espera...");
					//this.waitForJStoLoad();
				//}

				List<WebElement> pubsNew;
				try {
					// Controla que no tire el popup de login
					if (this.getAccess() == null) {
						this.checkAndClosePopupLogin();
					}

					pubsNew = this.getDriver()
							.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER + "[1]"));

					// if(this.getDriver().findElements(By.xpath("//a[@class='_xlt
					// _418x']")).size()>0) {
					if (this.getAccess() != null) {
						this.overlayHandler();
						
					}

					if (this.getAccess() == null) {
						//this.checkAndClosePopupLogin();
						
						if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)) {
							this.checkAndClosePopupLogin();

							JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
							jsx.executeScript("window.scrollBy(0,500)", "");
							this.moveTo(pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)));
							pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL))
							.click();
							publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew.get(0),COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN));
							
							
							
							//System.out.println("SCROLL.....");
							//this.saveScreenShot("SXROLL_POST1");
							/*
							try {
								pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL))
										.click();
								publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew.get(0),
										COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN));
							} catch (Exception e) {
								if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException")) {
									JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
									jsx.executeScript("window.scrollBy(0,500)", "");
									this.moveTo(pubsNew.get(0)
											.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)));
								}
								try {
									pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL))
											.click();
									publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew.get(0),
											COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN));
								} catch (Exception e1) {
									System.out.println("No se pudo seleccionar la parte de comentarios del Post "
											+ e.getClass().getSimpleName());
									this.saveScreenShot("ElementClickInterceptedException");
									e1.printStackTrace();
								}

							}
							*/
							
						} else {
							System.out.println("[INFO] LA PUBLICACION NO TIENE COMENTARIOS");
							
						}
					} else {
						if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER)) {
							publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew.get(0), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN));
						}else {
							System.out.println("[INFO] LA PUBLICACION NO TIENE COMENTARIOS");
						}
					}
					this.page.setPublications(publicationsImpl);

				} catch (Exception e) {
					System.out.println("[ERROR] AL ACCEDER AL POST.");
					e.printStackTrace();
					this.saveScreenShot("ERR_ACCESO_POST");
				}
			}

			return this.page;
		} else {
			System.out.println("[INFO] NO SE ENCONTRARON PUBLICACIONES PARA PROCESAR.");
			return null;
		}
	}

	public List<Comment> extractPubComments(WebElement pub, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) {
		if (this.existElement(pub, FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")) {
			this.TipoCargaComentarios(pub, 3);
			System.out.println("[INFO] OBTENIENDO LOS COMENTARIOS DEL POST: ");
			return this.obtainAllPublicationComments(
					pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")),
					FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		} else {
			System.out.println("[INFO] LA PUBLICACION NO TIENE COMENTARIOS.");
			this.saveScreenShot("INFO_PUB_SIN_COMENTARIOS");
			return null;
		}
	}

	private Long extractNumberFromString(String text) {
		final Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String likesCount = matcher.group(0);
			if (likesCount.contains(",")) {
				likesCount = likesCount.replace(",", "");
			}
			if (likesCount.contains(".")) {
				likesCount = likesCount.replace(".", "");
			}
			return Long.parseLong(likesCount);
		}
		return null;
	}

	public void updatePageLikes() {
		try {
			this.moveTo(this.getDriver().findElement(By.xpath("//div[@class='_4-u2 _6590 _3xaf _4-u8']")));

			List<WebElement> likesAndFollowers = this.getDriver()
					.findElements(By.xpath("//div[@class='_4-u2 _6590 _3xaf _4-u8']/div[@class='_2pi9 _2pi2']"));

			if (likesAndFollowers != null && likesAndFollowers.size() == 2) {
				Long likeCount = extractNumberFromString(likesAndFollowers.get(0).getText());

				if (likeCount != null) {
					this.page.setLikes(likeCount);
				} else {
					// Sent to slack if error
					throw new Exception("Cannot extract like count");
				}

				Long followersCount = extractNumberFromString(likesAndFollowers.get(1).getText());

				if (followersCount != null) {
					this.page.setFollowers(followersCount);
				} else {
					// Sent to slack if error
					throw new Exception("Cannot extract followers count");
				}

			} else {
				// TODO Sent to slack if error
				throw new Exception(
						"Cannot extract likesAndFollowers from //div[@class='_4-u2 _6590 _3xaf _4-u8']/div[@class='_2pi9 _2pi2']");
			}

		} catch (Exception e) {
			// TODO Sent to slack if error
			System.out.println("[ERROR] AL OBTENER LIKES AND FOLLOWERS!.");
			this.saveScreenShot("ERROR_LIKES_FOLLOWERS");
		}

	}

	public List<WebElement> inicializePublicationsToBeLoad(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {

		if (this.navigateTo(FacebookConfig.URL + facebookPage)) { // SI NO TIRA ERROR DE CONEXIÓN O DE PAGINA
																	// INEXISTENTE...
			this.updatePageLikes();
			System.out.println("[INFO] SE CARGÓ EL LINK: " + FacebookConfig.URL + facebookPage);
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
			try {

				this.overlayHandler();
				this.goToPublicationsSection();
				//this.waitForPageLoaded();
				this.waitForPublicationsLoaded();
				//this.saveScreenShot("PubsLoadead");
				System.out.println("[INFO] BUSCANDO PUBLICACIONES ENTRE EL RANGO DE FEHCAS DADA....");
				if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
					while (!((this.getDriver()
							.findElements(By.xpath(FacebookConfig
									.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookPage, uTIME_INI)))
							.size()) > 0)) {
						/**
						 * TODO Buscar una manera de que espere a que termine el scroll para evitar
						 * poner el sleep del proceso arbitrariamente.
						 */
						
						if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
							this.scrollMainPublicationsPage();
						} else {
							System.out.println(
									"[INFO] YA SE RECORRIERON TODAS LAS PUBLICACIONES DE LA PÁGINA. NO SE ENCONTRÓ BTN SHOW MORE: "
											+ FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
							break;
						}
						System.out.print("...|");
					}
					System.out.println("|FIN|");
				} else {
					System.out
							.println("[INFO] LA PAGINA NO TIENE NUNGUNA PUBLICACION o NO TUVO EL TIEMPO PARA CARGARSE");
					this.saveScreenShot("PAGINA_SIN_PUBS");
					return null;
				}
			} catch (Exception e) {
				System.err.println("[ERROR] EN LA CARGA DE PUBLICACIONES.");
				e.printStackTrace();
				this.saveScreenShot("ERR_CARGA_PUBS");
			}
		} catch (Exception e) {
			System.err.println("[ERROR] ERROR INESPERADO.");
			this.saveScreenShot("ERR_INESPERADO");
			return null;
		}
		// RETORNO SOLO LAS PUBLICACIONES QUE CUMPLIERON CON EL FILTRO.
		return this.filterPostsByUTIME(facebookPage, uTIME_INI, uTIME_FIN);

	}

	public boolean overlayHandler() {
		/*
		if (this.getDriver().findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
			// Pone un frame cuando el browser te pide notificaciones...
			System.out.println("[INFO] Se detectó overlay al cargar... cerrando overlay de la carga...");
			try {
				this.getActions().sendKeys(Keys.ESCAPE).perform();
			} catch (Exception e) {
				System.err.println("[ERROR] NO SE PUDO CERRAR EL OVERLAY.");
			}
		}
		*/
		ExpectedCondition<Boolean> overlayClosed = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
					(new Actions(driver)).sendKeys(Keys.ESCAPE).perform();
					return false;
				} else {
					return true;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(5))
				.pollingEvery(Duration.ofSeconds(1));

		return wait.until(overlayClosed);

		
		
		
		
	}

	public void goToPublicationsSection() {
		if (this.getAccess() == null) {
			String posts = this.getDriver().findElement(By.xpath(
					"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a"))
					.getAttribute("href");
			// ACcedo por la URL, puede que el elemento nunca me quede visible.
			this.navigateTo(posts);
		} else {
			try {
				// Accedo por el click en el menú opción "Publicaciones"
				((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(true);",
						this.getDriver().findElement(By.xpath(
								"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")));
				this.getDriver().findElement(By.xpath(
						"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a"))
						.click();

			} catch (Exception e) {
				System.err.println("[ERROR] NO SE PUDO ACCEDER AL MENÚ 'PUBLICACIONES'");
				this.saveScreenShot("ERR_ACCEDER_PUBLICACIONES");
			}

		}
	}

	public List<WebElement> filterPostsByUTIME(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {
		int match = this.getDriver()
				.findElements(By
						.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN)
								+ "//ancestor::div[contains(@class,'userContentWrapper')]"))
				.size();
		if (match > 0) {
			System.out.println("[INFO] SE ENCONTRARON " + String.valueOf(match) + " PUBLICACIONES ENTRE LAS FECHAS > a "
					+ uTIME_INI + " y < a " + uTIME_FIN);
			return this.getDriver().findElements(
					By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN)
							+ "//ancestor::div[contains(@class,'userContentWrapper')]"));
		} else {
			System.out.println("[ERROR] NO SE ENCONTRARON PUBLICACIONES EN LAS FECHAS INDICADAS.");
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
		//this.saveScreenShot("Scroll_MainPAge");
	}

	/**
	 * 
	 * @param pubsLoaded
	 * @param posIni
	 * @return true si al menos 1 publicación es menor a la fecha inicial.
	 */
	public boolean continueScroll(List<WebElement> pubsLoaded, int posIni, Long uTIME_INI) {
		for (int i = posIni; i < pubsLoaded.size(); i++) {
			if (pubsLoaded.get(i)
					.findElements(
							By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED("", uTIME_INI)))
					.size() > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Si existe el botón de show more, entonces lo clickea, hasta que se cargaron
	 * todos los mensajes para luego obtenerlos con un XPATH query y extraerle los
	 * datos. Me servirá para las replies y para los comentarios.
	 */
	public List<Comment> obtainAllPublicationComments(WebElement container, String xPathExpression,
			Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) {

		List<WebElement> comentarios = new ArrayList<WebElement>();
		List<Comment> comments = new ArrayList<Comment>();
		if (container.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container
					.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
		}
		// Si existe el botón de "Ver Más mensajes"
		// if (container.findElements(By.xpath(xPathExpression)).size() > 0) {
		if (this.existElement(container, xPathExpression)) {
			// ---
			// int cantIniComentarios =
			// container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size();

			WebElement showMoreLink = container.findElement(By.xpath(xPathExpression));
			this.moveTo(showMoreLink);
			try {
				showMoreLink.click();
				// this.saveScreenShot("AFTER_1click_OK");
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException")) {
					this.getActions().sendKeys(Keys.SPACE);
				}

				this.moveTo(showMoreLink);
				this.saveScreenShot("[ERROR]click_verMAsMsgs");
				showMoreLink.click();

			}

			int cantReintentos = 0;
			Long cantRequests = 0L;
			Long totalRequests = (Long) ((JavascriptExecutor) this.getDriver())
					.executeScript("return window.performance.getEntries().length;");

			// while (cantReintentos < 3) {
			while (totalRequests > cantRequests && cantReintentos < 3) {
				cantRequests = totalRequests;
				try {
					/*
					 * SIEMPRE ME TIRA COMPLETE switch (((JavascriptExecutor)
					 * this.getDriver()).executeScript("return document.readyState").toString()) {
					 * case "loading": System.out.println("LOADING"); // The document is still
					 * loading. break; case "interactive": // The document has finished loading. We
					 * can now access the DOM elements. System.out.println("INTERACTIVE!"); break;
					 * case "complete": // The page is fully loaded. System.out.println("COMPLETE");
					 * break; }
					 */

					// Time to load DOM Content
					/*
					 * long domLoadEventEnd = (Long)
					 * js.executeScript("return window.performance.timing.domContentLoadedEventEnd;"
					 * ); long fetchStart = (Long)
					 * js.executeScript("return window.performance.timing.fetchStart;");
					 * System.out.println("DOM Content Loaded: " + (domLoadEventEnd - fetchStart) +
					 * " ms.");
					 */
					// Time to load entire page
					/*
					 * long loadEventEnd = (Long)
					 * js.executeScript("return window.performance.timing.loadEventEnd;");
					 * System.out.println("Loaded: " + (loadEventEnd - fetchStart) + " ms.");
					 */

					// Si existe el botón mostrar más...
					// if(container.findElements(By.xpath(xPathExpression)).size()>0) {
					if (this.existElement(container, xPathExpression)) {
						showMoreLink = container.findElement(By.xpath(xPathExpression));
						this.moveTo(showMoreLink);
						showMoreLink.click();
						Thread.sleep(300);

						JavascriptExecutor js = (JavascriptExecutor) this.getDriver();
						// Total requests, made to get page rendered
						totalRequests = (Long) js.executeScript("return window.performance.getEntries().length;");
						System.out.print("... ");

						if (totalRequests > cantRequests) {
							cantReintentos = 0;
						} else {
							totalRequests++;
							cantReintentos++;
						}

					} else {

						try {
							// this.scrollMainPublicationsPage();
							JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
							jsx.executeScript("window.scrollBy(0,500)", "");
							// Me muestra si realmente se llegó al último mensaje...
							this.saveScreenShot("FINMENSAJES1");
							// showMoreLink = container.findElement(By.xpath(xPathExpression));
							// this.moveTo(showMoreLink);
							// showMoreLink.click();
							// this.saveScreenShot("FINMENSAJES2");
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (cantReintentos < 2) {
							JavascriptExecutor js = (JavascriptExecutor) this.getDriver();
							// Total requests, made to get page rendered
							totalRequests = (Long) js.executeScript("return window.performance.getEntries().length;");
							// System.out.println("Total requests: " + totalRequests);

							totalRequests++;
							cantReintentos++;
						} else {
							break;
						}

					}

				} catch (Exception e) {
					// this.saveScreenShot("elementStale_");
					// System.out.println("ERROR: " + e.toString());
					cantReintentos++;
				}
			}

		} else {
			System.out.println("NO HAY MÁS MENSAJES PARA CARGAR.");
		}

		System.out.println("[INFO] TOTAL COMENTARIOS LEIDOS: "
				+ container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
		// this.saveScreenShot("SCREEN_SCRAWLED_"+String.valueOf(System.currentTimeMillis()));
		if (COMMENTS_uTIME_FIN != null) {
			comentarios = container.findElements(By.xpath(".//abbr[@data-utime>=" + String.valueOf(COMMENTS_uTIME_INI)
					+ " and @data-utime<=" + String.valueOf(COMMENTS_uTIME_FIN)
					+ "]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList'])]"));
			System.out.println("[INFO] TOTAL COMENTARIOS (CON FILTRO DE UTIME): " + comentarios.size());
		} else {
			System.out.println("[INFO] TOTAL COMENTARIOS (SIN FILTRO DE UTIME): "
					+ container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
			comentarios = container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		}

		// System.out.println("Comentarios:
		// "+this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size());
		// comentarios =
		// this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV));
		// container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size();
		// System.out.println("[INFO] PROCESANDO: " + comentarios.size() + "
		// COMENTARIOS.");
		System.out.print("[INFO]PROCESANDO COMENTARIO: ");
		for (int j = 0; j < comentarios.size(); j++) {
			comments.add(this.extractCommentData(comentarios.get(j)));
			System.out.print(j + "|");
		}
		System.out.println("FIN");
		System.out.println("[INFO] SE PROCESARON TODOS LOS COMENTARIOS. (" + comentarios.size() + ")");

		// System.out.println("[INFO] CANTIDAD TOTAL DE COMENTARIOS PROCESADOS: " +
		// comments.size());
		// System.out.println("[TIME] Extract COMMENT FIN: " +
		// System.currentTimeMillis());
		return comments;
	}

	/*
	 * public List<Comment> obtainAllPublicationComments(WebElement container,
	 * String xPathExpression, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) {
	 * //Cantidad total de request que llevó cargar la página hasta el momento. Long
	 * cantInicialRequest = (Long)((JavascriptExecutor) this.getDriver()).
	 * executeScript("return window.performance.getEntries().length;");
	 * 
	 * //int cantIniComentarios =
	 * container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size();
	 * 
	 * if(this.existElement(container, xPathExpression)) {
	 * 
	 * WebElement showMoreCommentsLink =
	 * container.findElement(By.xpath(xPathExpression)); //Si el link existe,
	 * debería hacer el click sin problemas... y me debeía cambiar el total de
	 * requests. this.clickShowMoreComments(showMoreCommentsLink); Long
	 * cantNuevaRequest = (Long)((JavascriptExecutor) this.getDriver()).
	 * executeScript("return window.performance.getEntries().length;");
	 * 
	 * 
	 * this.moveTo(showMoreLink); try { showMoreLink.click(); Thread.sleep(150);
	 * //this.saveScreenShot("AFTER_1click_OK"); } catch(Exception e) {
	 * if(e.getClass().getSimpleName().equalsIgnoreCase(
	 * "ElementClickInterceptedException")) {
	 * this.getActions().sendKeys(Keys.SPACE); }
	 * 
	 * this.moveTo(showMoreLink); this.saveScreenShot("[ERROR]click_verMAsMsgs");
	 * showMoreLink.click();
	 * 
	 * }
	 */

	// Variable de control, por si en una de las veces la cantidad de requests no se
	// actualiza...
	/*
	 * int cantReintentos = 0;
	 * 
	 * 
	 * System.out.println("Cant Inicial Request: " + cantInicialRequest);
	 * System.out.println("Cant Nueva Request: " + cantNuevaRequest);
	 * //System.out.println("Leyendo mensajes...");
	 * 
	 * while(cantNuevaRequest>cantInicialRequest && cantReintentos<2) {
	 * System.out.print("... "); cantInicialRequest=cantNuevaRequest; //Si existe el
	 * botón mostrar más...
	 * //if(container.findElements(By.xpath(xPathExpression)).size()>0) {
	 * if(this.existElement(container, xPathExpression)) { showMoreCommentsLink =
	 * container.findElement(By.xpath(xPathExpression));
	 * if(this.clickShowMoreComments(showMoreCommentsLink)){
	 * System.out.println("Click en Show more ok."); cantNuevaRequest =
	 * (Long)((JavascriptExecutor) this.getDriver()).
	 * executeScript("return window.performance.getEntries().length;");
	 * System.out.println("Cant Requests: " + cantNuevaRequest); }else {
	 * System.out.println("Click en Show more NON ok."); if(cantReintentos<2) {
	 * cantReintentos++; //sumo uno para que pase la condición del while
	 * cantNuevaRequest++; }
	 * this.saveScreenShot("NOMORECOMMENTSLINK_"+cantReintentos+"_"); //No hay más
	 * mensajes para mostrar }
	 * 
	 * }else { if(cantReintentos<2) { cantReintentos++; //sumo uno para que pase la
	 * condición del while cantNuevaRequest++; }
	 * this.saveScreenShot("NOMORECOMMENTSLINK_"+cantReintentos+"_"); //No hay más
	 * mensajes para mostrar } //cantIniComentarios =
	 * (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
	 * //System.out.print("|"+cantIniComentarios); }
	 * 
	 * }else { this.saveScreenShot("NOMORECOMMENTSLINK_");
	 * System.out.println("No se encontró el SHOWMORECOMMENTSLINK"); }
	 * System.out.println("FIN LECTURA DE COMENTARIOS|"); /* while (cantReintentos <
	 * 3) { try { //Si existe el botón mostrar más...
	 * if(container.findElements(By.xpath(xPathExpression)).size()>0) { showMoreLink
	 * = container.findElement(By.xpath(xPathExpression));
	 * this.moveTo(showMoreLink); showMoreLink.click(); if
	 * (this.ctrlClickHasEffect(container, cantIniComentarios)) { cantReintentos =
	 * 0; cantIniComentarios =
	 * (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
	 * System.out.print("|"+cantIniComentarios); if(COMMENTS_uTIME_INI!= null) {
	 * if(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS+
	 * ".//abbr[@data-utime<"+ String.valueOf(COMMENTS_uTIME_INI) + "]")).size()>0)
	 * { //Si encuentro alguno con fecha mayor a la inicial dada... break; } }
	 */
	// cantIniComentarios =
	// (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size());
	/*
	 * if (cantIniComentarios > 2200) { break; }
	 */
	/*
	 * }else { break; }
	 * 
	 * } catch (Exception e) { //this.saveScreenShot("elementStale_");
	 * //System.out.println("ERROR: " + e.toString()); cantReintentos++; } }
	 * 
	 * 
	 * System.out.println("[INFO] TOTAL COMENTARIOS: " +
	 * container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size()); }
	 * else { System.out.println("NO HAY MÁS MENSAJES PARA CARGAR."); }
	 * 
	 */
	// this.saveScreenShot("SCREEN_SCRAWLED_"+String.valueOf(System.currentTimeMillis()));
	/*
	 * System.out.println("Cant Final requests: " + (Long)((JavascriptExecutor)
	 * this.getDriver()).
	 * executeScript("return window.performance.getEntries().length;"));
	 * System.out.println("TOTAL COMENTARIOS LEIDOS: " +
	 * (container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size()) );
	 * List<WebElement> comentarios = new ArrayList<WebElement>(); List<Comment>
	 * comments = new ArrayList<Comment>(); if(COMMENTS_uTIME_FIN!=null) {
	 * comentarios = container.findElements(By.xpath(".//abbr[@data-utime>="+
	 * String.valueOf(COMMENTS_uTIME_INI) + " and @data-utime<="+
	 * String.valueOf(COMMENTS_uTIME_FIN)
	 * +"]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList'])]"
	 * )); System.out.println("[INFO] TOTAL COMENTARIOS (CON FILTRO DE UTIME): " +
	 * comentarios.size()); }else {
	 * System.out.println("[INFO] TOTAL COMENTARIOS (SIN FILTRO DE UTIME): " +
	 * container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
	 * comentarios =
	 * container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)); }
	 * 
	 * //
	 * System.out.println("Comentarios: // "+this.getDriver().findElements(By.xpath(
	 * FacebookConfig.
	 * XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.
	 * XPATH_COMMENT_ROOT_DIV)).size()); // comentarios = //
	 * this.getDriver().findElements(By.xpath(FacebookConfig.
	 * XPATH_COMMENTS_CONTAINER)).get(0).findElements(By.xpath(FacebookConfig.
	 * XPATH_COMMENT_ROOT_DIV)); //
	 * container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size(
	 * ); //System.out.println("[INFO] PROCESANDO: " + comentarios.size() +
	 * " COMENTARIOS."); System.out.print("[INFO]PROCESANDO COMENTARIO: "); for (int
	 * j = 0; j < comentarios.size(); j++) {
	 * comments.add(this.extractCommentData(comentarios.get(j))); System.out.print(j
	 * + "|"); } System.out.println("FIN");
	 * System.out.println("[INFO] SE PROCESARON TODOS LOS COMENTARIOS. ("+
	 * comentarios.size() + ")");
	 * 
	 * //System.out.println("[INFO] CANTIDAD TOTAL DE COMENTARIOS PROCESADOS: " +
	 * comments.size()); //System.out.println("[TIME] Extract COMMENT FIN: " +
	 * System.currentTimeMillis()); return comments; }
	 */
	public boolean clickShowMoreComments(WebElement showMoreCommentsLink) {
		try {
			this.moveTo(showMoreCommentsLink);
			showMoreCommentsLink.click();

		} catch (Exception e) {
			System.out.println("[ERROR PARA CATCH] " + e.getClass().getSimpleName());
			if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException")) {
				try {
					this.getActions().sendKeys(Keys.SPACE);
					this.moveTo(showMoreCommentsLink);
					showMoreCommentsLink.click();
				} catch (Exception e1) {
					System.out.println("[ERROR] al intentar hacer click en Ver Mas mensajes.");
					this.saveScreenShot("[ERROR]CLICK_VER_MAS_");
					return false;
				}
			} else {
				System.out.println("[ERROR] al intentar hacer click en Ver Mas mensajes.");
				this.saveScreenShot("[ERROR]CLICK_VER_MAS_");
				return false;
			}
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public void totalCommentsPub(WebElement pub) {
		if (pub.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:"
					+ pub.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
		}
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
	 * 		TODO: Espero arbitrariamente a la carga de los mensajes... debería
	 *         encontrar la forma de esperar a que el REACTJS y el render del DOM
	 *         finalicen.
	 */

	public boolean ctrlClickHasEffect(WebElement container, int cantIniComentarios) {
		if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size() > cantIniComentarios)) {
			// if
			// (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size()
			// > cantIniComentarios)) {
			try {
				// System.out.println("[INFO]Esperando carga de
				// comentarios..."+System.currentTimeMillis());
				Thread.sleep(1000);
				// System.out.println("[INFO]FIN espera"+System.currentTimeMillis());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size() > cantIniComentarios)) {
			// if
			// (!(container.findElements(By.xpath(FacebookConfig.XPATH_COMMENT_ROOT_DIV)).size()
			// > cantIniComentarios)) {
			// System.out.println("[INFO] El Click no descargó nuevos comentarios");
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Se cargan todas las publicaciones, haciendo scrolls, del timestamp definido
	 * en las variables del CONFIG.
	 */
	public Comment extractCommentData(WebElement comentario) {
		Comment auxComment = new Comment();

		// Mensaje
		if (this.existElement(comentario, ".//a[@class='_5v47 fss']")) {
			WebElement aux;
			while (this.existElement(comentario, ".//a[@class='_5v47 fss']")) {
				aux = comentario.findElement(By.xpath(".//a[@class='_5v47 fss']"));
				try {
					this.moveTo(aux);
					aux.click();
					Thread.sleep(50);

				} catch (Exception e) {
					this.saveScreenShot("Error VerMasContenidoMensaje");
					e.printStackTrace();
					break;
				}
			}
		}
		// if
		// (comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size()
		// > 0) {
		if (this.existElement(comentario, FacebookConfig.XPATH_USER_COMMENT)) {
			String aux = "";
			for (int i = 0; i < comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size(); i++) {
				aux += comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).get(i).getText();
			}
			auxComment.setMensaje(aux);
			// auxComment.setMensaje(comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).getText());
		} else {
			// Puede ser porque postea solo una imagen...
			auxComment.setMensaje("");
		}
		// Usuario
		if (this.getAccess() != null) {
			String ini = "id=";
			String fin = "&";
			String pathUserID = comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT))
					.getAttribute("data-hovercard");
			// System.out.println("USERID CORTADO: " +
			// pathUserID.substring(pathUserID.indexOf(ini)+(ini.length()+1),pathUserID.indexOf(fin)));
			auxComment.setUserId(
					pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));

		}

		// Utime
		// System.out.println("USTIME: " +
		// comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime"));
		auxComment.setUTime(
				comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime"));

		// System.out.println("[INFO] COmentario procesado:"+auxComment.toString());
		return auxComment;
	}

	public boolean waitForJStoLoad() {
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
						.equals("complete")) {
					return true;
				} else {
					System.out.println("[INFO] DocumentReadyState UNCOMPLETE");
					return false;
				}

				// return (Boolean)((JavascriptExecutor)driver).executeScript("return
				// jQuery.active == 0");
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(30))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);

		return wait.until(jsLoad);
	}

	public void moveTo(WebElement element) {
		// Se fija si al hacer un scroll, antes o despues, aparea el botn de cerrar.
		if (this.getAccess() == null) {
			this.checkAndClosePopupLogin();
		}

		if (this.getAccess() == null) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({block: \"start\"});",
					element);
			// ((JavascriptExecutor)
			// this.getDriver()).executeScript("arguments[0].scrollIntoView(true);",
			// element);
			// this.saveScreenShot("moveElement_");
		} else {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", element);
			// System.out.println("SCROLL_INTO_ELEMENT");
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("[ERROR] NO SE PUDO HACER LA ESPERA THREAD.SLEEP");
		}
	}

	public void checkAndClosePopupLogin() {
		if (this.existElement(null, "//a[@id='expanding_cta_close_button']")) {
			try {
				this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
			} catch (Exception e) {
				// e.printStackTrace();
				// this.saveScreenShot("ERR_CLOSE_POPUPLOGIN");
			}
		}
	}

	public Publication extractPublicationData(WebElement publication) {
		Publication aux = new Publication();
		// this.saveScreenShot("EXTRACT_PUB_DATA");
		
		/**
		 * Extraigo LINK del post, que es su ID.
		 */
		if (this.regexPostID(
				publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href")) == "") {
			System.out.println("[INFO] ERROR AL ENCONTRAR EL ID DEL POST: "
					+ publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
		} else {
			aux.setId(this.regexPostID(
					publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href")));
		}
		/**
		 * TIMESTAMP El timestamp viene en GMT.
		 */
		
		/*
		 * Hay dos casos (necesito saber el abbr que contiene un timestamp, sino se confunde cuando 
		 * comparten un post de otra cuenta de facebook): 
		 *    <abbr data-utime='' class='timestamp'>
		 *    <abbr data-utime=''><span class='timestamp'>
		 */
		
		if(this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
			aux.setTimeStamp(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP))
					.getAttribute("data-utime")));
		}else if(this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)){
			aux.setTimeStamp(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1))
					.getAttribute("data-utime")));
		}
	
		/**
		 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO...
		 * SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
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
		
		if(this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
			aux.setDateTime((publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)))
					.getAttribute("title"));
		}else if(this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)){
			aux.setDateTime((publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)))
					.getAttribute("title"));
		}
		
		/**
		 * CANTIDAD DE REPRODUCCIONES
		 */
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)) {
			aux.setCantReproducciones(
					Integer.parseInt(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO))
							.getText().replaceAll("\\D+", "")));
		} else {
			aux.setCantReproducciones(null);
		}
		/**
		 * CANTIDAD DE SHARES
		 */
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)) {
			aux.setCantShare(
					Integer.parseInt(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE))
							.getText().replaceAll("\\D+", "")));
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
				aux.setTimeStamp(Long.parseLong(publicationsElements.get(i)
						.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));

				/**
				 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO...
				 * SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_TITLE)) {
					// puede ser que una publicación no tenga título y puede ser que tenga un link
					// de "ver más", al cual hacerle click.
					this.clickViewMoreTextContent(publicationsElements.get(i),
							FacebookConfig.XPATH_PUBLICATION_TITLE_VER_MAS);
					aux.setTitulo(publicationsElements.get(i)
							.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TITLE)).getText());
				} else {
					aux.setTitulo(null);
				}

				/**
				 * OWNER La pubicación siempre tiene un OWNER.
				 */
				aux.setOwner(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER))
						.getText());// .getAttribute("aria-label"));
				/**
				 * DATETIME Tener en cuenta que es GMT+4, porque es el del usuario. (controlar
				 * cuando la cuenta a scrapear sea de otro país, qué muestra? la del usuario que
				 * consulta o la del owner de la cuenta?.) TODO Si son posts, anteriores al día
				 * de la fecha, el formato del String cambia a: martes, 6 de marzo de 2018 a las
				 * 6:59
				 */
				String d = (publicationsElements.get(i)
						.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP))).getAttribute("title");
				/*
				 * SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
				 * try { Date date = simpleDateFormat.parse(d);
				 */
				aux.setDateTime(d);
				/*
				 * } catch (ParseException ex) { System.out.println("Exception " + ex); }
				 */
				/**
				 * CANTIDAD DE REPRODUCCIONES
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)) {
					aux.setCantReproducciones(Integer.parseInt(publicationsElements.get(i)
							.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)).getText()
							.replaceAll("\\D+", "")));
				} else {
					aux.setCantReproducciones(null);
				}
				/**
				 * CANTIDAD DE SHARES
				 */
				if (this.existElement(publicationsElements.get(i), FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)) {
					aux.setCantShare(Integer.parseInt(publicationsElements.get(i)
							.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).getText()
							.replaceAll("\\D+", "")));
				} else {
					aux.setCantShare(0);
				}

				// Lo almaceno en un array.
				System.out.println("CAPTURADOS_ " + aux.toString());
				publicationsImpl.add(aux);

			}
			// this.printPublications(publicationsImpl);
		} else {
			System.out.println("[ERROR] No se encontraron las publicaciones.");
		}

		// Si momento 0 al cargar la página no hay publicaciones, entonces busco el
		// botón más:
		/*
		 * while(publicationsElements.size()==0) {
		 * 
		 * if(this.getDriver().findElements(By.xpath(FacebookConfig.
		 * XPATH_PPAL_BUTTON_SHOW_MORE)).size()==1) { System.out.println("Show more");
		 * WebDriverWait wait = new WebDriverWait(this.getDriver(), 10); WebElement
		 * element =
		 * wait.until(ExpectedConditions.elementToBeClickable(By.xpath(FacebookConfig.
		 * XPATH_PPAL_BUTTON_SHOW_MORE))); System.out.println("CLICK!");
		 * element.click(); } if(this.getDriver().findElements(By.xpath(FacebookConfig.
		 * XPATH_PUBLICATIONS_CONTAINER)).size()>0) { publicationsElements =
		 * this.getDriver().findElements(By.xpath(FacebookConfig.
		 * XPATH_PUBLICATIONS_CONTAINER)); }
		 * 
		 * }
		 */
		/*
		 * publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig.
		 * XPATH_PUBLICATIONS_CONTAINER));
		 * 
		 * File scrFile2 =
		 * ((TakesScreenshot)this.getDriver()).getScreenshotAs(OutputType.FILE);
		 * 
		 * try { FileUtils.copyFile(scrFile2, new File("c:\\tmp\\screenshot8887.png"));
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		/*
		 * System.out.println(publications.get(i).getText()); //La publicacion tiene
		 * para ver más comentarios?
		 * //this.loadAllPublicationComments(publications.get(i));
		 * 
		 * 
		 * //Por ahora solo me fijo 1 vez si tiene el boton de VER MAS COMENTARIOS try {
		 * publications.get(i).findElement(By.xpath("//a[@class='UFIPagerLink']")).click
		 * (); this.getDriver().manage().timeouts().implicitlyWait(10,
		 * TimeUnit.SECONDS); } catch (NoSuchElementException e) {
		 * System.out.println("Element Not Found");
		 * 
		 * }
		 * 
		 * List<WebElement> comments =
		 * publications.get(i).findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		 * this.obtainPublicationComments(comments);
		 * System.out.println(" ==============="+i+" FIN================= ");
		 */
		// this.obtainPublicationComments(publications.get(i));
		/*
		 * ESTE ES EL FORMATO DE EXTRACCIÓN: Mauricio Macri 17 h · CON BOLSAS DE COMIDA
		 * PARA PERRO FABRICA MOCHILAS También usa carteles de la vía pública para
		 * fabricar bolsos, cartucheras y fundas de skate y surf, mientras les enseña un
		 * oficio a vecinos de Melchor Romero. Hoy recibí a Iván en Olivos. Swahili
		 * fundas 320.202 reproducciones
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
	 * Es el 'more text' que puede aparecer en el titulo de una publicación cuando
	 * es muy larga...
	 * 
	 * @param element
	 * @param xpathExpression
	 */
	private void clickViewMoreTextContent(WebElement element, String xpathExpression) {
		boolean verMasClicked = false;
		while (this.existElement(element, xpathExpression) && (!verMasClicked)) {
			try {
				WebElement we = element.findElement(By.xpath(xpathExpression));
				// ((JavascriptExecutor)
				// this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", we);
				((JavascriptExecutor) this.getDriver())
						.executeScript("arguments[0].scrollIntoView({block: \"start\"});", element);
				// this.moveTo(we);
				if (we.isDisplayed()) {
					we.click();
					verMasClicked = true;
				} else {
					System.out.println("[ERROR] VER MAS TITLE NOT DISPLAYED");

				}
			} catch (Exception w) {
				this.saveScreenShot("Err_ViewMoreText");
				break;
			}
		}

	}

	public void printPage(Page page) {
		System.out.println(":::::::::::::::::::::PAGE LIKES: " + page.getLikes());
		System.out.println(":::::::::::::::::::::PAGE FOLLOWERS: " + page.getFollowers());
		if (page.getPublications() != null) {
			System.out.println("SE ENCONTRARON UN TOTAL DE " + page.getPublications().size() + "PUBLICACIONES");
			for (int j = 0; j < page.getPublications().size(); j++) {
				System.out.println("============== PUBLICATION " + (j + 1) + " INICIO	===============");
				System.out.println(page.getPublications().get(j).toString());
				System.out.println("************** PUBLICATION " + (j + 1) + " FIN	***************");
			}
		} else {
			System.out.println("[INFO] PrintPage():LA LISTA DE PUBLICACIONES PARA IMPRIMIR ESTÁ VACÍA.");
		}
	}

	private void saveScreenShot(String name) {
		Path path = Paths.get("", "screenshots", name);
		File scrFile = ((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(path.toString() + System.currentTimeMillis() + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean navigateTo(String URL) {
		this.getDriver().navigate().to(URL);

		// Si por algún motivo se carga una URL que no existe, ej:
		// https://www.facebook2342.com/
		if (this.existElement(null, "//body[@class='neterror']")) {
			System.out.println("[ERROR] NET ERROR ACCESS: " + this.getDriver()
					.findElement(By.xpath("//body[@class='neterror']//div[@id='main-message']")).getText());
			this.saveScreenShot("NET_ERROR_ACCESS");
			return false;
		}

		if (this.existElement(null, "//div[contains(@id,'globalContainer')]//a[contains(@href,'ref=404')]")) {
			/**
			 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente,
			 * ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o
			 * inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost
			 * inexistente) - id post válido, pero URL inválida
			 * https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
			 */
			System.out.println("[ERROR] NO EXISTE LINK " + URL + ": "
					+ this.getDriver().findElement(By.xpath("//div[contains(@id,'globalContainer')]//h2")).getText());
			this.saveScreenShot("NO_EXISTE_LINK");
			return false;
		}

		return true;
	}

	public static void main(String args[]) {
		try {
			String a = "Thursday, March 15, 2018";
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");
			Date date = sdf.parse(a);
			System.out.println(date);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	public boolean waitForPageLoaded() {
		
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				String status = ((JavascriptExecutor) driver).executeScript("return document.readyState").toString();
				if (status.equals("complete") || status.equals("interactive")) {
					System.out.println("Estado pagina: " + status);
					return true;
				} else {
					System.out.println("Estado pagina: " + status);
					return false;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(30))
				.pollingEvery(Duration.ofSeconds(1));

		return wait.until(jsLoad);
		
	}
	
	public boolean waitForPublicationsLoaded() {
		ExpectedCondition<Boolean> pubsLoaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
					return true;
				}else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(30))
				.pollingEvery(Duration.ofSeconds(1));
		
		return wait.until(pubsLoaded);
	}
	
	
	

	// "opt=1: Comentarios Relevantes" --> Es el filtro por default que tienen todos
	// los posts de Facebook.
	// "opt=2: Más Recientes" --> Cuando se actualice un post
	// "opt=3: Comentarios Relevantes(no filtrados)" --> TODOS los comentarios
	private void TipoCargaComentarios(WebElement Post, int option) {
		try {
			// Puede que se abra automáticamente el Enviar mensajes al dueño de la página.
			if (this.getAccess() != null) {
				if (this.getDriver().findElements(By.xpath("//a[@class='_3olu _3olv close button _4vu4']"))
						.size() > 0) {
					this.getDriver().findElement(By.xpath("//a[@class='_3olu _3olv close button _4vu4']")).click();
				}
			}
			
			if (this.getAccess() == null) {
				this.checkAndClosePopupLogin();
			}
			
			//if (this.waitForJStoLoad()) {
				try {
					this.getDriver().findElement(By.xpath(
							"//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li["
									+ option + "]"))
							.click();
				} catch (Exception e1) {
					if (e1.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
						// this.getActions().sendKeys(Keys.SPACE);
						JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
						jsx.executeScript("window.scrollBy(0,500)", "");
						this.saveScreenShot("SCROLL_TIPOCARGA");
						Post.findElement(By
								.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']"))
								.click();
						this.getDriver().findElement(By.xpath(
								"//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li["
										+ option + "]"))
								.click();
					}
				}
				this.waitForJStoLoad();

				//this.saveScreenShot("AFTER_OPT_VERCOMENTARIOS");
				
			//} else {

			//}
		} catch (Exception e) {
			System.out.println("[ERROR] NO SE PUDO HACER EL CLICK EN MOSTRAR TODOS LOS MENSAJES, SIN ORDENAMIENTO");
			this.saveScreenShot("ERR_NO_SELECCIONO_MOSTRAR_MENSAJES");
			e.printStackTrace();
		}

	}

	public String regexPostID(String link) {
		// www.facebook.com/teamisurus/photos/a.413505532007856.104138.401416556550087/2144570302234695/?type=3
		// www.facebook.com/teamisurus/posts/2143052825719776
		String[] stringArray = link.split("/");
		Pattern pat = Pattern.compile("[0-9]{16,18}");
		for (int i = 0; i < stringArray.length; i++) {
			Matcher mat = pat.matcher(stringArray[i]);
			if (mat.matches()) {
				// System.out.println("[INFO] Post ID: " + stringArray[i]);
				return stringArray[i];
			}
		}
		return "";
	}

	public Page page() {
		return page;
	}

}
