package com.rocasolida.scrap;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
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

import com.rocasolida.FacebookConfig;
import com.rocasolida.entities.Comment;
import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Page;
import com.rocasolida.entities.Publication;
import com.rocasolida.scrap.util.CommentsSort;
import com.rocasolida.scrap.util.Driver;
import com.rocasolida.scrap.util.FacebookPostType;
import com.rocasolida.scrap.util.ScrapUtils;

public class FacebookScrap extends Scrap {

	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	private static Integer WAIT_UNTIL_SECONDS = 10;
	private static Integer WAIT_UNTIL_SPINNER = 10;
	private static Integer MAX_COMMENTS_PER_POST = 200;
	private static final Pattern ptURLPostTypePhotos = Pattern.compile("^.*\\/photos\\/.*$");
	private static final Pattern ptURLPostTypeVideos = Pattern.compile("^.*\\/videos\\/.*$");

	public FacebookScrap(Driver driver, boolean debug) throws MalformedURLException {
		super(driver, debug);
	}

	public boolean login(Credential access) {
		long tardo = System.currentTimeMillis();
		try {
			if (this.navigateTo(FacebookConfig.URL)) {
				// this.saveScreenShot("LOGIN");
				if (this.existElement(null, FacebookConfig.XPATH_BUTTON_LOGIN)) {
					WebElement formLogin = this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_FORM_LOGIN));
					formLogin.findElement(By.xpath(FacebookConfig.XPATH_INPUT_MAIL_LOGIN)).sendKeys(access.getUser());
					formLogin.findElement(By.xpath(FacebookConfig.XPATH_INPUT_PASS_LOGIN)).sendKeys(access.getPass());
					formLogin.findElement(By.xpath(FacebookConfig.XPATH_BUTTON_LOGIN)).click();
					if (loggedIn()) {
						super.setAccess(access);
						if (debug)
							System.out.println("[SUCCESS]Login Successfull! " + "usr: " + this.getAccess().getUser());
						return true;
					} else {
						if (debug)
							System.out.println("[ERROR]Check Login Credentials! " + "usr: " + access.getUser());
						return false;
					}
				}
				if (debug)
					System.out.println("[ERROR] No se cargó el botón de Login. Expression: " + FacebookConfig.XPATH_FORM_LOGIN);
				return false;
			} else {
				if (debug)
					System.out.println("[ERROR] AL INTENTAR ACCEDER A LA PÁGINA DE LOGIN: " + FacebookConfig.URL);
				return false;
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("login tardo: " + tardo);
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

	public Page obtainPageInformation(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			Page page = new Page();
			page.setName(facebookPage);
			List<WebElement> publicationsElements = this.inicializePublicationsToBeLoad(facebookPage, uTIME_INI, uTIME_FIN, page);
			if (publicationsElements != null) {
				List<Publication> publicationsImpl = new ArrayList<Publication>();
				for (int i = 0; i < publicationsElements.size(); i++) {
					if (this.waitForJStoLoad()) {
						this.moveTo(publicationsElements.get(i));
						publicationsImpl.add(this.extractPublicationData(publicationsElements.get(i)));
					} else {
						System.out.println("[ERROR] PROBLEMAS AL EXTRAER DATOS DEL POST.");
						this.saveScreenShot("PROBLEMA_EXTRAER_DATOSPOST");
					}

				}
				// Recorro publicaciones encontradas
				for (int i = 0; i < publicationsImpl.size(); i++) {
					// Voy a la pagina de la publicacion
					try {
						this.navigateTo(FacebookConfig.URL + publicationsImpl.get(i).getId());
					} catch (Exception e) {
						System.err.println("[ERROR] NO SE PUDO ACCEDER AL LINK DEL POST");
						this.saveScreenShot("ERR_ACCESO_POST");
						throw e;
					}
					try {
						this.ctrlLoadPost();
						if (debug)
							this.saveScreenShot("PostLoaded");
					} catch (Exception e) {
						System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
						throw e;
					}
					WebElement pubsNew;
					try {
						pubsNew = this.publicationCommentSectionClick();
						if (pubsNew == null) {
							for (int h = 0; h < 3; h++) {
								if (debug)
									System.out.println("[INFO]recargando el post... no tiene más scroll.");
								this.getDriver().navigate().refresh();
								this.ctrlLoadPost();
								pubsNew = this.publicationCommentSectionClick();
								if (pubsNew != null) {
									h = 3;
								}
							}
						}
						try {
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							if (!this.tipoCargaComentarios(pubsNew, 2)) {
								publicationsImpl.get(i).setComments(null);
								page.setPublications(publicationsImpl);
								continue;
							}
						} catch (Exception e) {
							if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
								if (debug) {
									System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
									this.saveScreenShot("WARN_SPINNERLOAD");
								}
								for (int j = 0; j < 3; j++) {
									if (debug)
										System.out.println("[INFO] INTENTO " + (j + 1) + " PARA QUE EL SPINNER NO SE MUESTRE.");
									try {
										this.navigateTo(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST + publicationsImpl.get(i).getId());
										this.ctrlLoadPost();
										pubsNew = this.publicationCommentSectionClick();
										if (debug)
											System.out.println("[INFO] SPINNER ACTIVE?...");
										this.waitUntilNotSpinnerLoading();
										this.tipoCargaComentarios(pubsNew, 2);
										j = 3;
									} catch (Exception e1) {
										if (e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
											if (debug) {
												System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
												this.saveScreenShot("WARN_SPINNERLOAD");
											}
										} else {
											e1.printStackTrace();
											throw e;
										}
									}

								}
							} else {
								throw e;
							}
						}
						extractPublicationDataFromDivOnPublicationPage(publicationsImpl.get(i), pubsNew);
						if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER) || (pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed())) {
							if (debug)
								System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION NRO#" + (i + 1) + ": " + FacebookConfig.URL + publicationsImpl.get(i).getId());
							publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, MAX_COMMENTS_PER_POST));
						} else {
							if (debug)
								System.out.println("[WARN] LA PUBLICACION NO TIENE COMENTARIOS");
						}
						page.setPublications(publicationsImpl);
					} catch (Exception e) {
						if (debug) {
							System.out.println("[ERROR] AL ACCEDER AL POST.");
							this.saveScreenShot("ERR_ACCESO_POST");
						}
						throw e;
					}
				}
				return page;
			} else {
				if (debug)
					System.out.println("[INFO] NO SE ENCONTRARON PUBLICACIONES PARA PROCESAR.");
				return null;
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainPageInformation tardo: " + tardo);
		}
	}

	public Page obtainPageInformationWithoutComments(String facebookPage, Long uTIME_INI, Long uTIME_FIN) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			Page page = new Page();
			page.setName(facebookPage);
			List<WebElement> publicationsElements = this.inicializePublicationsToBeLoad(facebookPage, uTIME_INI, uTIME_FIN, page);
			if (publicationsElements != null) {
				List<Publication> publicationsImpl = new ArrayList<Publication>();
				for (int i = 0; i < publicationsElements.size(); i++) {
					if (this.waitForJStoLoad()) {
						this.moveTo(publicationsElements.get(i));
						publicationsImpl.add(this.extractPublicationData(publicationsElements.get(i)));
					} else {
						System.out.println("[ERROR] PROBLEMAS AL EXTRAER DATOS DEL POST.");
						this.saveScreenShot("PROBLEMA_EXTRAER_DATOSPOST");
					}

				}
				page.setPublications(publicationsImpl);
				// Recorro publicaciones encontradas
				for (int i = 0; i < publicationsImpl.size(); i++) {
					// Voy a la pagina de la publicacion
					try {
						this.navigateTo(FacebookConfig.URL + publicationsImpl.get(i).getId());
					} catch (Exception e) {
						System.err.println("[ERROR] NO SE PUDO ACCEDER AL LINK DEL POST");
						this.saveScreenShot("ERR_ACCESO_POST");
						throw e;
					}
					try {
						this.ctrlLoadPost();
						if (debug)
							this.saveScreenShot("PostLoaded");
					} catch (Exception e) {
						System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
						throw e;
					}
					WebElement pubsNew;
					try {
						pubsNew = this.publicationCommentSectionClick();
						if (pubsNew == null) {
							for (int h = 0; h < 3; h++) {
								if (debug)
									System.out.println("[INFO]recargando el post... no tiene más scroll.");
								this.getDriver().navigate().refresh();
								this.ctrlLoadPost();
								pubsNew = this.publicationCommentSectionClick();
								if (pubsNew != null) {
									h = 3;
								}
							}
						}
						try {
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							if (!this.tipoCargaComentarios(pubsNew, 2)) {
								publicationsImpl.get(i).setComments(null);
								continue;
							}
						} catch (Exception e) {
							if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
								if (debug) {
									System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
									this.saveScreenShot("WARN_SPINNERLOAD");
								}
								for (int j = 0; j < 3; j++) {
									if (debug)
										System.out.println("[INFO] INTENTO " + (j + 1) + " PARA QUE EL SPINNER NO SE MUESTRE.");
									try {
										this.navigateTo(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST + publicationsImpl.get(i).getId());
										this.ctrlLoadPost();
										pubsNew = this.publicationCommentSectionClick();
										if (debug)
											System.out.println("[INFO] SPINNER ACTIVE?...");
										this.waitUntilNotSpinnerLoading();
										this.tipoCargaComentarios(pubsNew, 2);
										j = 3;
									} catch (Exception e1) {
										if (e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
											if (debug) {
												System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
												this.saveScreenShot("WARN_SPINNERLOAD");
											}
										} else {
											e1.printStackTrace();
											throw e;
										}
									}

								}
							} else {
								throw e;
							}
						}
						extractPublicationDataFromDivOnPublicationPage(publicationsImpl.get(i), pubsNew);
					} catch (Exception e) {
						if (debug) {
							System.out.println("[ERROR] AL ACCEDER AL POST.");
							this.saveScreenShot("ERR_ACCESO_POST");
						}
						throw e;
					}
				}
				return page;
			} else {
				if (debug)
					System.out.println("[INFO] NO SE ENCONTRARON PUBLICACIONES PARA PROCESAR.");
				return null;
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainPageInformation tardo: " + tardo);
		}
	}

	public Publication obtainPostInformation(String postId, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs) throws Exception {
		// Voy a la pagina de la publicacion
		try {
			Publication pub = new Publication();
			pub.setId(postId);
			try {
				this.navigateTo(FacebookConfig.URL + postId);
			} catch (Exception e) {
				System.err.println("[ERROR] NO SE PUDO ACCEDER AL LINK DEL POST");
				this.saveScreenShot("ERR_ACCESO_POST");
				throw e;
			}
			try {
				this.ctrlLoadPost();
				if (debug)
					this.saveScreenShot("PostLoaded");
			} catch (Exception e) {
				System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
				throw e;
			}
			String currentURL = this.getDriver().getCurrentUrl();
			FacebookPostType fpt = getPostType(currentURL);
			System.out.println("currentURL: " + currentURL + ". fpt: " + fpt);
			if (fpt != null && fpt.equals(FacebookPostType.PHOTO)) {
				return obtainPostTypePhotoInformation(postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub);
			} else {
				return obtainPostTypeOtherInformation(postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub);
			}
		} catch (Exception e) {
			if (debug) {
				System.out.println("[ERROR] AL ACCEDER AL POST.");
				this.saveScreenShot("ERR_ACCESO_POST");
			}
			throw e;
		}
	}

	private Publication obtainPostTypePhotoInformation(String postId, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs, Publication pub) throws Exception {
		WebElement pubsNew = this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_TYPE_PHOTO_CONTAINER + "[1]"));
		if (cs == null || cs.equals(CommentsSort.NEW)) {
			try {
				if (debug)
					System.out.println("[INFO] SPINNER ACTIVE?...");
				this.waitUntilNotSpinnerLoading();
				if (!this.tipoCargaComentarios(pubsNew, 2)) {
					pub.setComments(null);
					return pub;
				}
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
					if (debug) {
						System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
						this.saveScreenShot("WARN_SPINNERLOAD");
					}
					for (int j = 0; j < 3; j++) {
						if (debug)
							System.out.println("[INFO] INTENTO " + (j + 1) + " PARA QUE EL SPINNER NO SE MUESTRE.");
						try {
							this.navigateTo(FacebookConfig.URL + postId);
							this.ctrlLoadPost();
							pubsNew = this.publicationCommentSectionClick();
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							this.tipoCargaComentarios(pubsNew, 2);
							break;
						} catch (Exception e1) {
							if (e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
								if (debug) {
									System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
									this.saveScreenShot("WARN_SPINNERLOAD");
								}
							} else {
								e1.printStackTrace();
								throw e;
							}
						}

					}
				} else {
					throw e;
				}
			}
		}
		extractPublicationDataFromDivOnPublicationPage(pub, pubsNew);
		if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER) || (pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed())) {
			if (debug)
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION " + ": " + FacebookConfig.URL + pub.getId());
			pub.setComments(this.extractPubComments(pubsNew, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments));
		} else {
			if (debug)
				System.out.println("[WARN] LA PUBLICACION NO TIENE COMENTARIOS");
		}
		return pub;
	}

	private Publication obtainPostTypeOtherInformation(String postId, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs, Publication pub) throws Exception {
		WebElement pubsNew;
		pubsNew = this.publicationCommentSectionClick();
		if (pubsNew == null) {
			for (int h = 0; h < 3; h++) {
				if (debug)
					System.out.println("[INFO]recargando el post... no tiene más scroll.");
				this.getDriver().navigate().refresh();
				this.ctrlLoadPost();
				pubsNew = this.publicationCommentSectionClick();
				if (pubsNew != null) {
					break;
				}
			}
		}
		if (cs == null || cs.equals(CommentsSort.NEW)) {
			try {
				if (debug)
					System.out.println("[INFO] SPINNER ACTIVE?...");
				this.waitUntilNotSpinnerLoading();
				if (!this.tipoCargaComentarios(pubsNew, 2)) {
					pub.setComments(null);
					return pub;
				}
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
					if (debug) {
						System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
						this.saveScreenShot("WARN_SPINNERLOAD");
					}
					for (int j = 0; j < 3; j++) {
						if (debug)
							System.out.println("[INFO] INTENTO " + (j + 1) + " PARA QUE EL SPINNER NO SE MUESTRE.");
						try {
							this.navigateTo(FacebookConfig.URL + postId);
							this.ctrlLoadPost();
							pubsNew = this.publicationCommentSectionClick();
							if (debug)
								System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							this.tipoCargaComentarios(pubsNew, 2);
							break;
						} catch (Exception e1) {
							if (e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
								if (debug) {
									System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMENTARIOS! SPINNER ACTIVE!");
									this.saveScreenShot("WARN_SPINNERLOAD");
								}
							} else {
								e1.printStackTrace();
								throw e;
							}
						}

					}
				} else {
					throw e;
				}
			}
		}
		extractPublicationDataFromDivOnPublicationPage(pub, pubsNew);
		if (this.existElement(pubsNew, FacebookConfig.XPATH_COMMENTS_CONTAINER) || (pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed())) {
			if (debug)
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION " + ": " + FacebookConfig.URL + pub.getId());
			pub.setComments(this.extractPubComments(pubsNew, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments));
		} else {
			if (debug)
				System.out.println("[WARN] LA PUBLICACION NO TIENE COMENTARIOS");
		}
		return pub;
	}

	private FacebookPostType getPostType(String currentUrl) {
		if (currentUrl != null) {
			if (ptURLPostTypePhotos.matcher(currentUrl).matches()) {
				return FacebookPostType.PHOTO;
			} else if (ptURLPostTypeVideos.matcher(currentUrl).matches()) {
				return FacebookPostType.VIDEO;
			} else {
				return FacebookPostType.OTHER;
			}
		}
		return null;
	}

	private void extractPublicationDataFromDivOnPublicationPage(Publication publication, WebElement pubsNew) {
		try {
			// Cargo los likes del post y la cantidad de comments
			List<WebElement> wes = pubsNew.findElements(By.xpath("//*[contains(@class,'commentable_item')]//div[contains(@class,'_sa_')]//span"));
			// *[@id="u_0_o"]/div[1]/div/div/div/div/div/a/span[1]
			if (wes != null) {
				for (WebElement we : wes) {
					String aux = we.getText().toLowerCase();
					if (aux.contains("likes") || aux.contains(" me gusta")) {
						publication.setCantLikes(ScrapUtils.parseCount(aux));
					} else if (aux.contains("comments")) {
						publication.setCantComments(ScrapUtils.parseCount(aux));
					} else if (aux.contains("shares")) {
						publication.setCantShare(ScrapUtils.parseCount(aux));
					} else if (aux.contains("views")) {
						publication.setCantReproducciones(ScrapUtils.parseCount(aux));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void zoomOut() {
		/*
		 * WebElement html = this.getDriver().findElement(By.tagName("html"));
		 * html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		 * html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		 * html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		 * html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		 */

		/*
		 * JavascriptExecutor js = (JavascriptExecutor) this.getDriver();
		 * js.executeScript("document.body.style.zoom='40%'");
		 */

		Robot robot;
		try {
			robot = new Robot();
			for (int i = 0; i < 25; i++) {// -25%
				robot.keyPress(KeyEvent.VK_CONTROL);
				robot.keyPress(KeyEvent.VK_MINUS);
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void ctrlLoadPost() throws Exception {
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

	public boolean waitForClosingPost() {
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

	public boolean waitForVideoLoaded() {
		ExpectedCondition<Boolean> pubsLoaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				// Si existe la lista de Videos y se muestra en pantalla...
				if (driver.findElements(By.xpath("//div[@class='_2e7p']")).size() > 0 && driver.findElement(By.xpath("//div[@class='_2e7p']")).isDisplayed()) {
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

	public WebElement publicationCommentSectionClick() throws Exception {
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
							pubsNew.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();
							return pubsNew;
						}
					}

				} catch (Exception e) {
					if (debug) {
						System.err.println("[ERROR] ACCESO A SECCION COMENTARIOS DE LA PUBLICACIÓN");
						this.saveScreenShot("ERR_ACCESO_COMM_PUB");
					}
					throw e;
				}
			}
		}
		return pubsNew;
	}

	public boolean waitUntilPopupLoginAppears() {
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

	public boolean waitUntilCommentSectionVisible(final WebElement pub) {
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

	public List<Comment> extractPubComments(WebElement pub, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			if (this.existElement(pub, FacebookConfig.XPATH_COMMENTS_CONTAINER + "//*")) {
				// this.TipoCargaComentarios(pub, 3);
				if (debug)
					System.out.println("[INFO] OBTENIENDO LOS COMENTARIOS DEL POST: ");
				return this.obtainAllPublicationComments(pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER)), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments);
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

	public void updatePageLikes(Page page) {
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

	public List<WebElement> inicializePublicationsToBeLoad(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Page page) throws Exception {
		long aux = System.currentTimeMillis();
		try {
			if (this.navigateTo(FacebookConfig.URL + facebookPage)) { // SI NO TIRA ERROR DE CONEXIÓN O DE PAGINA INEXISTENTE...
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
					return this.processPagePosts(facebookPage, uTIME_INI, uTIME_FIN);
				default:
					if (debug)
						System.out.println("[WARNING] No se reconoce el tipo de página para hacer SCRAP");
					return null;
				}
			}
			return null;
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("inicializePublicationsToBeLoad tardo: " + aux);
		}
	}

	public List<WebElement> processPagePosts(String facebookPage, Long uTIME_INI, Long uTIME_FIN) throws Exception {
		try {
			long aux = System.currentTimeMillis();
			this.overlayHandler();
			aux = System.currentTimeMillis() - aux;
			System.out.println("overlayHandler: " + aux);
			aux = System.currentTimeMillis();
			this.goToPublicationsSection();
			aux = System.currentTimeMillis() - aux;
			System.out.println("goToPublicationsSection: " + aux);
			aux = System.currentTimeMillis();
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
			aux = System.currentTimeMillis() - aux;
			System.out.println("otra parte: " + aux);
			aux = System.currentTimeMillis();
			if (debug)
				System.out.println("[INFO] BUSCANDO PUBLICACIONES ENTRE EL RANGO DE FEHCAS DADA....");
			if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
				while (!((this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookPage, uTIME_INI))).size()) > 0)) {
					this.saveScreenShot("a1-" + System.currentTimeMillis());
					try {
						this.waitUntilShowMorePubsAppears(this);
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
							if (debug)
								System.out.println("[WARN] TimeoutException. Waiting ShowmorePublications button");
						} else {
							e.printStackTrace();
							throw e;
						}
					}
					if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
						this.scrollMainPublicationsPage();
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
					} else {

						if (debug) {
							this.saveScreenShot("posts");
							System.out.println("[INFO] YA SE RECORRIERON TODAS LAS PUBLICACIONES DE LA PÁGINA. NO SE ENCONTRÓ BTN SHOW MORE: " + FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
						}
						break;
					}
					if (debug)
						System.out.print("...|");
				}
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
			System.out.println("otra parte2: " + aux);
			aux = System.currentTimeMillis();
		} catch (Exception e) {
			if (debug) {
				System.err.println("[ERROR] EN LA CARGA DE PUBLICACIONES.");
				this.saveScreenShot("ERR_CARGA_PUBS");
			}
			throw e;
		}
		// RETORNO SOLO LAS PUBLICACIONES QUE CUMPLIERON CON EL FILTRO.
		return this.filterPostsByUTIME(facebookPage, uTIME_INI, uTIME_FIN);
	}

	public boolean waitUntilNotSpinnerLoading() {
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

	private boolean waitUntilNotSpinnerLoading(WebElement element) {
		ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				List<WebElement> wes = driver.findElements(By.xpath("//span[@role='progressbar']"));
				if (wes != null && wes.size() > 0) {
					for (WebElement we : wes) {
						if (we.isDisplayed()) {
							return false;
						}
					}
					return true;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SPINNER)).pollingEvery(Duration.ofMillis(200)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(morePubsLink);
	}

	public boolean waitForPublicationsLoaded(final FacebookScrap fs) {
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

	public boolean waitUntilShowMorePubsAppears(final FacebookScrap fs) {
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
		return wait.until(morePubsLink);
	}

	public boolean overlayHandler() {
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

	public void goToPublicationsSection() throws Exception {
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

	public boolean waitUntilPublicationsMenuOption() {
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

	public List<WebElement> filterPostsByUTIME(String facebookPage, Long uTIME_INI, Long uTIME_FIN) {
		long aux = System.currentTimeMillis();
		try {
			int match = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN) + "//ancestor::div[contains(@class,'userContentWrapper')]")).size();
			if (match > 0) {
				if (debug)
					System.out.println("[INFO] SE ENCONTRARON " + String.valueOf(match) + " PUBLICACIONES ENTRE LAS FECHAS > a " + uTIME_INI + " y < a " + uTIME_FIN);
				return this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION(facebookPage, uTIME_INI, uTIME_FIN) + "//ancestor::div[contains(@class,'userContentWrapper')]"));
			} else {
				if (debug)
					System.out.println("[WARN] NO SE ENCONTRARON PUBLICACIONES EN LAS FECHAS INDICADAS." + " INICIO:" + uTIME_INI + " FIN:" + uTIME_FIN);
				return null;
			}
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("filterPostsByUTIME: " + aux);
		}
	}

	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		// this.waitForJStoLoad();
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// System.out.println("[ERROR] NO SE PUDO HACER LA ESPERA THREAD.SLEEP");
		// }
		// this.saveScreenShot("Scroll_MainPAge");
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
	 * Si existe el botón de show more, entonces lo clickea, hasta que se cargaron
	 * todos los mensajes para luego obtenerlos con un XPATH query y extraerle los
	 * datos. Me servirá para las replies y para los comentarios.
	 * 
	 * @param cantComments
	 * @param cs
	 */
	public List<Comment> obtainAllPublicationComments(WebElement container, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			long a1 = System.currentTimeMillis();
			if (cantComments == null) {
				cantComments = MAX_COMMENTS_PER_POST;
			}
			List<Comment> comments = new ArrayList<Comment>();
			if (container.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
				if (debug)
					System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
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
				// this.waitUntilShowMoreCommAppears(this, container,
				// FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
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
				List<WebElement> comentarios = container.findElements(By.xpath(commentsFilter));
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
					comentarios = container.findElements(By.xpath(commentsFilter));
					if (debug)
						System.out.println("CANT COMENTARIOS: " + comentarios.size());
					a2 = System.currentTimeMillis() - a2;
					System.out.println("a2: " + a2 + ". comentarios.size(): " + comentarios.size() + ". comments.size(): " + comments.size() + ".");
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

	public void clickOnReplyLinks() {
		while (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENT_REPLY_LINKS)).size() > 0) {
			List<WebElement> replyLinks = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENT_REPLY_LINKS));
			for (int i = 0; i < replyLinks.size(); i++) {

			}
		}
	}

	public boolean waitUntilShowMoreCommAppears(final FacebookScrap fs, final WebElement component, final String xpath) {
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

	public void totalCommentsPub(WebElement pub) {
		if (pub.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			if (debug)
				System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + pub.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
		}
	}

	/**
	 * Se cargan todas las publicaciones, haciendo scrolls, del timestamp definido
	 * en las variables del CONFIG.
	 */
	public Comment extractCommentData(WebElement comentario) throws Exception {
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

	public boolean waitForMoreTextInCommentMessageLink(final FacebookScrap fs, final WebElement comentario) {
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

	public boolean waitForJStoLoad() {
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

	public void moveTo(WebElement element) {
		if (this.getAccess() == null) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({block: \"start\"});", element);
		} else {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", element);
		}
	}

	public void checkAndClosePopupLogin() {
		if ((this.existElement(null, "//a[@id='expanding_cta_close_button']") && this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).isDisplayed())) {
			try {
				this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
				if (debug)
					System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
			} catch (Exception e) {
				if (e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
					this.scrollDown();
					this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
					if (debug)
						System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
				} else {
					if (debug)
						this.saveScreenShot("ERRCLOSEPOPUPLGIN");
					e.printStackTrace();
				}
			}
		}
	}

	public Publication extractPublicationData(WebElement publication) {
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
			 * Hay dos casos (necesito saber el abbr que contiene un timestamp, sino se
			 * confunde cuando comparten un post de otra cuenta de facebook): <abbr
			 * data-utime='' class='timestamp'> <abbr data-utime=''><span class='timestamp'>
			 */

			if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
				aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
			} else if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) {
				aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).getAttribute("data-utime")));
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
			/*
			 * Usaremos siempre el UTC. if (this.existElement(publication,
			 * FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
			 * aux.setDateTime((publication.findElement(By.xpath(FacebookConfig.
			 * XPATH_PUBLICATION_TIMESTAMP))).getAttribute("title")); } else if
			 * (this.existElement(publication,
			 * FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) {
			 * aux.setDateTime((publication.findElement(By.xpath(FacebookConfig.
			 * XPATH_PUBLICATION_TIMESTAMP_1))).getAttribute("title")); }
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
			if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)) {
				aux.setCantShare(Integer.parseInt(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).getText().replaceAll("\\D+", "")));
			} else {
				aux.setCantShare(0);
			}
			/**
			 * CANTIDAD DE LIKES
			 */
			try {
				if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_CANT_LIKE)) {
					String auxLikes = publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_LIKE)).findElement(By.xpath("//span[contains(@class,'_3chu')]")).getAttribute("innerHTML");
					if (auxLikes.contains("&nbsp;mil")) {
						auxLikes = auxLikes.replaceAll("&nbsp;mil", "000");
						if (auxLikes.contains(",")) {
							auxLikes = auxLikes.replaceAll(",", "");
						}
					}
					aux.setCantLikes(Integer.valueOf(auxLikes));
				} else {
					aux.setCantLikes(0);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return aux;
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("extractPublicationData tardo: " + tardo);
		}
	}

	/**
	 * SIN LOGIN por el momento sin uso.
	 */
	public void obtainPublicationsAndCommentsNotLoggedIn(String facebookPage) {
		long tardo = System.currentTimeMillis();
		try {
			this.getDriver().navigate().to(FacebookConfig.URL + facebookPage);

			List<WebElement> publicationsElements;
			// Busco todas las publicaciones que se cargaron. (Si entras sin usuario
			// logueado, te carga 16 publicaciones de una vez).
			if (this.existElement(null, FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)) {
				publicationsElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
				List<Publication> publicationsImpl = new ArrayList<Publication>();

				for (int i = 0; i < publicationsElements.size(); i++) {
					if (debug)
						System.out.println(" =============== " + i + " DATOS PUBLICACIÓN ================= ");
					Publication aux = new Publication();

					/**
					 * TIMESTAMP El timestamp viene en GMT.
					 */
					aux.setUTime(Long.parseLong(publicationsElements.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));

					/**
					 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO...
					 * SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
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
				// this.printPublications(publicationsImpl);
			} else {
				if (debug)
					System.out.println("[ERROR] No se encontraron las publicaciones.");
			}
		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainPublicationsAndCommentsNotLoggedIn tardo: " + tardo);
		}
	}

	private boolean loggedIn() {
		try {
			this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_FORM_LOGIN));
			if (debug)
				System.out.println("[ERROR]Login error! check credentials provided");
			return false;
		} catch (NoSuchElementException e) {
			return true;
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

	public void printPage(Page page) {
		if (page != null) {
			System.out.println(":::::::::::::::::::::PAGE LIKES: " + page.getLikes());
			System.out.println(":::::::::::::::::::::PAGE FOLLOWERS: " + page.getFollowers());
			if (page.getPublications() != null) {
				System.out.println("SE ENCONTRARON UN TOTAL DE " + page.getPublications().size() + "PUBLICACIONES");
				/*
				 * for (int j = 0; j < page.getPublications().size(); j++) {
				 * System.out.println("============== PUBLICATION " + (j + 1) +
				 * " INICIO	===============");
				 * System.out.println(page.getPublications().get(j).toString());
				 * System.out.println("************** PUBLICATION " + (j + 1) +
				 * " FIN	***************"); }
				 */
				for (int j = 0; j < page.getPublications().size(); j++) {
					System.out.println("============== PUBLICATION " + (j + 1) + " INICIO	===============");
					System.out.println("TOTAL COMENTARIOS: " + page.getPublications().get(j).getComments().size());
					System.out.println("************** PUBLICATION " + (j + 1) + " FIN	***************");

				}
			} else {
				System.out.println("[INFO] PrintPage():LA LISTA DE PUBLICACIONES PARA IMPRIMIR ESTÁ VACÍA.");
			}
		}
	}

	private void saveScreenShot(String name) {
		if (debug) {
			Path path = Paths.get("", "screenshots", name);
			File scrFile = ((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.FILE);
			try {
				FileUtils.copyFile(scrFile, new File(path.toString() + System.currentTimeMillis() + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
				 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente,
				 * ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o
				 * inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost
				 * inexistente) - id post válido, pero URL inválida
				 * https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
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

	public boolean waitForPageLoaded() {

		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				String status = ((JavascriptExecutor) driver).executeScript("return document.readyState").toString();
				if (status.equals("complete")) {
					// System.out.println("Estado pagina: " + status);
					return true;
				} else {
					// System.out.println("Estado pagina: " + status);
					return false;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS)).pollingEvery(Duration.ofMillis(200));

		return wait.until(jsLoad);

	}

	// "opt=1: Comentarios Relevantes" --> Es el filtro por default que tienen todos
	// los posts de Facebook.
	// "opt=2: Más Recientes" --> Cuando se actualice un post
	// "opt=3: Comentarios Relevantes(no filtrados)" --> TODOS los comentarios
	private boolean tipoCargaComentarios(WebElement Post, int option) throws Exception {
		try {
			/*
			 * if(this.getAccess() == null) { this.scrollDown(); }
			 */
			// Puede que se abra automáticamente el Enviar mensajes al dueño de la página.
			if (this.getAccess() != null) {
				if (this.getDriver().findElements(By.xpath("//a[@class='_3olu _3olv close button _4vu4']")).size() > 0) {
					this.getDriver().findElement(By.xpath("//a[@class='_3olu _3olv close button _4vu4']")).click();
				}
			}

			/*
			 * if (this.getAccess() == null) { this.checkAndClosePopupLogin(); }
			 */

			try {
				try {
					this.waitUntilMenuOptionAppears(this, Post);
				} catch (Exception e) {
					if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) { // Si la publicación no tiene ningun tipo de actividad...
						if (debug)
							System.out.println("[WARN] TIEMPO DE ESPERA A QUE APAREZCA LINK TIPO CARGA AGOTADO");
						return false;
					} else {
						throw e;
					}
				}
				if (this.getAccess() != null) {
					this.moveTo(Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")));
				}

				try {
					if (debug)
						this.saveScreenShot("Antes_Sel_TipoCargAComentarios");
					Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).click();
					if (debug)
						this.saveScreenShot("Despues_Sel_TipoCargAComentarios");
				} catch (Exception e) {
					if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException") || e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
						this.overlayHandler();
						this.checkAndClosePopupLogin();
						this.scrollDown();
						if (this.getDriver().findElements(By.xpath("//span[@role='progressbar']")).size() > 0 && this.getDriver().findElement(By.xpath("//span[@role='progressbar']")).isDisplayed()) {
							try {
								if (debug)
									System.out.println("[INFO] SPINNER ACTIVE?...");
								this.waitUntilNotSpinnerLoading();
								this.scrollDown();
							} catch (Exception e1) {
								if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
									if (debug)
										System.out.println("[WARN] TIEMPO ESPERA NOT SPINNER EXCEEDED (Tipo De Carga)");
								}
								throw e;
							}
						}
						Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).click();
						if (debug)
							this.saveScreenShot("Despues_Sel_TipoCargAComentarios");
					} else {
						if (debug)
							this.saveScreenShot("ERR_Sel_OptionPpal");
						throw e;
					}
				}

				/*
				 * if(this.getAccess()==null) { this.scrollDown(); }
				 */
				// this.saveScreenShot("Sccrolldown");
				if (this.waitUntilMenuAppears()) {
					WebElement menuOption = null;
					try {
						menuOption = this.getDriver().findElement(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li[" + option + "]"));
						menuOption.click();
					} catch (Exception e) {
						if (e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							this.overlayHandler();
							this.checkAndClosePopupLogin();
							this.scrollDown();
							if (debug)
								this.saveScreenShot("ListOpt_TIPOCARGA");
							// Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow
							// UFILikeSentence')]/descendant::a[@class='_p']")).click();
							try {
								this.waitUntilMenuAppears();
								this.scrollDown();
								if (debug)
									this.saveScreenShot("opt_TIPOCARGA");
								menuOption.click();
							} catch (Exception e1) {
								if (e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
									if (debug)
										System.out.println("[WARN] no se mostraron las opciones de tipo carga");
								} else {
									throw e1;
								}
							}

						} else if (e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
							// puede ser que no tenga la opción... caso
							// Curne.(https://www.facebook.com/2018054074880609)
							return true;
						} else {
							if (debug)
								this.saveScreenShot("ERR_Sel_menuOption");
							throw e;
						}
					}
				}
			} catch (Exception e) {
				if (debug) {
					System.err.println("[ERROR] ERROR SELECCIONANDO EL TIPO DE CARGA");
					this.saveScreenShot("ERROR_TIPOCARGA");
				}
				// e.printStackTrace();
				throw e;
			}

			this.waitForJStoLoad();
			return true;
		} catch (Exception e) {
			if (debug) {
				System.err.println("[ERROR] NO SE PUDO HACER EL CLICK EN MOSTRAR TODOS LOS MENSAJES, SIN ORDENAMIENTO");
				this.saveScreenShot("ERR_NO_SELECCIONO_MOSTRAR_MENSAJES");
			}
			// e.printStackTrace();
			throw e;
		}

	}

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	public boolean waitUntilMenuOptionAppears(final FacebookScrap fs, final WebElement post) {
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

	public boolean waitUntilMenuAppears() {
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

	public String regexPostID(String link) {
		// www.facebook.com/teamisurus/photos/a.413505532007856.104138.401416556550087/2144570302234695/?type=3
		// www.facebook.com/teamisurus/posts/2143052825719776
		// https://www.facebook.com/permalink.php?story_fbid=1428319533981557&id=323063621173826
		// https://www.facebook.com/154152138076469/videos/972094692948872/
		String lastMatched = "";
		if (link.contains("permalink")) {
			String[] a = link.split("\\?")[1].split("&");
			for (String b : a) {
				if (b.contains("story_fbid=")) {
					return b.replace("story_fbid=", "");

				}
			}
		} else {

			String[] stringArray = link.split("/");
			Pattern pat = Pattern.compile("[0-9]{15,18}");
			for (int i = 0; i < stringArray.length; i++) {
				Matcher mat = pat.matcher(stringArray[i]);
				if (mat.matches()) {
					// System.out.println("[INFO] Post ID: " + stringArray[i]);
					// return stringArray[i];
					System.out.println("Valor macheado: " + stringArray[i]);
					lastMatched = stringArray[i];
				}
			}
		}
		return lastMatched;
	}

	public static void main(String args[]) {
		// String c =
		// "https://www.facebook.com/permalink.php?story_fbid=1428319533981557&id=323063621173826";
		// String[] a = c.split("\\?")[1].split("&");
		// for (String b : a) {
		// if (b.contains("story_fbid=")) {
		// System.out.println(b.replace("story_fbid=", ""));
		// }
		// }

		String url = "https://www.facebook.com/mauriciomacri/photos/a.105382683477.113835.55432788477/10156628922723478/?type=3&theater";
		String url2 = "https://www.facebook.com/mauriciomacri/videos/10156628998873478/";
		Pattern pt = Pattern.compile("^.*\\/photos\\/.*$");
		System.out.println(pt.matcher(url).matches());
		System.out.println(pt.matcher(url2).matches());
		Pattern pt2 = Pattern.compile("^.*\\/videos\\/.*$");
		System.out.println(pt2.matcher(url).matches());
		System.out.println(pt2.matcher(url2).matches());
	}

}
