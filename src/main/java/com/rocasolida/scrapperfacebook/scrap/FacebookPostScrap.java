package com.rocasolida.scrapperfacebook.scrap;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Comment;
import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookPostType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookPostScrap extends Scrap {

	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	private static Integer WAIT_UNTIL_SECONDS = 10;
	private static Integer WAIT_UNTIL_SPINNER = 10;
	private static Integer MAX_COMMENTS_PER_POST = 200;
	private static final Pattern ptURLPostTypePhotos = Pattern.compile("^.*\\/photos\\/.*$");
	private static final Pattern ptURLPostTypeVideos = Pattern.compile("^.*\\/videos\\/.*$");

	public FacebookPostScrap(Driver driver, boolean debug) throws MalformedURLException {
		super(driver, debug);
	}

	public Page scrapePage(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs, Integer cantPosts) {
		long tardo = System.currentTimeMillis();
		Page page = null;
		try {
			page = new Page();
			page.setName(facebookPage);
			// vas a la pagina de face
			List<Publication> publications = null;
			if (this.navigateTo(FacebookConfig.URL + facebookPage)) { // SI NO TIRA ERROR DE CONEXIÓN O DE PAGINA INEXISTENTE...
				// obtenes informacion de la pagina
				this.updatePageLikes(page);
				if (debug)
					System.out.println("[INFO] SE CARGÓ EL LINK: " + FacebookConfig.URL + facebookPage);
				String linkType = this.facebookLinkType(); // POR AHORA CHEQUEA SI ES PAGINA O PERFIL
				switch (linkType) {
				case "PROFILE":
					if (debug)
						System.out.println("[INFO] Es un Perfil.");
					return null;
				case "PAGE":
					if (debug)
						System.out.println("[INFO] Es una Página.");
					publications = this.processPagePosts(facebookPage, uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, cantPosts);
					break;
				default:
					if (debug)
						System.out.println("[WARNING] No se reconoce el tipo de página para hacer SCRAP");
					return null;
				}
			}
			page.setPublications(publications);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainPageInformation tardo: " + tardo);
		}
		return page;
	}

	private boolean navigateTo(String URL) {
		long aux = System.currentTimeMillis();
		try {
			this.getDriver().navigate().to(URL);
			// Si por algún motivo se carga una URL que no existe, ej:
			// https://www.facebook2342.com/
			if (this.existElement(null, "//body[@class='neterror']")) {
				if (debug) {
					System.out.println("[ERROR] NET ERROR ACCESS: " + this.getDriver().findElement(By.xpath("//body[@class='neterror']//div[@id='main-message']")).getText());
					this.saveScreenShot("NET_ERROR_ACCESS");
				}
				return false;
			}
			if (this.existElement(null, "//div[contains(@id,'globalContainer')]//a[contains(@href,'ref=404')]")) {
				/**
				 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente, ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost inexistente) - id post válido, pero URL inválida https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
				 */
				if (debug) {
					System.out.println("[ERROR] NO EXISTE LINK " + URL + ": " + this.getDriver().findElement(By.xpath("//div[contains(@id,'globalContainer')]//h2")).getText());
					this.saveScreenShot("NO_EXISTE_LINK");
				}
				return false;
			}
			return true;
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("navigateTo tardo: " + aux);
		}
	}

	private boolean existElement(WebElement element, String xpathExpression) {
		long aux = System.currentTimeMillis();
		try {
			if (element == null)
				return ((this.getDriver().findElements(By.xpath(xpathExpression))).size() > 0);
			else
				return ((element.findElements(By.xpath(xpathExpression))).size() > 0);
		} finally {
			aux = System.currentTimeMillis() - aux;
			// System.out.println("existElement: " + aux);
		}
	}

	private void saveScreenShot(String name) {
		try {
			if (debug) {
				Path path = Paths.get("", "screenshots", name);
				File scrFile = ((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(scrFile, new File(path.toString() + System.currentTimeMillis() + ".png"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePageLikes(Page page) {
		long aux = System.currentTimeMillis();
		try {
			this.moveTo(this.getDriver().findElement(By.xpath("//div[@class='_4-u2 _6590 _3xaf _4-u8']")));
			List<WebElement> likesAndFollowers = this.getDriver().findElements(By.xpath("//div[@class='_4-u2 _6590 _3xaf _4-u8']/div[@class='_2pi9 _2pi2']"));
			if (likesAndFollowers != null && likesAndFollowers.size() == 2) {
				Long likeCount = extractNumberFromString(likesAndFollowers.get(0).getText());
				if (likeCount != null) {
					page.setLikes(likeCount);
				} else {
					// Sent to slack if error
					throw new Exception("Cannot extract like count");
				}
				Long followersCount = extractNumberFromString(likesAndFollowers.get(1).getText());
				if (followersCount != null) {
					page.setFollowers(followersCount);
				} else {
					// Sent to slack if error
					throw new Exception("Cannot extract followers count");
				}
			} else {
				// Sent to slack if error
				throw new Exception("Cannot extract likesAndFollowers from //div[@class='_4-u2 _6590 _3xaf _4-u8']/div[@class='_2pi9 _2pi2']");
			}
		} catch (Exception e) {
			// Sent to slack if error
			if (debug) {
				System.out.println("[ERROR] AL OBTENER LIKES AND FOLLOWERS!.");
				this.saveScreenShot("ERROR_LIKES_FOLLOWERS");
			}
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("updatePageLikes tardo: " + aux);
		}
	}

	private void moveTo(WebElement element) {
		if (this.getAccess() == null) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({block: \"start\"});", element);
		} else {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", element);
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

	private String facebookLinkType() {
		// Primero. Determinar si es una PAGINA o un PERFIL.
		// Por lo visto: Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES
		// UN PERFIL
		// XPATH:
		// //div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a
		// getAttribute('data-tab-key') == 'timeline' (Biografía)
		// getAttribute('data-tab-key') == 'friends'
		// ASUMO QUE SI (Encuentro: INICIO || PUBLICACIONES || COMUNIDAD EN MENU DE LA
		// IZQ) ES UNA PÁGINA.
		/**
		 * TODO
		 * Agregar el tipo de post LINK. en el data-lynx-uri -> aparece el valor l.facebook.com/l.php
		 * 
		 * Agregar el tipo de post LIVE VIDEO:
		 * //div[@class='_5pcr userContentWrapper']/descendant::div[@class='_6a _6b']/following-sibling::div/h5/span/span
		 * SI DA VACIO, es Video común.
		 * SI DA TEXTO, es una transmisión en vivo 
		 * queda pendiente que nos digan que es el tipo de post STATUS. 07/05/2019
		 */
		try {
			if (this.existElement(null, "//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts' or @data-key='tab_community' or @data-key='tab_home']//descendant::a")) {
				return "PAGE";
			}
			// Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
			if (this.existElement(null, "//div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a")) {
				return "PROFILE";
			}
		} catch (Exception e) {
			System.out.println("[ERROR] AL COMPROBAR TIPO DE LINK (PAGINA | PERFIL)");
			this.saveScreenShot("ERR_COMPR_LINK");
		}
		return "";
	}

	private List<Publication> processPagePosts(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs, Integer cantPosts) {
		List<Publication> publicationsImpl = new ArrayList<Publication>();
		Publication pub;
		try {
			long aux = System.currentTimeMillis();
			// this.overlayHandler();
			// aux = System.currentTimeMillis() - aux;
			// System.out.println("overlayHandler: " + aux);
			aux = System.currentTimeMillis();
			goToPublicationsSection();
			aux = System.currentTimeMillis() - aux;
			System.out.println("goToPublicationsSection: " + aux);
			aux = System.currentTimeMillis();
			waitUntilNotSpinnerLoadingAndWaitForPublicationsLoaded();
			aux = System.currentTimeMillis() - aux;
			System.out.println("tiempo de carga de publicaciones: " + aux);
			checkAndClosePopupLogin();
			aux = System.currentTimeMillis();
			if (debug)
				System.out.println("[INFO] BUSCANDO PUBLICACIONES ENTRE EL RANGO DE FECHAS DADA....");
			if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
				int lastPostSize = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size();
				int prevPostSize = 0;
				int retriesMax = 3;
				int retriesCount = 0;
				do {
					saveScreenShot("a1-" + System.currentTimeMillis());
					waitUntilShowMorePubsAppears(this);
					if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
						scrollMainPublicationsPage();
						checkAndClosePopupLogin();
						try {
							if (debug) {
								System.out.println("[INFO] SPINNER ACTIVE?...");
								this.saveScreenShot("SPINNER ACTIVE");
							}
							waitUntilNotSpinnerLoading();
						} catch (Exception e1) {
							if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
								if (debug)
									System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
							}
						}
					} else {
						if (debug) {
							this.saveScreenShot("posts");
							System.out.println("[INFO] YA SE RECORRIERON TODAS LAS PUBLICACIONES DE LA PÁGINA. NO SE ENCONTRÓ BTN SHOW MORE: " + FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
						}
						break;
					}
					if (debug)
						System.out.print("...|");
					// Obtengo posts
					List<WebElement> publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN) + "//ancestor::div[contains(@class,'userContentWrapper') and not(contains(@style,'hidden'))]"));
					//recorro los posts encontrados
					for (int i = 0; i < publicationsElements.size(); i++) {
						if (this.waitForJStoLoad()) {
							this.moveTo(publicationsElements.get(i));
							//extraigo datos de la publicacion
							pub = this.extractPublicationData(facebookPage, publicationsElements.get(i));
							if(debug) {
								System.out.println(pub);
								System.out.println("*********************************************************************************************");
							}
							//Extrae los comentarios...
							/*
							pub = obtainPostTypeOtherInformation(facebookPage, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub, publicationsElements.get(i));
							System.out.println(pub);
							*/
							publicationsImpl.add(pub);
							//oculto la publicacion
							((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", publicationsElements.get(i));
							// return publicationsImpl;
						} else {
							System.out.println("[ERROR] PROBLEMAS AL EXTRAER DATOS DEL POST.");
							this.saveScreenShot("PROBLEMA_EXTRAER_DATOSPOST");
						}
					}

					prevPostSize = lastPostSize;
					lastPostSize = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size();
					System.out.println("last post size: " + lastPostSize);
					if (lastPostSize == prevPostSize) {
						retriesCount++;
					}
					if (cantPosts != null && publicationsImpl.size() >= cantPosts) {
						break;
					}
				} while (!((this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookPage, uTIME_INI))).size()) > 0) && retriesCount < retriesMax);
				if (debug)
					System.out.println("|FIN|");
			} else {
				if (debug) {
					System.err.println("[INFO] LA PAGINA NO TIENE NUNGUNA PUBLICACION o NO TUVO EL TIEMPO PARA CARGARSE");
					this.saveScreenShot("PAGINA_SIN_PUBS");
				}
				return null;
			}
			aux = System.currentTimeMillis() - aux;
			System.out.println("tiempo de extraccion de posts: " + aux);
			aux = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
			if (debug) {
				System.err.println("[ERROR] EN LA CARGA DE PUBLICACIONES.");
				this.saveScreenShot("ERR_CARGA_PUBS");
			}
		}
		// RETORNO SOLO LAS PUBLICACIONES QUE CUMPLIERON CON EL FILTRO.
		return publicationsImpl;
	}

	private void waitUntilNotSpinnerLoadingAndWaitForPublicationsLoaded() throws Exception {
		try {
			try {
				if (debug)
					System.out.println("[INFO] SPINNER ACTIVE?...");
				this.waitUntilNotSpinnerLoading();
			} catch (Exception e1) {
				if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug)
						System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
				}
			}
			this.waitForPublicationsLoaded(this);
		} catch (Exception e) {
			if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
				if (debug) {
					System.out.println("[WARN]Tiempo espera carga publicaciones agotado");
					this.saveScreenShot("ERR_ESPERA_CARGA_PUBS");
				}
				throw e;
			} else {
				throw e;
			}
		}
	}

	private boolean overlayHandler() {
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
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(5)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(overlayClosed);
	}

	private void goToPublicationsSection() throws Exception {
		try {
			this.waitUntilPublicationsMenuOption();
			if (this.getAccess() == null) {
				try {
					String posts = this.getDriver().findElement(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")).getAttribute("href");
					// ACcedo por la URL, puede que el elemento nunca me quede visible.
					this.navigateTo(posts);
				} catch (Exception e) {
					if (debug)
						System.err.println("[ERROR] ACCEDIENDO A LA URL DEL POST");
					throw e;
				}
			} else {
				try {
					// Accedo por el click en el menú opción "Publicaciones"
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(true);", this.getDriver().findElement(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")));
					this.getDriver().findElement(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")).click();
				} catch (Exception e) {
					if (debug) {
						System.err.println("[ERROR] NO SE PUDO ACCEDER AL MENÚ 'PUBLICACIONES'");
						this.saveScreenShot("ERR_ACCEDER_PUBLICACIONES");
					}
					throw e;
				}

			}
		} catch (Exception e) {
			System.err.println("No tiene opción de menu 'publicaciones'.");
		}
	}

	private boolean waitUntilPublicationsMenuOption() {
		ExpectedCondition<Boolean> commentLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")).size() > 0) {
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200)).ignoring(TimeoutException.class);
		return wait.until(commentLink);
	}
 
	private boolean waitUntilNotSpinnerLoading() {
		ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//span[@role='progressbar']")).size() > 0 && driver.findElement(By.xpath("//span[@role='progressbar']")).isDisplayed()) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SPINNER)).pollingEvery(Duration.ofMillis(200)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(morePubsLink);
	}

	private boolean waitForPublicationsLoaded(final FacebookPostScrap fs) {
		ExpectedCondition<Boolean> pubsLoaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if ((driver.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) && (driver.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER + "[1]")).isDisplayed()) && (fs.waitForJStoLoad())) {
					if (debug) {
						System.out.println("Container publications TRUE");
						fs.saveScreenShot("container_pub_true");
					}
					return true;
				} else {
					if (debug)
						System.out.println("Container publications FALSE");
					fs.scrollMainPublicationsPage();
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200)).ignoring(StaleElementReferenceException.class).ignoring(NoSuchElementException.class);
		return wait.until(pubsLoaded);
	}

	private boolean waitForJStoLoad() {
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (((JavascriptExecutor) driver).executeScript("return document.readyState").toString().equals("complete")) {
					return true;
				} else {
					if (debug)
						System.out.println("[INFO] DocumentReadyState UNCOMPLETE");
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200)).ignoring(NoSuchElementException.class);
		return wait.until(jsLoad);
	}

	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		// Varilla agrego este delay para que Facebook no lo bloquee por concurrencia
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
	}

	private boolean waitUntilShowMorePubsAppears(final FacebookPostScrap fs) {
		boolean result = false;
		try {
			ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					if (fs.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE)) {
						// System.out.println("true");
						return true;
					} else {
						fs.scrollMainPublicationsPage();
						return false;
					}
				}
			};
			Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));
			result = wait.until(morePubsLink);
		} catch (Exception e) {
			if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
				if (debug)
					System.out.println("[WARN] TimeoutException. Waiting ShowmorePublications button");
			} else {
				e.printStackTrace();
				throw e;
			}
		}
		return result;
	}

	private void checkAndClosePopupLogin() {
		//////////////////////////////////////////////////////////////////////////////////
		// NOTA: Varilla el dia 14/11 comento esta logica y puso la del hide a ver que tul
		//////////////////////////////////////////////////////////////////////////////////
		// if ((this.existElement(null, "//a[@id='expanding_cta_close_button']") && this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).isDisplayed())) {
		// try {
		// this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
		// if (debug)
		// System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
		// } catch (Exception e) {
		// if (e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
		// this.scrollDown();
		// this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
		// if (debug)
		// System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
		// } else {
		// if (debug)
		// this.saveScreenShot("ERRCLOSEPOPUPLGIN");
		// e.printStackTrace();
		// }
		// }
		// }
		ocultarBannersLogin();
	}

	private void ocultarBannersLogin() {
		try {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='_67m7']")));
		} catch (Exception ex) {
		}
		try {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='_62uh']")));
		} catch (Exception ex) {
		}
		try {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='_5hn6']")));
		} catch (Exception ex) {
		}
		try {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='generic_dialog pop_dialog generic_dialog_modal']")));
		} catch (Exception ex) {
		}
		try {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='_4-u2 _hoc clearfix _4-u8']")));
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'display:none!important')", this.getDriver().findElement(By.xpath("//div[@class='_3d9q fixed_elem']")));
		} catch (Exception ex) {
		}
	}

	private Publication extractPublicationData(String pageName, WebElement publication) {
		long tardo = System.currentTimeMillis();
		try {
			Publication aux = new Publication();

			/**
			 * Extraigo LINK del post, que es su ID.
			 */
			String postID = this.regexPostID(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
			if (postID == "") {
				if (debug)
					System.out.println("[INFO] ERROR AL ENCONTRAR EL ID DEL POST: " + publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
			} else {
				aux.setId(postID);
				aux.setUrl(FacebookConfig.URL + postID);
			}

			/**
			 * TIMESTAMP El timestamp viene en GMT.
			 */

			/*
			 * Hay dos casos (necesito saber el abbr que contiene un timestamp, sino se confunde cuando comparten un post de otra cuenta de facebook): <abbr data-utime='' class='timestamp'> <abbr data-utime=''><span class='timestamp'>
			 */

			if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
				aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
			} else if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) {
				aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).getAttribute("data-utime")));
			}

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
			if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_OWNER)) {
				aux.setOwner(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER)).getText());
			} else if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_PHOTO_OWNER)) {
				aux.setOwner(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_PHOTO_OWNER)).getText());
			} else {
				aux.setOwner(pageName);
			}

			/**
			 * DATETIME
			 */
			/*
			 * Usaremos siempre el UTC. if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) { aux.setDateTime((publication.findElement(By.xpath(FacebookConfig. XPATH_PUBLICATION_TIMESTAMP))).getAttribute("title")); } else if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) { aux.setDateTime((publication.findElement(By.xpath(FacebookConfig. XPATH_PUBLICATION_TIMESTAMP_1))).getAttribute("title")); }
			 */
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
			if(this.existElement(publication, FacebookConfig.XP_POST_TOTALSHARES)) {
				int totalShares = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_TOTALSHARES)).getText());
				aux.setCantShare(totalShares);
				if(debug)
					System.out.println("Total Shares: " + totalShares);						
			}
			
			/**
			 * Tipo de post (link, video, live video, photo)
			 */
			try {
				String url = publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href");
				Matcher match = this.ptURLPostTypePhotos.matcher(url);
				
				if(match.matches()) {
					//es foto
					if(debug)
						System.out.println("[POST TYPE] SET AS 'PHOTO'!");
					aux.setType(FacebookPostType.PHOTO);
				}
				
				//System.out.println("GETTEXT: " + publication.findElement(By.xpath(FacebookConfig.XP_PUBLICATION_LIVEVIDEO)));
				match = this.ptURLPostTypeVideos.matcher(url);
				if(match.matches() && aux.getType()==null) {
					//es video, falta definir si es live_video.
					if(publication.findElements(By.xpath(FacebookConfig.XP_PUBLICATION_LIVEVIDEO)).size()>0) {
						if(debug)
							System.out.println("[POST TYPE] SET AS 'LIVE VIDEO'!");
						aux.setType(FacebookPostType.LIVE_VIDEO);
					}else {
						if(debug) 
							System.out.println("[POST TYPE] SET AS 'VIDEO'!");
						aux.setType(FacebookPostType.VIDEO);
					}
				}
				
				//Agregar el tipo de post LINK. en el data-lynx-uri -> aparece el valor l.facebook.com/l.php
				if(aux.getType()==null) {
					if(this.existElement(publication, FacebookConfig.XP_PUBLICATION_LINK)) {
						if(debug)
							System.out.println("[POST TYPE] SET AS 'LINK'!");
						aux.setType(FacebookPostType.LINK);
					}else {
						System.out.println("[POST TYPE] SET AS 'OTHER'!");
						aux.setType(FacebookPostType.OTHER);
					}
				}
				
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
			
			/**
			 * Captura de reactions
			 */
			try {
				
				if(this.existElement(publication, FacebookConfig.XP_POST_TOTALREACTIONS)) {
					int totalReactions = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_TOTALREACTIONS)).getText());
					aux.setCantReactions(totalReactions);
					if(debug)
						System.out.println("Total Reacciones: " + totalReactions);
					
					if(this.existElement(publication, FacebookConfig.XP_POST_LIKES)) {
						int likes = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_LIKES)).getAttribute("aria-label"));
						aux.setCantLikes(Integer.valueOf(likes));
						if(debug)
							System.out.println("POST LIKEs: " + likes);						
					}
					if(this.existElement(publication, FacebookConfig.XP_POST_LOVES)) {
						int loves = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_LOVES)).getAttribute("aria-label"));
						aux.setCantLoves(Integer.valueOf(loves));
						if(debug)
							System.out.println("POST LOVEs: " + loves);
					}
					if(this.existElement(publication, FacebookConfig.XP_POST_WOW)) {
						int wow = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_WOW)).getAttribute("aria-label"));
						aux.setCantWows(Integer.valueOf(wow));
						if(debug)
							System.out.println("POST WOWs: " + wow);
					}
					if(this.existElement(publication, FacebookConfig.XP_POST_ANGRY)) {
						int angry = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_ANGRY)).getAttribute("aria-label"));
						aux.setCantAngries(Integer.valueOf(angry));
						if(debug)
							System.out.println("POST ANGRIEs: " + angry);
					}
					if(this.existElement(publication, FacebookConfig.XP_POST_HAHA)) {
						int haha = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_HAHA)).getAttribute("aria-label"));
						aux.setCantHahas(Integer.valueOf(haha));
						if(debug)
							System.out.println("POST HAHAs: " + haha);
					}
					if(this.existElement(publication, FacebookConfig.XP_POST_SORRY)) {
						int sorry = this.formatStringToNumber(publication.findElement(By.xpath(FacebookConfig.XP_POST_SORRY)).getAttribute("aria-label"));
						aux.setCantSads(Integer.valueOf(sorry));
						if(debug)
							System.out.println("POST SORRYs: " + sorry);
					}
					
				}else {
					if(debug)
						System.out.println("[INFO] El post no tiene reacciones.");
					
					aux.setCantReactions(0);
					aux.setCantLikes(0);
					aux.setCantLoves(0);
					aux.setCantWows(0);
					aux.setCantAngries(0);
					aux.setCantHahas(0);
					aux.setCantSads(0);
				}
				
				
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
			
			return aux;
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("extractPublicationData tardo: " + tardo);
		}
	}
	
	private int formatStringToNumber(String text) {
		int number = this.extractNumberFromString(text).intValue();
		if (text.contains("mil")) {
			if(text.contains(",")) {
				number = number * 100;
			}else {
				number = number * 1000;
			}
		}
		return number;
	}

	private String regexPostID(String link) {
		// www.facebook.com/teamisurus/photos/a.413505532007856.104138.401416556550087/2144570302234695/?type=3
		// www.facebook.com/teamisurus/posts/2143052825719776
		// https://www.facebook.com/permalink.php?story_fbid=1428319533981557&id=323063621173826
		// https://www.facebook.com/154152138076469/videos/972094692948872/
		// https://www.facebook.com/horaciorodriguezlarreta/posts/10156882613511019?__xts__%5B0%5D=68.ARBj2C-5qrcHODKpyJCUTm1TDe10fEsZMrPUhkrbkT41H42Jt2optgAlDRvZeJ2mbmyviQ-wIt3KnfMbpRG6u18nufB-fo42wL06yCvYdmyFl33YI_hi849HNgVKw3Ez6W2-kXeaeR3IRoXu7SXIzLroVH1Tawc1wyHFSTzSp-SqYAQWl2h8pR0&__tn__=-R
		String lastMatched = "";
		if (link.contains("permalink")) {
			String[] a = link.split("\\?")[1].split("&");
			for (String b : a) {
				if (b.contains("story_fbid=")) {
					return b.replace("story_fbid=", "");

				}
			}
		} else {
			if (link.contains("?")) {
				link = link.split("\\?")[0];
			}
			String[] stringArray = link.split("/");
			Pattern pat = Pattern.compile("[0-9]{15,18}");
			for (int i = 0; i < stringArray.length; i++) {
				Matcher mat = pat.matcher(stringArray[i]);
				if (mat.matches()) {
					// System.out.println("[INFO] Post ID: " + stringArray[i]);
					// return stringArray[i];
					//System.out.println("Valor macheado: " + stringArray[i]);
					lastMatched = stringArray[i];
				}
			}
		}
		return lastMatched;
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
			try {
				WebElement we = element.findElement(By.xpath(xpathExpression));
				((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({block: \"start\"});", element);
				if (we.isDisplayed()) {
					we.click();
					verMasClicked = true;
				} else {
					if (debug)
						System.out.println("[ERROR] VER MAS TITLE NOT DISPLAYED");
				}
			} catch (Exception w) {
				if (debug)
					this.saveScreenShot("Err_ViewMoreText");
				break;
			}
		}
	}

	private Publication obtainPostTypeOtherInformation(String pageName, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs, Publication pub, WebElement pubWeb) throws Exception {
		WebElement pubsNew = pubWeb;
		if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER3)) {
			if (debug)
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION " + ": " + FacebookConfig.URL + pub.getId());
			pub.setComments(this.extractPubComments(pubsNew, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs));
		} else if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER4)) {
			if (debug)
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION " + ": " + FacebookConfig.URL + pub.getId());
			pub.setComments(this.extractPubVideoLiveComments(pubsNew, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs));
		} else {
			if (debug)
				System.out.println("[WARN] LA PUBLICACION NO TIENE COMENTARIOS");
		}
		return pub;
	}

	private List<Comment> extractPubVideoLiveComments(WebElement pub, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			if (this.existElement(pub, FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")) {
				Thread.sleep(1000);
				moveTo(pub);
				if (cs != null && !cs.equals(CommentsSort.RELEVANCE)) {
					this.tipoCargaComentarios(pub, cs);
				}
				if (debug)
					System.out.println("[INFO] OBTENIENDO LOS COMENTARIOS DEL POST: ");
				return this.obtainAllPublicationVideoLiveComments(pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments);
			} else {
				if (debug) {
					System.out.println("[INFO] LA PUBLICACION NO TIENE COMENTARIOS.");
					this.saveScreenShot("INFO_PUB_SIN_COMENTARIOS");
				}
				return null;
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("extractPubComments tardo: " + tardo);
		}
	}

	private List<Comment> obtainAllPublicationVideoLiveComments(WebElement container, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			long a1 = System.currentTimeMillis();
			if (cantComments == null) {
				cantComments = MAX_COMMENTS_PER_POST;
			}
			List<Comment> comments = new ArrayList<Comment>();
			// Si existe el botón de "Ver Más mensajes"
			try {
				try {
					if (debug)
						System.out.println("[INFO] SPINNER ACTIVE?...");
					this.waitUntilNotSpinnerLoading();
				} catch (Exception e1) {
					if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
					}
				}
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug)
						System.out.println("[WARN] TIEMPO DE ESPERA APARICION BOTON SHOW MOORE COMMENTS EXCEDIDO.");
				} else {
					if (debug)
						this.saveScreenShot("SM_primeraVez_");
					throw e;
				}
			}
			// Obtengo filtro de comentarios
			String commentsFilter = "";
			String ctrlCommentsOutIniFilter = "";
			if (COMMENTS_uTIME_FIN != null) {
				commentsFilter = ".//abbr[@data-utime>" + String.valueOf(COMMENTS_uTIME_INI) + " and @data-utime<=" + String.valueOf(COMMENTS_uTIME_FIN) + "]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList']) and not(contains(@style,'hidden'))]";
				// Comentarios anteriores a la fecha inicial del filtro
				ctrlCommentsOutIniFilter = ".//abbr[@data-utime<" + String.valueOf(COMMENTS_uTIME_INI) + "]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList']) and not(contains(@style,'hidden'))]";
			} else {
				commentsFilter = ".//div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList']) and not(contains(@style,'hidden'))]";
			}
			if (debug)
				System.out.println("FILTRO XPATH-COMMENTS APLICADO: " + commentsFilter);
			a1 = System.currentTimeMillis() - a1;
			System.out.println("a1: " + a1);
			long a3 = System.currentTimeMillis();
			try {
				WebElement showMoreLink;
				// Obtengo los comentarios según el filtro de fechas...
				Thread.sleep(1000);
				List<WebElement> comentarios = container.findElements(By.xpath(commentsFilter));
				System.out.println(comentarios);
				if (comentarios.isEmpty()) {
					System.out.println("no comments");
				}
				int retries_max = 3;
				int retries = 0;
				do {
					long a2 = System.currentTimeMillis();
					Comment comment = null;
					// Si existen comentarios, los proceso.-
					if (comentarios.size() > 0) {
						for (int j = 0; j < comentarios.size(); j++) {
							long a = System.currentTimeMillis();
							comment = this.extractLiveCommentData(comentarios.get(j));
							a = System.currentTimeMillis() - a;
							System.out.println(comment + ". Tardo: " + a);
							comments.add(comment);
							if (debug)
								System.out.print(j + "|");
						}
						if (debug) {
							System.out.println(" ");
							System.out.println("[INFO] comentarios size: " + comentarios.size());
							System.out.println("[INFO] comments size: " + comments.size());
						}
						// Luego de que ya los procesé Busco TODOS los visibles que machean con el
						// filtro y los pongo hidden.
						for (int i = 0; i < comentarios.size(); i++) {
							((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", comentarios.get(i));
							// Condicion: //div[(contains(@style,"hidden"))]
						}
					} else {
						// swi no hubo comentarios, puede ser porque se generaron muchos comentarios el
						// dia de hoy...
						// lo que daría por fuera del rango filtro...
						if (!ctrlCommentsOutIniFilter.isEmpty() && container.findElements(By.xpath(ctrlCommentsOutIniFilter)).size() > 50) {
							// Quiere decir que ya se cargaron más de 50 mensajes con fecha anterior al
							// filtro
							if (debug)
								this.saveScreenShot("SALIDA_PRECOZ");
							break;
						}
					}
					if (!existElement(container, FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS)) {
						break;
					}
					showMoreLink = container.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS));
					try {
						showMoreLink.click();
					} catch (Exception e) {
						saveScreenShot("varilla test " + e.getClass().getSimpleName());
						if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							this.getActions().sendKeys(Keys.ESCAPE).perform();
							this.overlayHandler();
							this.checkAndClosePopupLogin();
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							this.scrollDown();
							this.waitUntilShowMoreCommAppears(this, container, FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
							showMoreLink.click();
						} else if (e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
							if (debug)
								System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
						} else if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {

						} else {
							e.printStackTrace();
							throw e;
						}
					}
					// Obtengo los comentarios según el filtro de fechas...
					Thread.sleep(1000);
					comentarios = container.findElements(By.xpath(commentsFilter));
					if (debug)
						System.out.println("CANT COMENTARIOS: " + comentarios.size());
					a2 = System.currentTimeMillis() - a2;
					System.out.println("a2: " + a2 + ". comentarios.size(): " + comentarios.size() + ". comments.size(): " + comments.size() + ".");
					if (comentarios.size() == 0) {
						retries++;
						if (retries >= retries_max) {
							break;
						}
					}
				} while (comments.size() < cantComments && (comentarios.size() > 0 || this.waitUntilNotSpinnerLoading()));
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug) {
						e.printStackTrace();
						this.saveScreenShot("tiemoutexception_SM");
						System.out.println("[WARN] TIEMPO DE ESPERA SHOW MORE MESSAGES LINK EXCEDIDO.");
					}
				} else if (e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
					if (debug)
						System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
				} else if (e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
					if (debug)
						System.out.println("[WARN] Desapareció el botón de ShowMore Comments. [no such element exception]");
				} else {
					e.printStackTrace();
					if (debug)
						this.saveScreenShot("exception_SM_1");
					throw e;
				}
			}
			a3 = System.currentTimeMillis() - a3;
			System.out.println("a3: " + a3);
			return comments;
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainAllPublicationComments tardo: " + tardo);
		}
	}

	private Comment extractLiveCommentData(WebElement comentario) throws Exception {
		Comment auxComment = new Comment();
		// Mensaje
		long a = System.currentTimeMillis();
		if (this.existElement(comentario, ".//a[@class='_5v47 fss']")) {
			WebElement aux;
			while (this.existElement(comentario, ".//a[@class='_5v47 fss']")) {
				aux = comentario.findElement(By.xpath(".//a[@class='_5v47 fss']"));
				try {
					this.moveTo(aux);
					aux.click();
					try {
						this.waitForMoreTextInCommentMessageLink(this, comentario);
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) { // Si la publicación no tiene ningun tipo de actividad...

						} else {
							throw e;
						}
					}
				} catch (Exception e) {
					if (debug)
						this.saveScreenShot("Error VerMasContenidoMensaje");
					throw e;
				}
			}
		}
		a = System.currentTimeMillis() - a;
		System.out.println("ver mas texto del comentario tardo: " + a);
		if (this.existElement(comentario, FacebookConfig.XPATH_USER_COMMENT)) {
			String aux = "";
			for (int i = 0; i < comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size(); i++) {
				aux += comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).get(i).getText();
			}
			auxComment.setMensaje(aux);
		} else {
			// Puede ser porque postea solo una imagen...
			auxComment.setMensaje("");
		}
		// Usuario id
		if (this.getAccess() != null) {
			String ini = "id=";
			String fin = "&";
			String pathUserID = comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT)).getAttribute("data-hovercard");
			auxComment.setUserId(pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));
		}
		// Usuario name
		auxComment.setUserName(comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT2)).getText());

		// Utime
		auxComment.setUTime(Long.valueOf(comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime")));
		try {
			// https://www.facebook.com/mauriciomacri/videos/10156570748083478/?comment_id=10156570873778478&comment_tracking=%7B%22tn%22%3A%22R9%22%7D
			String href = comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_ID)).getAttribute("href");
			String commentId = href.split("\\?")[1].split("&")[0].split("=")[1];
			auxComment.setId(commentId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Extraer likes.
		if (this.getAccess() != null) {
			auxComment.setCantLikes(Integer.valueOf(comentario.findElement(By.xpath(".//span[contains(@class,'UFICommentLikeButton')]")).getText()));
		}

		return auxComment;
	}

	private WebElement publicationCommentSectionClick() throws Exception {
		System.out.println(this.getDriver().getCurrentUrl());
		WebElement pubsNew = this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER + "[1]"));
		if (this.getAccess() == null) {
			if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)) {
				try {
					this.waitUntilCommentSectionVisible(pubsNew);
					try {
						if (debug)
							this.saveScreenShot("antesClickCommentSection_NL_1");
						pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();
						return pubsNew;
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							if (!(Boolean) ((JavascriptExecutor) this.getDriver()).executeScript("return window.innerHeight+window.scrollY>=document.body.offsetHeight;")) {
								this.scrollDown();
							} else {
								if (debug)
									System.out.println("[INFO] LA VENTANA NO TIENE MAS SCROLL!!!!!!!!!");
								if (this.existElement(null, "//a[@class='_42ft _4jy0 _3obb _4jy6 _4jy1 selected _51sy']")) {
									WebElement divButtom = this.getDriver().findElement(By.xpath("//a[@class='_42ft _4jy0 _3obb _4jy6 _4jy1 selected _51sy']//parent::div//parent::div//parent::div//parent::div//parent::div[1]"));
									((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'height:0px')", divButtom);
								}
								if (debug)
									this.saveScreenShot("SINMASSCROLL_ClickCommentSection_NL_2");
								// return null;
							}
							this.checkAndClosePopupLogin();
							if (debug)
								this.saveScreenShot("antesClickCommentSection_NL_2");
							try {
								pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();
							} catch (Exception ex) {
							}
							return pubsNew;
						}
					}

				} catch (Exception e) {
					if (!e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug) {
							System.err.println("[ERROR] ACCESO A SECCION COMENTARIOS DE LA PUBLICACIÓN");
							this.saveScreenShot("ERR_ACCESO_COMM_PUB");
						}
						throw e;
					}
				}
			}
		}
		return pubsNew;
	}

	private boolean waitUntilCommentSectionVisible(final WebElement pub) {
		ExpectedCondition<Boolean> commentSection = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed() && pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isEnabled()) {
					// System.out.println("true");
					JavascriptExecutor jsx = (JavascriptExecutor) driver;
					jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
					return true;
				} else {
					JavascriptExecutor jsx = (JavascriptExecutor) driver;
					jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));
		return wait.until(commentSection);
	}

	private void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	private void ctrlLoadPost() throws Exception {
		try {
			if (this.getAccess() == null) {
				this.waitForPublicationsLoaded(this);
				if (this.getAccess() == null) {
					try {
						if (this.waitUntilPopupLoginAppears()) {
							this.checkAndClosePopupLogin();
						}
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
							if (debug)
								System.out.println("[WARN] TIEMPO ESPERA POPUPLOGIN AGOTADO");
						} else {
							throw e;
						}
					}
				}
				if (debug)
					this.saveScreenShot("PostLoaded");
			} else {
				if (this.getDriver().getCurrentUrl().contains("video")) {
					try {
						// if(this.waitForVideoLoaded()) { //Es por spinner loading
						if (this.waitUntilNotSpinnerLoading()) {
							this.getDriver().findElement(By.xpath("//button[@data-testid='play_pause_control']")).click();
							this.getDriver().findElement(By.xpath("//a[@role='tab'][1]")).click();
						}

					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
							if (debug)
								System.out.println("[WARN] TIEMPO ESPERA POPUPLOGIN AGOTADO");
						} else {
							throw e;
						}
					}

				} else {
					try {
						this.waitForClosingPost();
						this.getDriver().findElement(By.xpath("//i[@class='img sp_Gc-AtOOGa_D sx_c3f24f'][1]")).click();
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
							if (debug)
								System.out.println("[WARN] TIEMPO ESPERA POPUPLOGIN AGOTADO");
						} else {
							throw e;
						}
					}
				}
				// Controlar que cargue la parte de comentarios si es video
				// --- //video
				// --- //div[@class="_2e7p"] --- esta es la lista de videos a la derecha
				// Pausar el video click: //button[@data-testid="play_pause_control"]
			}
		} catch (Exception e) {
			System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
			throw e;
		}
	}

	private boolean waitUntilPopupLoginAppears() {
		ExpectedCondition<Boolean> commentSection = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//a[@id='expanding_cta_close_button']")).size() > 0 && driver.findElement(By.xpath("//a[@id='expanding_cta_close_button']")).isDisplayed()) {
					return true;
				} else {
					JavascriptExecutor jsx = (JavascriptExecutor) driver;
					jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));
		return wait.until(commentSection);
	}

	private boolean waitForClosingPost() {
		ExpectedCondition<Boolean> pubsLoaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				// Si existe la lista de Videos y se muestra en pantalla...
				if (driver.findElements(By.xpath("//i[@class='img sp_Gc-AtOOGa_D sx_c3f24f'][1]")).size() > 0 && driver.findElement(By.xpath("//i[@class='img sp_Gc-AtOOGa_D sx_c3f24f'][1]")).isDisplayed()) {
					if (debug) {
						System.out.println("Container publications TRUE");
						// fs.saveScreenShot("container_pub_true");
					}
					return true;
				} else {
					if (debug)
						System.out.println("Container publications FALSE");
					// fs.scrollMainPublicationsPage();
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200)).ignoring(StaleElementReferenceException.class).ignoring(NoSuchElementException.class);
		return wait.until(pubsLoaded);
	}

	private void merge(Publication publication, Publication result) {
		if (result != null) {
			if (result.getId() != null) {
				publication.setId(result.getId());
			}
			if (result.getCantComments() != null) {
				publication.setCantComments(result.getCantComments());
			}
			if (result.getCantLikes() != null) {
				publication.setCantLikes(result.getCantLikes());
			}
			if (result.getCantReproducciones() != null) {
				publication.setCantReproducciones(result.getCantReproducciones());
			}
			if (result.getCantShare() != null) {
				publication.setCantShare(result.getCantShare());
			}
			if (result.getComments() != null) {
				publication.setComments(result.getComments());
			}
			if (result.getOwner() != null) {
				publication.setOwner(result.getOwner());
			}
			if (result.getTitulo() != null) {
				publication.setTitulo(result.getTitulo());
			}
			if (result.getUrl() != null) {
				publication.setUrl(result.getUrl());
			}
			if (result.getUTime() != null) {
				publication.setUTime(result.getUTime());
			}
		}
	}

	private void extractPublicationDataFromDivOnPublicationPage(Publication publication, WebElement pubsNew) {
		try {
			// Cargo los likes del post y la cantidad de comments
			List<WebElement> wes = pubsNew.findElements(By.xpath(".//*[contains(@class,'commentable_item')]//div[contains(@class,'_sa_')]//span"));
			wes.addAll(pubsNew.findElements(By.xpath(".//div[contains(@class,'UFIShareRow')]//span")));
			wes.addAll(pubsNew.findElements(By.xpath(".//a[contains(@class,'UFIShareLink')]")));
			if (wes != null) {
				for (WebElement we : wes) {
					String aux = we.getText().toLowerCase();
					if (aux.contains(" likes") || aux.contains(" me gusta")) {
						publication.setCantLikes(ScrapUtils.parseCount(aux));
					} else if (aux.contains(" comments") || aux.contains(" comentarios")) {
						publication.setCantComments(ScrapUtils.parseCount(aux));
					} else if (aux.contains(" share") || aux.contains(" shares") || aux.contains(" compartido")) {
						publication.setCantShare(ScrapUtils.parseCount(aux));
					} else if (aux.contains(" views")) {
						publication.setCantReproducciones(ScrapUtils.parseCount(aux));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private boolean tipoCargaComentarios(WebElement pub, CommentsSort cs) throws Exception {
		try {
			List<WebElement> elements = pub.findElements(By.xpath(".//a[@data-testid='UFI2ViewOptionsSelector/link']"));
			if (elements.size() > 0) {
				moveTo(elements.get(0));
				elements.get(0).click();
			} else if (pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).size() > 0) {
				((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).get(0));
				try {
					if (this.getDriver().findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
						(new Actions(this.getDriver())).sendKeys(Keys.ESCAPE).perform();
					}
					Thread.sleep(1000);
					this.overlayHandler();
					pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).get(0).click();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Thread.sleep(1000);
			// Espero a que cargue las opciones del menu y le hago click a la ultima opcion.
			ExpectedCondition<Boolean> pubOptionsLoad = new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver driver) {
					if (pub.findElements(By.xpath("//ul[@class='_54nf']")).size() > 0) {
						int index = 0;
						switch (cs) {
						case ALL:
							index = 2;
							break;
						case NEW:
							index = 1;
							break;
						case RELEVANCE:
							index = 0;
							break;
						}
						WebElement link = pub.findElements(By.xpath("//ul[@class='_54nf']//a[@class='_54nc']")).get(index);
						// tomo el ultimo item...
						try {
							if (pub.findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
								(new Actions(driver)).sendKeys(Keys.ESCAPE).perform();
								return false;
							}
							Thread.sleep(1000);
							
//							boolean result = false;
//						    int attempts = 0;
//						    while(attempts < 2) {
//						        try {
//						        	link.click();
						        	pub.findElements(By.xpath("//ul[@class='_54nf']//a[@class='_54nc']")).get(index).click();
//						            result = true;
//						            break;
//						        } catch(Exception e) {
//						        	System.err.println("[INFO] CLICK FALLÓ. ATTEMP 1. " + e.getClass().getSimpleName());
//						        }
//						        attempts++;
//						    }
							
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					} else {
						return false;
					}
				}
			};
			Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
			return wait.until(pubOptionsLoad);
		} catch (Exception e) {
			e.printStackTrace();
			if (debug) {
				System.err.println("[ERROR] NO SE PUDO HACER EL CLICK EN MOSTRAR TODOS LOS MENSAJES, SIN ORDENAMIENTO");
				this.saveScreenShot("ERR_NO_SELECCIONO_MOSTRAR_MENSAJES");
			}
			// e.printStackTrace();
			throw e;
		}
	}

	private boolean waitUntilMenuOptionAppears(final FacebookPostScrap fs, final WebElement post) {
		ExpectedCondition<Boolean> menuAppears = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (post.findElements(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).size() > 0 && post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).isDisplayed()) {
					// System.out.println("existe show more comments.!");
					return true;
				} else {

					// if(fs.getAccess()==null) {
					// fs.checkAndClosePopupLogin();
					// }
					// System.out.println("no existe show more comments.!");
					return false;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200)).ignoring(NoSuchElementException.class);

		return wait.until(menuAppears);
	}

	private boolean waitUntilMenuAppears() {
		ExpectedCondition<Boolean> menuAppears = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li")).size() > 0 && driver.findElement(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li")).isDisplayed()) {
					// System.out.println("existe show more comments.!");
					return true;
				} else {
					// if(fs.getAccess()==null) {
					// fs.checkAndClosePopupLogin();
					// }
					// System.out.println("no existe show more comments.!");
					JavascriptExecutor jsx = (JavascriptExecutor) driver;
					jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
					return false;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));
		// .ignoring(StaleElementReferenceException.class);

		return wait.until(menuAppears);
	}

	private List<Comment> extractPubComments(WebElement pub, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			if (this.existElement(pub, FacebookConfig.XPATH_COMMENTS_CONTAINER2 + "//*")) {
				Thread.sleep(1000);
				moveTo(pub);
				if (cs != null && !cs.equals(CommentsSort.RELEVANCE)) {
					this.tipoCargaComentarios(pub, cs);
				}
				if (debug)
					System.out.println("[INFO] OBTENIENDO LOS COMENTARIOS DEL POST: ");
				return this.obtainAllPublicationComments(pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER2)), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments);
			} else {
				if (debug) {
					System.out.println("[INFO] LA PUBLICACION NO TIENE COMENTARIOS.");
					this.saveScreenShot("INFO_PUB_SIN_COMENTARIOS");
				}
				return null;
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("extractPubComments tardo: " + tardo);
		}
	}

	/**
	 * Si existe el botón de show more, entonces lo clickea, hasta que se cargaron todos los mensajes para luego obtenerlos con un XPATH query y extraerle los datos. Me servirá para las replies y para los comentarios.
	 * 
	 * @param cantComments
	 * @param cs
	 */
	private List<Comment> obtainAllPublicationComments(WebElement container, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			long a1 = System.currentTimeMillis();
			if (cantComments == null) {
				cantComments = MAX_COMMENTS_PER_POST;
			}
			List<Comment> comments = new ArrayList<Comment>();
			// if (container.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			if (this.existElement(container, "//a[@data-testid='UFI2CommentsCount/root']")) {
				if (debug)
					// System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
					System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container.findElement(By.xpath("//a[@data-testid='UFI2CommentsCount/root']")).getText());
			}
			if (this.existElement(container, "//a[@data-testid='UFI2SharesCount/root']")) {
				if (debug)
					// System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
					System.out.println("COMPARTIDOS QUE SE INDICA EN EL POST:" + container.findElement(By.xpath("//a[@data-testid='UFI2SharesCount/root']")).getText());
			}
			// Si existe el botón de "Ver Más mensajes"
			try {
				try {
					if (debug)
						System.out.println("[INFO] SPINNER ACTIVE?...");
					this.waitUntilNotSpinnerLoading();
				} catch (Exception e1) {
					if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
					}
				}
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug)
						System.out.println("[WARN] TIEMPO DE ESPERA APARICION BOTON SHOW MOORE COMMENTS EXCEDIDO.");
				} else {
					if (debug)
						this.saveScreenShot("SM_primeraVez_");
					throw e;
				}
			}
			// Obtengo filtro de comentarios
			String commentsFilter = "";
			String ctrlCommentsOutIniFilter = "";
			if (COMMENTS_uTIME_FIN != null) {
				commentsFilter = ".//abbr[@data-utime>" + String.valueOf(COMMENTS_uTIME_INI) + " and @data-utime<=" + String.valueOf(COMMENTS_uTIME_FIN) + "]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList']) and not(contains(@style,'hidden'))]";
				// Comentarios anteriores a la fecha inicial del filtro
				ctrlCommentsOutIniFilter = ".//abbr[@data-utime<" + String.valueOf(COMMENTS_uTIME_INI) + "]//ancestor::div[contains(@class,'UFICommentContentBlock') and not(ancestor::div[@class=' UFIReplyList']) and not(contains(@style,'hidden'))]";
			} else {
				commentsFilter = ".//div[contains(@data-testid,'UFI2Comment/root_depth_0') and not(contains(@style,'hidden'))]";
			}
			if (debug)
				System.out.println("FILTRO XPATH-COMMENTS APLICADO: " + commentsFilter);
			a1 = System.currentTimeMillis() - a1;
			System.out.println("a1: " + a1);
			long a3 = System.currentTimeMillis();
			int retries_max = 3;
			int retries = 0;
			try {
				WebElement showMoreLink;
				// Obtengo los comentarios según el filtro de fechas...
				Thread.sleep(1000);
				List<WebElement> comentarios = container.findElements(By.xpath(commentsFilter));
				System.out.println(comentarios);
				System.out.println(comentarios);
				if (comentarios.isEmpty()) {
					System.out.println("no comments");
				}
				do {
					long a2 = System.currentTimeMillis();
					Comment comment = null;
					// Si existen comentarios, los proceso.-
					if (comentarios.size() > 0) {
						for (int j = 0; j < comentarios.size(); j++) {
							long a = System.currentTimeMillis();
							comment = this.extractCommentData(comentarios.get(j));
							a = System.currentTimeMillis() - a;
							System.out.println(comment + ". Tardo: " + a);
							comments.add(comment);
							if (debug)
								System.out.print(j + "|");
						}
						if (debug) {
							System.out.println(" ");
							System.out.println("[INFO] comentarios size: " + comentarios.size());
							System.out.println("[INFO] comments size: " + comments.size());
						}
						// Luego de que ya los procesé Busco TODOS los visibles que machean con el
						// filtro y los pongo hidden.
						for (int i = 0; i < comentarios.size(); i++) {
							((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", comentarios.get(i));
							// Condicion: //div[(contains(@style,"hidden"))]
						}
					} else {
						// swi no hubo comentarios, puede ser porque se generaron muchos comentarios el
						// dia de hoy...
						// lo que daría por fuera del rango filtro...
						if (!ctrlCommentsOutIniFilter.isEmpty() && container.findElements(By.xpath(ctrlCommentsOutIniFilter)).size() > 50) {
							// Quiere decir que ya se cargaron más de 50 mensajes con fecha anterior al
							// filtro
							if (debug)
								this.saveScreenShot("SALIDA_PRECOZ");
							break;
						}
					}
					if (!existElement(container, FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS2)) {
						break;
					}
					showMoreLink = container.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS2));
					try {
						showMoreLink.click();
					} catch (Exception e) {
						saveScreenShot("varilla test " + e.getClass().getSimpleName());
						if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							this.getActions().sendKeys(Keys.ESCAPE).perform();
							this.overlayHandler();
							this.checkAndClosePopupLogin();
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							this.scrollDown();
							this.waitUntilShowMoreCommAppears(this, container, FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS2);
							showMoreLink.click();
						} else if (e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
							if (debug)
								System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
						} else if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {

						} else {
							e.printStackTrace();
							throw e;
						}
					}
					// Obtengo los comentarios según el filtro de fechas...
					Thread.sleep(3000);
					comentarios = container.findElements(By.xpath(commentsFilter));
					if (debug)
						System.out.println("CANT COMENTARIOS: " + comentarios.size());
					a2 = System.currentTimeMillis() - a2;
					System.out.println("a2: " + a2 + ". comentarios.size(): " + comentarios.size() + ". comments.size(): " + comments.size() + ".");
					if (comentarios.size() == 0) {
						retries++;
						if (retries >= retries_max) {
							break;
						}
					}
				} while (comments.size() < cantComments && (comentarios.size() > 0 || this.waitUntilNotSpinnerLoading()));
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug) {
						e.printStackTrace();
						this.saveScreenShot("tiemoutexception_SM");
						System.out.println("[WARN] TIEMPO DE ESPERA SHOW MORE MESSAGES LINK EXCEDIDO.");
					}
				} else if (e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
					if (debug)
						System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
				} else if (e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
					if (debug)
						System.out.println("[WARN] Desapareció el botón de ShowMore Comments. [no such element exception]");
				} else {
					e.printStackTrace();
					if (debug)
						this.saveScreenShot("exception_SM_1");
					throw e;
				}
			}
			a3 = System.currentTimeMillis() - a3;
			System.out.println("a3: " + a3);
			return comments;
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainAllPublicationComments tardo: " + tardo);
		}
	}

	/**
	 * Se cargan todas las publicaciones, haciendo scrolls, del timestamp definido en las variables del CONFIG.
	 */
	private Comment extractCommentData(WebElement comentario) throws Exception {
		Comment auxComment = new Comment();
		// Mensaje primero se fija si tiene los tres puntitos o el leer mas y lo expande
		long a = System.currentTimeMillis();
		if (this.existElement(comentario, FacebookConfig.XPATH_SEE_MORE_COMMENT_TEXT)) {
			WebElement aux;
			while (this.existElement(comentario, FacebookConfig.XPATH_SEE_MORE_COMMENT_TEXT)) {
				aux = comentario.findElement(By.xpath(FacebookConfig.XPATH_SEE_MORE_COMMENT_TEXT));
				try {
					moveTo(aux);
					aux.click();
					try {
						this.waitForMoreTextInCommentMessageLink(this, comentario);
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) { // Si la publicación no tiene ningun tipo de actividad...

						} else {
							throw e;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (debug)
						this.saveScreenShot("Error VerMasContenidoMensaje");
					throw e;
				}
			}
		}
		a = System.currentTimeMillis() - a;
		System.out.println("ver mas texto del comentario tardo: " + a);
		if (this.existElement(comentario, FacebookConfig.XPATH_USER_COMMENT2)) {
			String aux = "";
			List<WebElement> comment_parts = comentario.findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT2));
			for (int i = 0; i < comment_parts.size(); i++) {
				aux += comment_parts.get(i).getText();
			}
			auxComment.setMensaje(aux);
		} else {
			// Puede ser porque postea solo una imagen...
			auxComment.setMensaje("");
		}
		// Usuario id
		if (this.getAccess() != null) {
			String ini = "id=";
			String fin = "&";
			String pathUserID = comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT)).getAttribute("data-hovercard");
			auxComment.setUserId(pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));
		}
		// Usuario name
		auxComment.setUserName(comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_NAME_COMMENT)).getText());

		// Utime
		auxComment.setUTime(Long.valueOf(comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME2)).getAttribute("data-utime")));
		try {
			// https://www.facebook.com/mauriciomacri/videos/10156570748083478/?comment_id=10156570873778478&comment_tracking=%7B%22tn%22%3A%22R9%22%7D
			String href = comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_ID2)).getAttribute("href");
			String commentId = href.split("\\?")[1].split("&")[0].split("=")[1];
			auxComment.setId(commentId);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// Extraer likes.
		if (this.getAccess() != null) {
			auxComment.setCantLikes(Integer.valueOf(comentario.findElement(By.xpath(".//span[contains(@class,'UFICommentLikeButton')]")).getText()));
		}

		return auxComment;
	}

	private boolean waitForMoreTextInCommentMessageLink(final FacebookPostScrap fs, final WebElement comentario) {
		ExpectedCondition<Boolean> moreTextLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (fs.existElement(comentario, ".//a[@class='_5v47 fss']")) {
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(1)).pollingEvery(Duration.ofMillis(50)).ignoring(NoSuchElementException.class);
		return wait.until(moreTextLink);
	}

	private boolean waitUntilShowMoreCommAppears(final FacebookPostScrap fs, final WebElement component, final String xpath) {
		ExpectedCondition<Boolean> commentLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (fs.existElement(component, xpath) && component.findElement(By.xpath(xpath)).isDisplayed()) {
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));
		return wait.until(commentLink);
	}
}
