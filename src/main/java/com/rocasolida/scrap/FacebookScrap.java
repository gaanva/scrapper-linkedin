package com.rocasolida.scrap;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
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
import com.rocasolida.scrap.util.Driver;

public class FacebookScrap extends Scrap {

	private Page page;
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	private static Integer WAIT_UNTIL_SECONDS = 10;
	private static Integer WAIT_UNTIL_SPINNER = 15;
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

	public Page obtainPageInformation(String facebookPage, Long uTIME_INI, Long uTIME_FIN, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) throws Exception{
		List<WebElement> publicationsElements = this.inicializePublicationsToBeLoad(facebookPage, uTIME_INI, uTIME_FIN);
		
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
		
			for (int i = 0; i < publicationsImpl.size(); i++) {
				System.out.println("[INFO] EXTRAYENDO DATOS DE COMENTARIOS DE LA PUBLICACION NRO#" + (i + 1) + ": "
						+ FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST
						+ publicationsImpl.get(i).getId());
				try {
					this.navigateTo(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST
							+ publicationsImpl.get(i).getId());
				} catch (Exception e) {
					System.err.println("[ERROR] NO SE PUDO ACCEDER AL LINK DEL POST");
					this.saveScreenShot("ERR_ACCESO_POST");
					throw e;
				}
				try{
					this.ctrlLoadPost();
					this.zoomOut();
					this.saveScreenShot("PostLoaded");
				}catch(Exception e) {
					System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
					throw e;
				}

				List<WebElement> pubsNew;
				try {
					pubsNew = this.publicationCommentSectionClick();
					
					try{
						System.out.println("[INFO] SPINNER ACTIVE?...");
						this.waitUntilNotSpinnerLoading();
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
							System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMETNARIOS! SPINNER ACTIVE!");
							this.saveScreenShot("WARN_SPINNERLOAD");
							for(int j=0; j<3; j++) {
								System.out.println("[INFO] INTENTO "+(j+1)+" PARA QUE EL SPINNER NO SE MUESTRE.");
								try{ 
									this.navigateTo(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST
											+ publicationsImpl.get(i).getId());
									this.ctrlLoadPost();
									pubsNew = this.publicationCommentSectionClick();
									System.out.println("[INFO] SPINNER ACTIVE?...");
									this.waitUntilNotSpinnerLoading();
									j=3;
								}catch(Exception e1) {
									if(e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")){
										System.out.println("[WARN] NO SE CARGÓ LA SECCIÓN COMETNARIOS! SPINNER ACTIVE!");
										this.saveScreenShot("WARN_SPINNERLOAD");
									}else {
										e1.printStackTrace();
										throw e;
									}
								}
								
							}
						}else {
							throw e;
						}
					}
					if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER) && pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed()) {
						publicationsImpl.get(i).setComments(this.extractPubComments(pubsNew.get(0), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN));
					}else {
						System.out.println("[WARN] LA PUBLICACION NO TIENE COMENTARIOS");
					}
					
					this.page.setPublications(publicationsImpl);

				} catch (Exception e) {
					System.out.println("[ERROR] AL ACCEDER AL POST.");
					this.saveScreenShot("ERR_ACCESO_POST");
					throw e;
				}
			}

			return this.page;
		} else {
			System.out.println("[INFO] NO SE ENCONTRARON PUBLICACIONES PARA PROCESAR.");
			return null;
		}
		
	}
	
	public void zoomOut() {
		/*WebElement html = this.getDriver().findElement(By.tagName("html"));
		html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		html.sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
		*/
		
		/*JavascriptExecutor js = (JavascriptExecutor) this.getDriver();
		js.executeScript("document.body.style.zoom='60%'");
		*/
		
		Robot robot;
		try {
			robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_MINUS);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_MINUS);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_MINUS);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_MINUS);
			//this.saveScreenShot(name);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public void ctrlLoadPost() throws Exception{
		try{
			this.waitForPublicationsLoaded(this);
			if(this.getAccess()==null) {
				try {
					if(this.waitUntilPopupLoginAppears(this)){
						this.checkAndClosePopupLogin();
					}
				}catch(Exception e) {
					if(e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")){
						System.out.println("[WARN] TIEMPO ESPERA POPUPLOGIN AGOTADO");
					}else{
						throw e;
					}
				}
			}
			
			this.saveScreenShot("PostLoaded");
		}catch(Exception e) {
			System.err.println("[ERROR] NO SE PUDO ACCEDER AL POST");
			throw e;
		}
	}
	public List<WebElement> publicationCommentSectionClick() throws Exception{
		List<WebElement> pubsNew = this.getDriver()
				.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER + "[1]"));
		if (this.getAccess() == null) {
			if (this.existElement(pubsNew.get(0), FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)) {
				try {	
					this.waitUntilCommentSectionVisible(pubsNew.get(0));
					try{
						this.saveScreenShot("antesClickCommentSection_NL_1");
						pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();
						return pubsNew;
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							//this.overlayHandler(); //ESTO ES SOLO PARA LOGIN
							if((Boolean)((JavascriptExecutor) this.getDriver()).executeScript("return document.documentElement.scrollHeight>document.documentElement.clientHeight;")) {
								this.scrollDown();
							}else {
								System.out.println("[INFO] LA VENTANA NO TIENE SCROLL");
							}
							this.checkAndClosePopupLogin();
							this.saveScreenShot("antesClickCommentSection_NL_2");
							pubsNew.get(0).findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).click();
							return pubsNew;
						}
					}
					
				}catch(Exception e) {
					System.err.println("[ERROR] ACCESO A SECCION COMENTARIOS DE LA PUBLICACIÓN");
					this.saveScreenShot("ERR_ACCESO_COMM_PUB");
					//return pubsNew;
					throw e;
				}
				
			}
		}
		
		return pubsNew;
		
	}
	
	public boolean waitUntilPopupLoginAppears(final FacebookScrap fs) {
		ExpectedCondition<Boolean> commentSection = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	    		if(driver.findElements(By.xpath("//a[@id='expanding_cta_close_button']")).size()>0 && driver.findElement(By.xpath("//a[@id='expanding_cta_close_button']")).isDisplayed()) {
	            	//System.out.println("true");
	    			return true;
	            }else {
	            	JavascriptExecutor jsx = (JavascriptExecutor) driver;
	        		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	        		return false;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofMillis(500));
				//.ignoring(StaleElementReferenceException.class);

		return wait.until(commentSection);
	}

	
	public boolean waitUntilCommentSectionVisible(final WebElement pub) {
		ExpectedCondition<Boolean> commentSection = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	    		if(pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isDisplayed() && pub.findElement(By.xpath(FacebookConfig.XPATH_COMMENTS_CONTAINER_NL)).isEnabled()) {
	            	//System.out.println("true");
	    			JavascriptExecutor jsx = (JavascriptExecutor) driver;
	        		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	            	return true;
	            }else {
	            	JavascriptExecutor jsx = (JavascriptExecutor) driver;
	        		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	        		return false;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofMillis(500));
				//.ignoring(StaleElementReferenceException.class);

		return wait.until(commentSection);
	}

	public List<Comment> extractPubComments(WebElement pub, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) throws Exception{
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

	public List<WebElement> inicializePublicationsToBeLoad(String facebookPage, Long uTIME_INI, Long uTIME_FIN) throws Exception{

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
	

	public List<WebElement> processPagePosts(String facebookPage, Long uTIME_INI, Long uTIME_FIN) throws Exception{
		try {

			this.overlayHandler();
			this.goToPublicationsSection();
			//this.waitForPageLoaded();
			try {
				try{
					System.out.println("[INFO] SPINNER ACTIVE?...");
					this.waitUntilNotSpinnerLoading();
				}catch(Exception e1) {
					if(e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
					}
				}
				this.waitForPublicationsLoaded(this);
			}catch(Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")){
					System.out.println("[WARN]Tiempo espera carga publicaciones agotado");
					this.saveScreenShot("ERR_ESPERA_CARGA_PUBS");
					throw e;
				}else {
					throw e;
				}
				
			}
			//this.saveScreenShot("PubsLoadead");
			System.out.println("[INFO] BUSCANDO PUBLICACIONES ENTRE EL RANGO DE FEHCAS DADA....");
			if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) {
				while (!((this.getDriver()
						.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookPage, uTIME_INI)))
						.size()) > 0)) {
					try {
						this.waitUntilShowMorePubsAppears(this);
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")){
							System.out.println("[WARN] TimeoutException. Waiting ShowmorePublications button");
						}else {
							e.printStackTrace();
							throw e;
						}
						
					}
					
					if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
						this.scrollMainPublicationsPage();
						try{
							System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
						}catch(Exception e1) {
							if(e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
								System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
							}
						}
					} else {
						this.saveScreenShot("posts");
						System.out.println(
								"[INFO] YA SE RECORRIERON TODAS LAS PUBLICACIONES DE LA PÁGINA. NO SE ENCONTRÓ BTN SHOW MORE: "
										+ FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
						break;
					}
					System.out.print("...|");
				}
				//this.saveScreenShot("posts");
				System.out.println("|FIN|");
			} else {
				System.err
						.println("[INFO] LA PAGINA NO TIENE NUNGUNA PUBLICACION o NO TUVO EL TIEMPO PARA CARGARSE");
				this.saveScreenShot("PAGINA_SIN_PUBS");
				return null;
			}
		} catch (Exception e) {
			System.err.println("[ERROR] EN LA CARGA DE PUBLICACIONES.");
			e.printStackTrace();
			this.saveScreenShot("ERR_CARGA_PUBS");
			throw e;
		}
		
		// RETORNO SOLO LAS PUBLICACIONES QUE CUMPLIERON CON EL FILTRO.
		return this.filterPostsByUTIME(facebookPage, uTIME_INI, uTIME_FIN);

	}
	
	public boolean waitUntilNotSpinnerLoading(){
	    ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	            if(driver.findElements(By.xpath("//span[@role='progressbar']")).size()>0 && driver.findElement(By.xpath("//span[@role='progressbar']")).isDisplayed()) {
	            	//System.out.println("true spinner!");
	            	return false;
	            }else {
	            	return true;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SPINNER))
				.pollingEvery(Duration.ofMillis(1000));

		return wait.until(morePubsLink);    
	    
	}
	
	public boolean waitForPublicationsLoaded(final FacebookScrap fs) {
		ExpectedCondition<Boolean> pubsLoaded = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if ((driver.findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size() > 0) && (driver.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER+"[1]")).isDisplayed()) && (fs.waitForJStoLoad())) {
					System.out.println("Container publications TRUE");
					fs.saveScreenShot("container_pub_true");
					return true;
				}else {
					System.out.println("Container publications FALSE");
					fs.scrollMainPublicationsPage();
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofSeconds(1))
				.ignoring(StaleElementReferenceException.class)
				.ignoring(NoSuchElementException.class);
		
		return wait.until(pubsLoaded);	
		
	}
	
	
	public boolean waitUntilShowMorePubsAppears(final FacebookScrap fs){
	    ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	            if(fs.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE)) {
	            	//System.out.println("true");
	            	return true;
	            }else {
	            	fs.scrollMainPublicationsPage();
	            	return false;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofMillis(1000));

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

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(5))
				.pollingEvery(Duration.ofSeconds(1));

		return wait.until(overlayClosed);

		
		
		
		
	}

	public void goToPublicationsSection() throws Exception{
		
		try{ 
			this.waitUntilPublicationsMenuOption();
		
			if (this.getAccess() == null) {
				try {
					String posts = this.getDriver().findElement(By.xpath(
							"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a"))
							.getAttribute("href");
					// ACcedo por la URL, puede que el elemento nunca me quede visible.
					this.navigateTo(posts);
				}catch(Exception e) {
					System.err.println("[ERROR] ACCEDIENDO A LA URL DEL POST");
					throw e;
				}
			} else {
				try {
					// Accedo por el click en el menú opción "Publicaciones"
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(true);",
					this.getDriver().findElement(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")));
					this.getDriver().findElement(By.xpath(
							"//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a"))
							.click();
	
				} catch (Exception e) {
					System.err.println("[ERROR] NO SE PUDO ACCEDER AL MENÚ 'PUBLICACIONES'");
					this.saveScreenShot("ERR_ACCEDER_PUBLICACIONES");
					throw e;
				}
	
			}
		}catch(Exception e) {
			System.err.println("No tiene opción de menu 'publicaciones'.");
		}
	}
	
	public boolean waitUntilPublicationsMenuOption() {

	    ExpectedCondition<Boolean> commentLink = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	            if(driver.findElements(By.xpath("//div[@id='entity_sidebar']//descendant::div//descendant::div[@data-key='tab_posts']//descendant::a")).size()>0) {
	            	return true;
	            }else {
	            	return false;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofMillis(1000))
				.ignoring(TimeoutException.class);

		return wait.until(commentLink);
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
			System.out.println("[WARN] NO SE ENCONTRARON PUBLICACIONES EN LAS FECHAS INDICADAS." + " INICIO:" +uTIME_INI + " FIN:"+uTIME_FIN);
			return null;
		}
	}

	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
		//this.waitForJStoLoad();
		//try {
			//Thread.sleep(1000);
		//} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			//System.out.println("[ERROR] NO SE PUDO HACER LA ESPERA THREAD.SLEEP");
		//}
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
	public List<Comment> obtainAllPublicationComments(WebElement container, String xPathExpression,Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN) throws Exception{
		List<WebElement> comentarios = new ArrayList<WebElement>();
		List<Comment> comments = new ArrayList<Comment>();
		if (container.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:" + container
					.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
		}
		// Si existe el botón de "Ver Más mensajes"
		// if (container.findElements(By.xpath(xPathExpression)).size() > 0) {
		
		try{
			try{
				System.out.println("[INFO] SPINNER ACTIVE?...");
				this.waitUntilNotSpinnerLoading();
			}catch(Exception e1) {
				if(e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
				}
			}
			this.waitUntilShowMoreCommAppears(this, container, xPathExpression);
			
		}catch(Exception e) {
			if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
				
				System.out.println("[WARN] TIEMPO DE ESPERA APARICION BOTON SHOW MOORE COMMENTS EXCEDIDO.");
				
			}else {
				//e.printStackTrace();
				this.saveScreenShot("SM_primeraVez_");
				throw e;
			}				
		}
		
		
		if (this.existElement(container, xPathExpression)) {
			WebElement showMoreLink;
			
			try {
				while(this.waitUntilShowMoreCommAppears(this, container, xPathExpression)) {
					showMoreLink = container.findElement(By.xpath(xPathExpression));
					try {
						//this.moveTo(showMoreLink);
						showMoreLink.click();
					}catch(Exception e){
						if(e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							//this.saveScreenShot("SM_ElementClickIntercepted");
							this.getActions().sendKeys(Keys.ESCAPE).perform();
							this.overlayHandler();
							this.checkAndClosePopupLogin();
							System.out.println("[INFO] SPINNER ACTIVE?...");
							this.waitUntilNotSpinnerLoading();
							this.scrollDown();
							this.waitUntilShowMoreCommAppears(this, container, xPathExpression);
							showMoreLink.click();
						}else if(e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
							System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
						}else {
							throw e;
						}
					}
				}
			}catch(Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					this.saveScreenShot("tiemoutexception_SM");
					System.out.println("[WARN] TIEMPO DE ESPERA SHOW MORE MESSAGES LINK EXCEDIDO.");
				}else if(e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
					System.out.println("[WARN] La referencia al botón ShowMore Comments desapareció.");
				}else if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")){
					System.out.println("[WARN] Desapareció el botón de ShowMore Comments. [no such element exception]");
				}else {
					this.saveScreenShot("exception_SM_1");
					e.printStackTrace();
					throw e;
				}
				
			}
				
		} else {
			System.out.println("NO HAY MÁS MENSAJES PARA CARGAR.");
			this.saveScreenShot("NOHAYMASMENSAJESCARGA");
		}

		System.out.println("[INFO] TOTAL COMENTARIOS LEIDOS: "
				+ container.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size());
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
		System.out.print("[INFO]PROCESANDO COMENTARIO: ");
		for (int j = 0; j < comentarios.size(); j++) {
			comments.add(this.extractCommentData(comentarios.get(j)));
			System.out.print(j + "|");
		}
		System.out.println("[INFO] SE PROCESARON TODOS LOS COMENTARIOS. (" + comentarios.size() + ")");
		return comments;
	}
	
	public boolean waitUntilShowMoreCommAppears(final FacebookScrap fs, final WebElement component, final String xpath){
	    ExpectedCondition<Boolean> commentLink = new ExpectedCondition<Boolean>() {
	    	public Boolean apply(WebDriver driver) {
	            if(fs.existElement(component, xpath) && component.findElement(By.xpath(xpath)).isDisplayed()) {
	            	//System.out.println("existe show more comments.!");
	            	return true;
	            }else {
	            	//if(fs.getAccess()==null) {
	            		//fs.checkAndClosePopupLogin();
	            	//}else {
	            		//fs.overlayHandler();
	            		//fs.getActions().sendKeys(Keys.ESCAPE).perform();
	            	//}
	            	//fs.scrollDown();
	            	//System.out.println("no existe show more comments.!");
	            	return false;
	            }
	        }
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofMillis(500));
				//.ignoring(StaleElementReferenceException.class);

		return wait.until(commentLink);
	}

	public void totalCommentsPub(WebElement pub) {
		if (pub.findElements(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).size() > 0) {
			System.out.println("COMENTARIOS QUE SE INDICA EN EL POST:"
					+ pub.findElement(By.xpath("//div[@class='UFIRow UFIShareRow']/node()/node()[2]/span")).getText());
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
			auxComment.setUserId(
					pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));

		}

		// Utime
		auxComment.setUTime(
				comentario.findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime"));
		return auxComment;
	}

	public boolean waitForJStoLoad() {
		ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (((JavascriptExecutor) driver).executeScript("return document.readyState").toString()
						.equals("complete")) {
					//System.out.println("[INFO] DocumentReady");
					
					return true;
				} else {
					System.out.println("[INFO] DocumentReadyState UNCOMPLETE");
					return false;
				}

				// return (Boolean)((JavascriptExecutor)driver).executeScript("return
				// jQuery.active == 0");
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);

		return wait.until(jsLoad);
	}

	public void moveTo(WebElement element) {
		// Se fija si al hacer un scroll, antes o despues, aparea el botn de cerrar.
		//if (this.getAccess() == null) {
			//this.checkAndClosePopupLogin();
		//}

		if (this.getAccess() == null) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView({block: \"start\"});",
					element);
		} else {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", element);
			
		}
		//try {
			//Thread.sleep(100);
		//} catch (InterruptedException e1) {
			//System.out.println("[ERROR] NO SE PUDO HACER LA ESPERA THREAD.SLEEP");
		//}
	}

	public void checkAndClosePopupLogin() {
		if ((this.existElement(null, "//a[@id='expanding_cta_close_button']") && 
				this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).isDisplayed()) /*|| 
			this.existElement(null, "//div[@id='u_0_c']") && 
				this.getDriver().findElement(By.xpath("//div[@id='u_0_c']")).isDisplayed()*/) {
			try {
				this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
				System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
					this.scrollDown();
					this.getDriver().findElement(By.xpath("//a[@id='expanding_cta_close_button']")).click();
					System.out.println("[INFO] SE CERRÓ POPUP LOGIN.");
				}else {
					this.saveScreenShot("ERRCLOSEPOPUPLGIN");
					e.printStackTrace();
				}
				
			}
		}//else {
			//System.out.println("[INFO] NO SE DETECTÓ POPUP LOGIN ABIERTO.");
		//}
	}

	public Publication extractPublicationData(WebElement publication) {
		Publication aux = new Publication();
		
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
				((JavascriptExecutor) this.getDriver())
						.executeScript("arguments[0].scrollIntoView({block: \"start\"});", element);
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
			/*for (int j = 0; j < page.getPublications().size(); j++) {
				System.out.println("============== PUBLICATION " + (j + 1) + " INICIO	===============");
				System.out.println(page.getPublications().get(j).toString());
				System.out.println("************** PUBLICATION " + (j + 1) + " FIN	***************");
			}*/
			
			for (int j = 0; j < page.getPublications().size(); j++) {
				System.out.println("============== PUBLICATION " + (j + 1) + " INICIO	===============");
				System.out.println("TOTAL COMENTARIOS: " + page.getPublications().get(j).getComments().size());
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
				if (status.equals("complete")) {
					//System.out.println("Estado pagina: " + status);
					return true;
				} else {
					//System.out.println("Estado pagina: " + status);
					return false;
				}
			}
		};

		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
				.pollingEvery(Duration.ofSeconds(1));

		return wait.until(jsLoad);
		
	}
	
	
	
	

	// "opt=1: Comentarios Relevantes" --> Es el filtro por default que tienen todos
	// los posts de Facebook.
	// "opt=2: Más Recientes" --> Cuando se actualice un post
	// "opt=3: Comentarios Relevantes(no filtrados)" --> TODOS los comentarios
	private void TipoCargaComentarios(WebElement Post, int option) throws Exception{
		try {
			/*if(this.getAccess() == null) {
				this.scrollDown();
			}*/
			// Puede que se abra automáticamente el Enviar mensajes al dueño de la página.
			if (this.getAccess() != null) {
				if (this.getDriver().findElements(By.xpath("//a[@class='_3olu _3olv close button _4vu4']"))
						.size() > 0) {
					this.getDriver().findElement(By.xpath("//a[@class='_3olu _3olv close button _4vu4']")).click();
				}
			}
			
			/*if (this.getAccess() == null) {
				this.checkAndClosePopupLogin();
			}*/
			
			
			
			try {
				try{
					this.waitUntilMenuOptionAppears(this, Post);
				}catch(Exception e) {
					if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						System.out.println("[WARN] TIEMPO DE ESPERA A QUE APAREZCA LINK TIPO CARGA AGOTADO");
					}else {
						throw e;
					}
				}
				if(this.getAccess()!=null) {
					this.moveTo(Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")));
				}
				
				try{ 
					this.saveScreenShot("Antes_Sel_TipoCargAComentarios");
					Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).click();
				}catch(Exception e){
					if(e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException") || e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
						this.overlayHandler();
						this.checkAndClosePopupLogin();
						this.scrollDown();
						if(this.getDriver().findElements(By.xpath("//span[@role='progressbar']")).size()>0 && this.getDriver().findElement(By.xpath("//span[@role='progressbar']")).isDisplayed()) {
							try{
								System.out.println("[INFO] SPINNER ACTIVE?...");
								this.waitUntilNotSpinnerLoading();
								this.scrollDown();
							}catch(Exception e1) {
								if(e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
									System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
								}
							}
						}
						Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).click();
					}else {
						this.saveScreenShot("ERR_Sel_OptionPpal");
						throw e;
					}
				}
				
				/*if(this.getAccess()==null) {
					this.scrollDown();
				}*/
				//this.saveScreenShot("Sccrolldown");
				if(this.waitUntilMenuAppears()) {
					WebElement menuOption = this.getDriver().findElement(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li["+ option + "]"));
					/*if(this.getAccess()!=null) {
						this.moveTo(menuOption);
					}*/
					
					//this.checkAndClosePopupLogin();
					try{
						menuOption.click();
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException")) {
							this.overlayHandler();
							this.checkAndClosePopupLogin();
							this.scrollDown();
							this.saveScreenShot("ListOpt_TIPOCARGA");
							//Post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).click();
							try{
								this.waitUntilMenuAppears();
								this.scrollDown();
								this.saveScreenShot("opt_TIPOCARGA");
								menuOption.click();
							}catch(Exception e1) {
								if(e1.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
									System.out.println("[WARN] no se mostraron las opciones de tipo carga");
								}else {
									throw e1;
								}
							}
							
						}else {
							this.saveScreenShot("ERR_Sel_menuOption");
							throw e;
						}
					}
				}
			} catch (Exception e) {
				System.err.println("[ERROR] ERROR SELECCIONANDO EL TIPO DE CARGA");
				this.saveScreenShot("ERROR_TIPOCARGA");
				e.printStackTrace();
				throw e;
			}
			
			this.waitForJStoLoad();
				
		} catch (Exception e) {
			System.err.println("[ERROR] NO SE PUDO HACER EL CLICK EN MOSTRAR TODOS LOS MENSAJES, SIN ORDENAMIENTO");
			this.saveScreenShot("ERR_NO_SELECCIONO_MOSTRAR_MENSAJES");
			e.printStackTrace();
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
		    		if(post.findElements(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).size()>0 
		    				&& post.findElement(By.xpath(".//div[contains(@class, 'UFIRow UFILikeSentence')]/descendant::a[@class='_p']")).isDisplayed()) {
		            	//System.out.println("existe show more comments.!");
		            	
		    			return true;
		            }else {
		            	
		            	//if(fs.getAccess()==null) {
		            		//fs.checkAndClosePopupLogin();
		            	//}
		            	//System.out.println("no existe show more comments.!");
		            	return false;
		            }
		        }
			};

			Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
					.pollingEvery(Duration.ofMillis(500));
					//.ignoring(StaleElementReferenceException.class);

			return wait.until(menuAppears);
	}
	
	public boolean waitUntilMenuAppears() {
		 ExpectedCondition<Boolean> menuAppears = new ExpectedCondition<Boolean>() {
		    	public Boolean apply(WebDriver driver) {
		            if(driver.findElements(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li")).size()>0
		            		&& driver.findElement(By.xpath("//div[@class='uiContextualLayer uiContextualLayerBelowRight']/descendant::ul[@role='menu']/li")).isDisplayed()) {
		            	//System.out.println("existe show more comments.!");
		            	return true;
		            }else {
		            	//if(fs.getAccess()==null) {
		            		//fs.checkAndClosePopupLogin();
		            	//}
		            	//System.out.println("no existe show more comments.!");
		            	JavascriptExecutor jsx = (JavascriptExecutor)driver;
		        		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
		            	return false;
		            }
		        }
			};

			Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(this.WAIT_UNTIL_SECONDS))
					.pollingEvery(Duration.ofMillis(500));
					//.ignoring(StaleElementReferenceException.class);

			return wait.until(menuAppears);
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
