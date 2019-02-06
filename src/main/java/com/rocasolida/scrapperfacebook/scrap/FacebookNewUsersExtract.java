package com.rocasolida.scrapperfacebook.scrap;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.entities.User;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookLinkType;

public class FacebookNewUsersExtract extends Scrap {
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	
	private static Integer WAIT_UNTIL_SPINNER = 10;
	private static Integer WAIT_UNTIL_SECONDS = 5;
	//Cada cuanto el waiter chequea la condicion.
	private static Integer CHECK_DELAY_MS = 10;
	
	//Encontro la cantidad de comentarios objetivos
	private boolean encontroCantUsers = false;
	//hay mas publicaciones para extraer comentarios
	private boolean hayMasPubs = true;
	
	public FacebookNewUsersExtract(Driver driver, boolean debug) throws MalformedURLException {
		super(driver, debug);
	}
	//lo podria llevar al util
	public void login(Credential access) throws Exception {
		long tardo = System.currentTimeMillis();
		try {
			this.navigateTo(FacebookConfig.URL);

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
				} else {
					if (debug)
						System.out.println("[ERROR]Check Login Credentials! " + "usr: " + access.getUser());
					throw new Exception("[ERROR]Check Login Credentials! " + "usr: " + access.getUser());
				}
			}

			if (debug)
				System.out.println("[ERROR] No se cargó el botón de Login. Expression: " + FacebookConfig.XPATH_FORM_LOGIN);

		} finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("login tardo: " + tardo);
		}
	}
	
	public List<User> obtainUserProfileInformation(List<String> profilesUrl) throws Exception{
		long tardo = System.currentTimeMillis();
		try {
			User auxUser;
			List<User> lista = new ArrayList<User>();
			for(int i=0; i<profilesUrl.size(); i++) {
				try {
					auxUser = new User();
					auxUser.setUrlPerfil(profilesUrl.get(i));
					
					if(!profilesUrl.get(i).contains("?")) {
						this.navigateTo(profilesUrl.get(i)+FacebookConfig.URL_ABOUT_INFO_OVERVIEW);
					}else {
						//education
						this.navigateTo(profilesUrl.get(i)+FacebookConfig.URL_ABOUT_INFO_OVERVIEW_1);
					}
					
					if(debug) 
						System.out.println("Cargando URL: " + this.getDriver().getCurrentUrl());
					this.overlayHandler();
					
					auxUser = this.extractOverviewInfo(auxUser);
					
					/*
					 * 06/02/2019: el acceso haciendo el click en la opcion de menu, la hace tan rapido, que 
					 * te bloquean el usuario.
					try {
						if(this.existElement(null, "//a[contains(@href,'contact-info')]")) {
							this.getDriver().findElement(By.xpath("//a[contains(@href,'contact-info')]")).click();
							this.waitUntilNotSpinnerLoading();
						}else {
							System.out.println("No existe el elemento de contact info.");
						}
						
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
							System.out.println("[TIMEOUT] Espera para la carga de la seccion de CONTACT-INFO. Se dejará continuar...");
						}else {
							throw e;
						}
					}
					*/
					Thread.sleep(3000);
					if(!profilesUrl.get(i).contains("?")) {
						this.navigateTo(profilesUrl.get(i)+FacebookConfig.URL_ABOUT_INFO_BASICA);
					}else {
						this.navigateTo(profilesUrl.get(i)+FacebookConfig.URL_ABOUT_INFO_BASICA_1);
					}
					
					
					if(debug) 
						System.out.println("URL ACTUAL: "+ this.getDriver().getCurrentUrl());
					//this.overlayHandler();
					
					//Extraigo info básica
					auxUser = this.extractUserBasicInfo(auxUser);
					lista.add(auxUser);
				}catch(Exception e) {
					throw e;
				}
			}
			return lista;
		}finally{
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainUserProfileInformation tardo: " + tardo);
		}
	}
	
	private User extractOverviewInfo(User aux) {
		try {
			//ubicacion
			if(this.existElement(null, FacebookConfig.USER_PLACES)){
				aux.setUbicacion(this.getDriver().findElement(By.xpath(FacebookConfig.USER_PLACES)).getText());
			}
			//estudios
			List<String> estudios = new ArrayList<String>();
			if(this.existElement(null,FacebookConfig.USER_EDUCATION)){
				List<WebElement> estudiosAux = this.getDriver().findElements(By.xpath(FacebookConfig.USER_EDUCATION)); 
				if(estudiosAux.size()==1) {
					estudios.add(estudiosAux.get(0).getText());
				}else {
					estudios.add(estudiosAux.get(1).getText());
					aux.setEmpleo(estudiosAux.get(0).getText());
				}
				aux.setEstudios(estudios);
			}			
		}catch(Exception e){
			throw e;
		}
		return aux;
		
	}
	
	private User extractUserBasicInfo(User aux) throws Exception{
		try {
			//foto perfil
			if(this.existElement(null, FacebookConfig.USER_PIC)){
				aux.setUrlFotoPerfil(this.getDriver().findElement(By.xpath(FacebookConfig.USER_PIC)).getAttribute("src"));
			}
			//sexo
			if(this.existElement(null,FacebookConfig.USER_GENDER)){
				aux.setGenero(this.getDriver().findElement(By.xpath(FacebookConfig.USER_GENDER)).getText());
			}
			//Fecha_nac o edad
			if(this.existElement(null,FacebookConfig.USER_FECHANAC)){
				aux.setFechaNac(this.getDriver().findElement(By.xpath(FacebookConfig.USER_FECHANAC)).getText());
			}
			//ubicacion
			if(this.existElement(null,FacebookConfig.USER_UBICACION)){
				aux.setUbicacion(this.getDriver().findElement(By.xpath(FacebookConfig.USER_UBICACION)).getText());
			}
			
		}catch(Exception e){
			throw e;
		}
		return aux;
	}
	
	/**
	 * toma una page y la cantidad de usuarios a extraer de los comentarios de todas las publicaciones necesarias. 
	 * Devuelve la lista de urls de los usuarios que pudo extraer.
	 * Devuelve SIEMPRE los que encuentra. Por mas que haya un error devuelve lso scrapeados, e informando el error.
	 * @param facebookPage
	 * @param cantUsuarios
	 * @return lista de urls de cada usuario.
	 * @throws Exception
	 */
	public List<String> obtainUsersCommentInformation(String facebookPage, int cantUsuarios, String postUrl) throws Exception {
		List<String> users = new ArrayList<String>();
		List<Publication> publicaciones = new ArrayList<Publication>();
		//Por las dudas me guardo todas las pubs procesadas...
		List<Publication> auxPubs = new ArrayList<Publication>();
		long tardo = System.currentTimeMillis();
		try {
			do{
				
				if(postUrl == null) {
					//Verifico que sea link de pagina, y cargo la pagina..
					this.loadPage(facebookPage);
					//Obtengo las publicaciones a procesar...
					//La segunda vez, toma como referencia el ultimo utime, para procesar a partir de esa publicacion 
					//a las de mas abajo.
					auxPubs = this.loadPublicationsToBeProcessed(publicaciones.isEmpty()?null:publicaciones.get(publicaciones.size()-1));
				}else {
					Publication aux = new Publication();
					aux.setUrl(postUrl);
					auxPubs.add(aux);
				}
				
				for(int i=0; i<auxPubs.size();i++) {
					this.navigateTo(auxPubs.get(i).getUrl());
					this.waitUntilPublicationLoad();
					users = this.processPublicationComments(users, cantUsuarios);
					if(this.encontroCantUsers){
						break;
					}
					
					System.out.println("se procesó la publicacion: "+(i+1)+": "+auxPubs.get(i).getUrl());
				}
				
				//Si recorri las publicaciones de la lista que me pasaron a buscar... entonces, me quedo sin publicaciones.
				//Caso contrario, tengo que ir a buscar mas publicaciones...
				if(postUrl != null) {
					this.hayMasPubs=false;
				}
				publicaciones.addAll(auxPubs);
			
			}while(!encontroCantUsers && hayMasPubs);
			
			if(!hayMasPubs)
				System.out.println("[WARN] Se recorrieron todas las publicaciones.");
			
			if(!encontroCantUsers)
				System.out.println("[WARN] No se encontró la cantidad de usuarios objetivo. ("+cantUsuarios+")");
			
			System.out.println("[INFO] Se encontraron: "+ users.size() +" usuarios nuevos.");
			
			return users;
		}catch(Exception e) {
			if(users.size()>0) {
				System.err.println("Se detecto error, pero se encontraron "+ users.size() +" usuarios nuevos...");
				e.printStackTrace();
				return users;
			}else {
				throw e;
			}
		}finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainUsersCommentInformation tardo: " + tardo);
		}
		
	}
	
	/**
	 * Extrae de los elementos html de cada publicacion, su URL y uTime. Devuelve una lista de objetos publication.
	 * @param pubsHtml
	 * @return
	 */
	private List<Publication> extractPublicationsInfo(List<WebElement> pubsHtml) {
		List<Publication> pubs = new ArrayList<Publication>();
		String postID="";
		if(debug)
			System.out.println("[INFO] SE PROCESARAN "+pubsHtml.size()+" PUBLICACIONES");
		
		for(int i=0; i<pubsHtml.size(); i++) {
			Publication aux = new Publication();
			try {
				 postID = this.regexPostID(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
			}catch(Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
					postID = this.regexPostID(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_LINK)).getAttribute("href"));
					
				}
			}
				
			
			if (postID == "") {
				if (debug)
					System.out.println("[INFO] ERROR AL ENCONTRAR EL ID DEL POST: " + pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
			} else {
				aux.setId(postID);
				aux.setUrl(FacebookConfig.URL + postID);
			}
			
			
			if (this.existElement(pubsHtml.get(i), FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
				aux.setUTime(Long.parseLong(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
			} else if (this.existElement(pubsHtml.get(i), FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) {
				aux.setUTime(Long.parseLong(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).getAttribute("data-utime")));
			}
			
			pubs.add(aux);
		}
		
		return pubs;
	}
	
	
	//Este lo podria llevar al 'util'
	private boolean waitUntilNotSpinnerLoading() {
		ExpectedCondition<Boolean> morePubsLink = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//span[@role='progressbar']")).size() > 0 && driver.findElement(By.xpath("//span[@role='progressbar']")).isDisplayed()) {
					System.out.println("[INFO] Se esta cargando el contenido...");
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SPINNER)).pollingEvery(Duration.ofMillis(CHECK_DELAY_MS)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(morePubsLink);
	}
	
	//lo podria llevar al util
	private FacebookLinkType facebookLinkType() throws Exception{
			try {
				if (this.getDriver().findElements(By.xpath("//div[@id='entity_sidebar']//div//div[@data-key='tab_posts' or @data-key='tab_community' or @data-key='tab_home']//descendant::a")).size() > 0) {
					return FacebookLinkType.PAGE;
				}
				// Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
				if (this.getDriver().findElements(By.xpath("//div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a")).size() > 0) {
					return FacebookLinkType.PROFILE;
				}
			} catch (Exception e) {
				this.saveScreenShot("ERR_COMPR_LINK");
				throw new Exception("[ERROR] AL COMPROBAR TIPO DE LINK (PAGINA | PERFIL)");
			}
			
			return null;
		}
	
	//controla que sea una pagina y la carga......
	private void loadPage(String facebookPage) throws Exception {
		long aux = System.currentTimeMillis();
		try {
			this.navigateTo(FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST); // SI NO TIRA ERROR DE CONEXIÓN O DE PAGINA INEXISTENTE...
			
			if (debug)
				System.out.println("[INFO] SE CARGÓ EL LINK: " + FacebookConfig.URL + facebookPage + FacebookConfig.URL_POST);
			
			// POR AHORA CHEQUEA SI ES PAGINA O PERFIL
			switch (this.facebookLinkType()) {
			case PROFILE:
				throw new Exception("[INFO] Es un Perfil.");
			case PAGE:
				System.out.println("[INFO] OK: el link provisto es de una pagina.");
				//Por ahí antes se podria cargar la espera del container de publications...
				break;
			default:
				throw new Exception("[WARNING] No se reconoce el tipo de página para hacer SCRAP. En LoadPublications().");
			}
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("LoadPublications con wait, tardo: " + aux);
		}
	}
	
	
	/**
	 * Carga las publicaciones sobre las cuales se scrapearan sus comentarios.
	 * Toma como referencia el ultimo uTime procesado, para asegurarse de traer nuevas publicaciones.
	 * @param lastPubProcessed
	 * @return
	 * @throws Exception
	 */
	private List<Publication> loadPublicationsToBeProcessed(Publication lastPubProcessed) throws Exception{
		List<WebElement> pubs = new ArrayList<WebElement>();
		int intentosCargaPubs=0;
		int CANT_INTENTOS = 3;
		
		this.hiddenOverlay();
		
		
		if(lastPubProcessed == null) {
			do{
				//Entonces cargo las publiciones que me trae la pagina
				try {
					if (debug)
						System.out.println("[INFO] espera a la carga de las publicaciones de la pagina...");
					this.waitUntilNotSpinnerLoading();
					if(!(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()>0)) {
						this.hayMasPubs = false;
						break;
					}
				} catch (Exception e1) {
					if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEDIDO.");
						//Si excedio el tiempo de espera, vuelvo a intentar...
						intentosCargaPubs++;
					}else {
						System.out.println("[ERROR] al cargar la pagina.");
						throw e1;
					}
				}
			}while(!(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()>0) && intentosCargaPubs< CANT_INTENTOS);
			if(intentosCargaPubs == CANT_INTENTOS) {
				throw new Exception("[WARN] se espero " + CANT_INTENTOS + "veces ("+(WAIT_UNTIL_SPINNER*CANT_INTENTOS)+" seg.) a que cargue las publicaciones de la pagina ppal. Volver a intentar mas tarde!.");
			}
			
			pubs=this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
		
		}else {
			//Tengo que cargar a partir de la ultima procesada...
			//Se supone que las primeras publicaciones que cargie, no van a aplicar al filtro...
			if(debug)
				System.out.println("FILTRO pubs aplicado: " +"//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@class,'f_1jzqrr12pf j_1jzqrqwrre')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]");
				//System.out.println("FILTRO pubs aplicado: " + FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(null, lastPubProcessed.getUTime()));
			
			while(!((this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@class,'f_1jzqrr12pf j_1jzqrqwrre')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]")).size()) > 0)
					&& intentosCargaPubs< CANT_INTENTOS) {
				try {
					if(this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE)) {
						this.scrollMainPublicationsPage();
						//Espera de carga de publicaciones...
						this.waitUntilNotSpinnerLoading();
					}else {
						try {
							//Puede ser que no haya mas publicaciones o que se esten cargando las publicaciones...
							this.waitUntilShowMorePubsAppears(this);
						}catch(Exception e) {
							if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
								if (debug)
									System.out.println("[WARN] TimeoutException. Waiting ShowmorePublications Button...");
								intentosCargaPubs++;
								if(CANT_INTENTOS == intentosCargaPubs) {
									//Asumo que no hay mas publicaciones.
									this.hayMasPubs = false;
								}
							}
						}
					}
				} catch (Exception e) {
					if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TimeoutException. Esperando que aparezca el spinner de carga de publicaciones...");
						intentosCargaPubs++;
					} else {
						System.out.println("[ERROR] error al esperar carga de mas publications de la page.");
						throw e;
					}
				}
			}
			
			if(intentosCargaPubs == CANT_INTENTOS && hayMasPubs) {
				throw new Exception("[WARN] se espero " + CANT_INTENTOS + "veces ("+(WAIT_UNTIL_SPINNER*CANT_INTENTOS)+" seg.) a que carguen nuevas publicaciones de la pagina ppal. Vuelva a intentar mas tarde");
			}
			pubs=this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@class,'f_1jzqrr12pf j_1jzqrqwrre')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]"+"//ancestor::div[contains(@class,'userContentWrapper')]"));
			
		}
		
		return this.extractPublicationsInfo(pubs);
		
	}
	
	private boolean waitUntilShowMorePubsAppears(final FacebookNewUsersExtract fs) {
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
	//Lo podria llevar al util
	private boolean waitUntilMoreCommentsClickLoad() {
		ExpectedCondition<Boolean> loadMore = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS)).size() > 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(loadMore);
	}
	//lo podria llevar al util
	private boolean waitUntilMoreCommentsOverlayClickLoad() {
		ExpectedCondition<Boolean> loadMore = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS_1)).size() > 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(loadMore);
	}
	//lo podria llevar al util
	private boolean waitUntilMainPagePubsLoad(final int cantAnterior, final String xpathExpression) {
		ExpectedCondition<Boolean> loadMorePublications = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(xpathExpression)).size() > cantAnterior) {
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(200));
		return wait.until(loadMorePublications);
	}
	
	//lo podria llevar al util
	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}
	//lo podria llevar al util
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
	//lo podria llevar al util
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
	//lo podria llevar al util
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
	//lo podria llevar al util
	private void navigateTo(String URL) throws Exception {
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
				throw new Exception("[ERROR] NET ERROR ACCESS: " + this.getDriver().findElement(By.xpath("//body[@class='neterror']//div[@id='main-message']")).getText());
			}
			if (this.existElement(null, "//div[contains(@id,'globalContainer')]//a[contains(@href,'ref=404')]")) {
				/**
				 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente, ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost inexistente) - id post válido, pero URL inválida https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
				 */
				if (debug) {
					System.out.println("[ERROR] NO EXISTE LINK " + URL + ": " + this.getDriver().findElement(By.xpath("//div[contains(@id,'globalContainer')]//h2")).getText());
					this.saveScreenShot("NO_EXISTE_LINK");
				}
				throw new Exception("[ERROR] NO EXISTE LINK " + URL + ": " + this.getDriver().findElement(By.xpath("//div[contains(@id,'globalContainer')]//h2")).getText());
			}

		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("navigateTo tardo: " + aux);
		}
	}
	
	
	public List<String> processPublicationComments(List<String> users, int cantUsers) throws Exception {
		try {
			List<WebElement> pubComments = new ArrayList<WebElement>();
			int totUsersProcessed = users.size();
			boolean hayMasComentarios = true;
			
			this.hiddenOverlay();
			this.clickOnViewAllPublicationComments();
			
			do {
				if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size() > 0) {
					pubComments = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
				}else if(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_1)).size() > 0){
					pubComments = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_1));
				}else {
					pubComments = new ArrayList<WebElement>();
				}
				
				if(pubComments.size()==0) {
					if (debug)
						System.out.println("VER MAS COMENTARIOS DE LA PUBLICACION...");
					
					pubComments = this.cargarMasComentarios();
					if(pubComments == null) {
						if (debug)
							System.out.println("NO HAY MAS COMENTARIOS...");
						hayMasComentarios=false;
					}else {
						if (debug)
							System.out.println("SE CARGARON "+pubComments.size()+" COMENTARIOS NUEVOS...");
					}
				}
				
				if(pubComments != null) {
					for(int j=0; j<pubComments.size(); j++) {
						String auxUser = this.extractCommentUserProfileLink(pubComments.get(j));
						//Ya procese el comentario, entonces lo pongo en hidden.
						((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", pubComments.get(j));
						
						if(debug){
							if(auxUser==null) {
								System.out.println("EL USUARIO ES NULL. NO SE AGREGARÁ A LA LISTA");
							}
							
							if(users.contains(auxUser)) {
								System.out.println("EL USUARIO YA EXISTE EN LA LISTA");
							}
						}
						
						if(auxUser!=null && !users.contains(auxUser)) {
							users.add(auxUser);
							totUsersProcessed ++;
							System.out.println("Se encontro un usaurio nuevo. Total: " + totUsersProcessed);
						}
						
						if(totUsersProcessed == cantUsers) {
							this.encontroCantUsers = true;
							break;
						}
						
					}
				}
			}while(!this.encontroCantUsers && hayMasComentarios);
			return users;
		}finally {
			
		}
	}
	//Hace click en el link de cargar mas comentarios y espera a que carguen...
	private List<WebElement> cargarMasComentarios() {
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS)).size() == 1) {
			try {
				this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS)).click();
				this.waitUntilMoreCommentsClickLoad();
				return this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
				
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
					//No hay mas comentarios para cargar...
					return null;
				}else{
					System.out.println("Error al hacer click en VER MAS MENSAJES");
					throw e;
				}
			}
			
		}else if(this.getDriver().findElements(By.xpath("//div[@class='_6iiz _77br']//a[@class='_4sxc _42ft']")).size() == 1){
			try {
				this.getDriver().findElement(By.xpath("//div[@class='_6iiz _77br']//a[@class='_4sxc _42ft']")).click();
				// Poner un wait after click. (sumar al de extracción de comments...)
				this.waitUntilMoreCommentsOverlayClickLoad();
				return this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_1));
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
					//No hay mas comentarios para cargar...
					return null;
				}else{
					System.out.println("Error al hacer click en VER MAS MENSAJES OVERLAY");
					throw e;
				}
			}
			//pubComments = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		}else {
			return null;
		}
	}
	
	private void hiddenOverlay() {
		if(this.existElement(null,"//div[@class='_3ixn']")) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath("//div[@class='_3ixn']")));
			System.out.println("Se oculto el overlay");
		}
	}
	
	//click "Ver todos los mensajes" de la publicacion...
	public void clickOnViewAllPublicationComments() {
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_VIEW_ALL_PUB_COMMENTS_LINK)).size() > 0) {
			try {
				this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_VIEW_ALL_PUB_COMMENTS_LINK)).click();
				// Poner un wait after click. (sumar al de extracción de comments...)
				this.waitUntilMoreCommentsClickLoad();
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("ElementNotInteractableException") || e.getClass().getSimpleName().equalsIgnoreCase("ElementClickInterceptedException")) {
					System.out.println("Error al hacer click en VER TODOS LOS MENSAJES");
					this.hiddenOverlay();
					(new Actions(this.getDriver())).sendKeys(Keys.ESCAPE).perform();
					this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_VIEW_ALL_PUB_COMMENTS_LINK)).click();
					// Poner un wait after click. (sumar al de extracción de comments...)
					this.waitUntilMoreCommentsClickLoad();
				}else {
					throw e;
				}
				
			}
		}
	}
	
	public boolean waitUntilPublicationLoad() {
		//div[contains(@class,'uiContextualLayerParent')]
		ExpectedCondition<Boolean> overlayPubOpen = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//div[contains(@class,'uiContextualLayerParent')]")).size() > 0) {
					System.out.println("cargo el overlay de la publicacion...");
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(overlayPubOpen);
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
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(overlayClosed);
	}

	
	
	private String extractCommentUserProfileLink(WebElement comentario) throws Exception {
		// Usuario Profile Url
		String user = "";
		if (this.getAccess() != null) {
			try {
				user = comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE)).getAttribute("href");
				int pos = user.indexOf("fref=ufi&rc=p");
				if(pos>0) {
					user = user.substring(0, pos-1);
				}
				
				System.out.println("***USER: " + user);
				//return comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE)).getAttribute("href");
				return user;
			}catch(Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
					try {
						user = comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE_1)).getAttribute("href");
						int pos = user.indexOf("fref=ufi&rc=p");
						if(pos>0) {
							user = user.substring(0, pos-1);
						}
						
						//return comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE_1)).getAttribute("href");
						return user;
					}catch(Exception e1) {
						System.out.println("No se pudo extraer el ID del usuario.");
						return null;
					}
				}
			}
			
		}
		return null;
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
	

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

}
