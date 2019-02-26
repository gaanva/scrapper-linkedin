package com.rocasolida.scrapperfacebook.scrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.openqa.selenium.support.ui.WebDriverWait;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.entities.User;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookLinkType;
import com.rocasolida.scrapperfacebook.scrap.util.PublicationScrapper;

public class FacebookNewUsersExtract extends Scrap {
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	
	private static Integer WAIT_UNTIL_SPINNER = 10;
	private static Integer WAIT_UNTIL_SECONDS = 20;
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
	
	/**
	 * Le paso una lista de profilesUrl, las accede y le saca su información.
	 * @param profilesUrl
	 * @return
	 * @throws Exception
	 */
	public List<User> obtainUserProfileInformation(List<String> profilesUrl, int sleepSecondsTime) throws Exception{
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
					//this.overlayHandler();
					
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
					///////////////////////////////////////////////////////////TimeSleep segun lo ingresado por parametro./////////////////////////////////////////////////////////////
					if(debug)
						System.out.println("[INFO] SLEEP TIME DE " + sleepSecondsTime + " SEGUNDOS.");
					Thread.sleep(sleepSecondsTime*1000);
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
	 * Toma la url de un post, y scrapea solo la cantidad de usuarios de ese post particular.
	 * Devuelve SIEMPRE los que encuentra. Por mas que haya un error devuelve lso scrapeados, e informando el error.
	 * @param facebookPage
	 * @param cantUsuarios
	 * @param postUrl
	 * @return lista de urls de cada usuario.
	 * @throws Exception
	 */
	public List<String> obtainUsersInformationFromComment(String facebookPage, String postUrl, int cantUsuarios) throws Exception {
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
				//Va a ser null si la lsta de publicaciones que saca de un page es null, ya qye no hay más publicaciones...
				if(auxPubs!=null) {
					for(int i=0; i<auxPubs.size();i++) {
						this.navigateTo(auxPubs.get(i).getUrl());
						//this.waitUntilPublicationLoad();
						
						System.out.println("-------------------------------------------------->>>>>   se procesara la publicacion: "+(i+1)+": "+auxPubs.get(i).getUrl());
						
						//Revisar por qué captura mal la publicacion cuando es 1 sola... o cuando es un video!
						//List<WebElement> pubs = this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]"));
						
				        
						//Le paso un objeto que tiene las queries a ejecutar según el tipo de post...
						PublicationScrapper ps = null;
						try {
							ps =this.xpathPublicationQueries();
						}catch(Exception e) {
							if(e.getMessage().equalsIgnoreCase("PUB_EVENT")) {
								if(debug)
									System.err.println("[INFO] NO SE PROCESÓ LA PUBLICACION, YA QUE ES DE TIPO EVENTO: " + this.getDriver().getCurrentUrl());
								//Busco la próxima publicación.
								continue;
							}else {
								//Si es un tipo de publicacion no reconocido corta el proceso.
								throw e;
							}
						}
						users = this.processPublicationUSerComments(users, cantUsuarios, ps);
						if(this.encontroCantUsers){
							break;
						}
						
						
					}
					
					//Si recorri las publicaciones de la lista que me pasaron a buscar... entonces, me quedo sin publicaciones.
					//Caso contrario, tengo que ir a buscar mas publicaciones...
					if(postUrl != null) {
						this.hayMasPubs=false;
					}
					publicaciones.addAll(auxPubs);
				}
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

	
	
	public List<String> obtainSpecificsUsersInformationFromComments(List<String> userScreenNames, String postUrl) throws Exception{
		List<String> users = new ArrayList<String>();
		HashMap<String, String> usersHash = new HashMap<String, String>();
		
		long tardo = System.currentTimeMillis();
		try {
			
			//Controlo que me pasen bien los parámetros	
			if(postUrl == null || postUrl=="") {
				throw new Exception("[ERROR] Se debe ingresar el POST de la URL a scrappear.");
			}
			if(userScreenNames==null || userScreenNames.size()==0) {
				throw new Exception("[ERROR] Se debe ingresar la lista de users screen names que se desea encontrar.");
			}
			//Abro la publicacion...
			this.navigateTo(postUrl);
			//Valida que hayan publicaciones en pantalla
			//this.waitUntilPublicationLoad();
			
			//Empiezo a scrapear los comentarios
			//List<WebElement> pubs = this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]"));
			//Le paso las queries xpath que tiene que hacer...
			usersHash = this.processPublicationSpecificUSersComments(userScreenNames, this.xpathPublicationQueries());
			
			
			//Recorrer el hashmap y mostrar los null...	
			Iterator ite = usersHash.entrySet().iterator();

			if(debug)
				System.out.println("[INFO]  SE MUESTRA LA LISTA DE SCREEN NAMES Y SI FUERON ENCONTRADAS: ");
		    while(ite.hasNext()) {
		        Map.Entry e = (Map.Entry) ite.next();
		        System.out.println(e.getKey() + " -> "+e.getValue());
		        if(e.getValue()!=null) {
		        	//Si lo encontró, devuelve la url del perfil.
		        	users.add(e.getValue().toString());
		        }else {
		        	//Si no lo encontró, devuelvo el nombre del usuario.
		        	users.add("NO SE ENCONTRO: " + e.getKey().toString());
		        }

		    }	
				
			
		    if(!encontroCantUsers)
				System.out.println("[WARN] No se encontró la cantidad de usuarios objetivo.");
			
			
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
			
			
			if(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()>0) {
				pubs=this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
			}else if(intentosCargaPubs == CANT_INTENTOS && hayMasPubs) {
				throw new Exception("[WARN] se espero " + CANT_INTENTOS + "veces ("+(WAIT_UNTIL_SPINNER*CANT_INTENTOS)+" seg.) a que carguen nuevas publicaciones de la pagina ppal. Vuelva a intentar mas tarde");
			}else if(!hayMasPubs) {
				if(debug)
					System.out.println("[INFO] NO HAY MAS PUBLICACIONES");
				return null;
			}
			
			pubs=this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
		
		}else {
			//Tengo que cargar a partir de la ultima procesada...
			//Se supone que las primeras publicaciones que cargie, no van a aplicar al filtro...
			if(debug)
				System.out.println("FILTRO pubs aplicado: " +"//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]");
				//System.out.println("FILTRO pubs aplicado: " + FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(null, lastPubProcessed.getUTime()));
			
			//Busca las próximas 5 o más publicaciones
			while(!((this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]")).size()) > 5)
					&& intentosCargaPubs< CANT_INTENTOS && this.hayMasPubs) {
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
							}else if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
								//Si no se encuentra el boton de mostrar mas publicaciones, se asume que no hay más publicaciones en la pagina...
								if(debug)
									System.out.println("[INFO] NO HAY MÁS PUBLICACIONES EN LA PÁGINA");
								this.hayMasPubs = false;
							}else {
								
								throw e;
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
			
			if(this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]")).size()>0) {
				pubs=this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]//descendant::div[contains(@id,'subtitle')]//descendant::abbr[@data-utime<"+lastPubProcessed.getUTime()+"]"+"//ancestor::div[contains(@class,'userContentWrapper')]"));
			}else if(intentosCargaPubs == CANT_INTENTOS && hayMasPubs) {
				throw new Exception("[WARN] se espero " + CANT_INTENTOS + "veces ("+(WAIT_UNTIL_SPINNER*CANT_INTENTOS)+" seg.) a que carguen nuevas publicaciones de la pagina ppal. Vuelva a intentar mas tarde");
			}else if(!hayMasPubs) {
				if(debug)
					System.out.println("[INFO] NO HAY MAS PUBLICACIONES");
				return null;
			}
			
			
			
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
				if (driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS)).size() > 0 || driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS_1)).size() > 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(loadMore);
	}
	
	private boolean waitUntilShowPublicationsCommentsClickLoad(final WebElement pub, final PublicationScrapper ps) {
		ExpectedCondition<Boolean> loadMore = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (pub.findElements(By.xpath(ps.getXpath_publication_spinner_loader())).size() > 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(5)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
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
	
	
	/**
	 * Recibe una lista con los screenNameUsers que se desean rastrear.
	 * @param targetScreenNameUsers
	 * @return devuelve la lista de los encontrados.
	 * @throws Exception
	 */
	public HashMap<String, String> processPublicationSpecificUSersComments(List<String> targetScreenNameUsers, PublicationScrapper ps) throws Exception {
		try {
			//Lista de screenNames encontrados
			HashMap<String, String> usrsUrlProfileFound = new HashMap<String, String>();
			List<String> usrScreenNameFound = new ArrayList<String>();
			
			int totUsersProcessed = 0;
			int totAProcesar = targetScreenNameUsers.size();
			List<WebElement> pubComments = new ArrayList<WebElement>();
			boolean hayMasComentarios = true;
			
			WebElement pub = this.getDriver().findElements(By.xpath(ps.getXpath_publication_container())).get(0);
			
			System.out.println("[INFO] se buscaran "+targetScreenNameUsers.size()+" screen names en los comentarios de la publicacion..");
			
			if(ps.getXpath_publication_loaded()!=null) {
				this.waitUntilPublicationLoad(ps);
			}
			//this.hiddenOverlay();
			if(ps.getXpath_mostrar_comments()!=null) {
				this.clickOnViewAllPublicationComments(pub, ps);
			}
			
			//al hacer click en ver mas comentarios, carga todos los comentarios.
			this.pubShowAllComments(pub);
			
			//this.listAllPubComments(pub);
			do {
				if (pub.findElements(By.xpath(ps.getXpath_all_comments())).size() > 0) {
					pubComments = pub.findElements(By.xpath(ps.getXpath_all_comments()));
				}else {
					pubComments = new ArrayList<WebElement>();
				}
				
				if(pubComments.size()==0) {
					if (debug)
						System.out.println("VER MAS COMENTARIOS DE LA PUBLICACION...");
					
					pubComments = this.cargarMasComentarios(pub, ps);
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
						String commentUserScreenName = this.extractCommentUserScreenName(pubComments.get(j));
						/*String aux;
						if(targetScreenNameUsers.contains(commentUserScreenName)) {
							aux = this.extractCommentUserProfileLink(pubComments.get(j));
						}*/
						//Ya procese el comentario, entonces lo pongo en hidden.
						((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", pubComments.get(j));
						
						if(debug){
							if(commentUserScreenName==null) {
								System.out.println("EL NOMBRE EXTRAIDO DEL USUARIO ES NULL. NO SE AGREGARÁ A LA LISTA");
							}
							
							if(usrScreenNameFound.contains(commentUserScreenName)) {
								System.out.println("EL USUARIO YA EXISTE EN LA LISTA " + commentUserScreenName);
							}
						}
						//encontre un usuario buscado
						if(commentUserScreenName!=null) {
							String urlProfile;
							//si está en la lista de target a buscar... LO PROCESO. Acepta que pasen usuarios iguales.
							if(targetScreenNameUsers.contains(commentUserScreenName)) {
								//Lo sumo a la lista de encontrados
								usrScreenNameFound.add(commentUserScreenName);
								urlProfile = this.extractCommentUserProfileLink(pubComments.get(j));
								if(urlProfile != null) {
									usrsUrlProfileFound.put(commentUserScreenName, urlProfile);
									//Contabilizo el encontrado...
									totUsersProcessed++;
									//Quito la primer ocurrencia de la lista... puede que me pasen 2 o más iguales.
									targetScreenNameUsers.remove(commentUserScreenName);
									System.out.println("Se encontro el screen name: '"+commentUserScreenName+"' . Total: " + totUsersProcessed);
								}else {
									if(debug)
										System.err.println("[ERROR] Revisar por que no se pudo extraer la url de perfil. screen name: '" + commentUserScreenName+"'");
								}
							} 
							//--> esto si quiere encontrar más de 1 repetido, pero habría que recorrer todos los comments.
							/*else {
								//Si ese usr screen name, ya lo encontré, puede ser que sea repetido....
								//Extraigo el urlProfile
								urlProfile = this.extractCommentUserProfileLink(pubComments.get(j));
								//Me fijo si existe en la lista de urls...
								if(!usrUrlProfile.contains(urlProfile)) {
									//Hay screen names repetidos...
									System.out.println("[INFO] Este screen name("+commentUserScreenName+") se repite. [" + urlProfile + "]");
									System.out.println("[INFO] Se agrega a la lista de urlProfiles.");
									usrsUrlProfileFound.put(commentUserScreenName, urlProfile);
 								}
							}*/
						}
						
						if(totUsersProcessed == totAProcesar) {
							this.encontroCantUsers = true;
							break;
						}
						
					}
				}
			}while(!this.encontroCantUsers && hayMasComentarios);
			
			if(!this.encontroCantUsers) {
				for(int i=0; i<targetScreenNameUsers.size(); i++) {
					usrsUrlProfileFound.put(targetScreenNameUsers.get(i), null);
				}
			}
			
			return usrsUrlProfileFound;
		}finally {
			
		}
	}

	public List<String> processPublicationUSerComments(List<String> users, int cantUsers, PublicationScrapper ps) throws Exception {
		try {
			List<WebElement> pubComments = new ArrayList<WebElement>();
			int totUsersProcessed = users.size();
			boolean hayMasComentarios = true;
			//Cuando es un single post, trae más de 1, pero siempre se trabaja sobre el 1ero. El resto de los tipos de posts SIEMPRE trae 1.
			WebElement publication = this.getDriver().findElements(By.xpath(ps.getXpath_publication_container())).get(0);
			
			
			if(ps.getXpath_publication_loaded()!=null) {
				this.waitUntilPublicationLoad(ps);
			}
			
			if(ps.getXpath_mostrar_comments()!=null) {
				this.clickOnViewAllPublicationComments(publication, ps);
			}
			
			//Mostrar TODOS los comentarios y no solo los relevantes...
			this.pubShowAllComments(publication);
			
			do {
				if (publication.findElements(By.xpath(ps.getXpath_all_comments())).size() > 0) {
					pubComments = publication.findElements(By.xpath(ps.getXpath_all_comments()));
				}/*else if(publication.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_1)).size() > 0){
					pubComments = publication.findElements(By.xpath(FacebookConfig.XPATH_COMMENTS_1));
				}*/else {
					pubComments = new ArrayList<WebElement>();
				}
				
				if(pubComments.size()==0) {
					if (debug)
						System.out.println("VER MAS COMENTARIOS DE LA PUBLICACION...");
					pubComments = this.cargarMasComentarios(publication, ps);
					
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
								System.out.println("EL USUARIO YA EXISTE EN LA LISTA " + auxUser);
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
	private List<WebElement> cargarMasComentarios(WebElement publication, PublicationScrapper ps) {
		List<WebElement> verMasMsjsLink = publication.findElements(By.xpath(ps.getXpath_ver_mas_comments()));
		if (verMasMsjsLink.size() >= 1) {
			try {
				this.overlayHandler();
				verMasMsjsLink.get(0).click();
				this.waitUntilMoreCommentsClickLoad();
				//Cargaron los comentarios, y los devuelvo.
				//return this.getDriver().findElements(By.xpath(ps.getXpath_all_comments()));
				return publication.findElements(By.xpath(ps.getXpath_all_comments()));
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")){
					//No hay mas comentarios para cargar...
					return null;
				}else if(publication.findElements(By.xpath("//div[contains(@class,'_6hnh _7gq4')]")).size()>0){
					if(debug)
						System.out.println("[INFO]Se llegó al fin de mensajes relevantes.");
					return null;
				}else{
					System.out.println("Error al hacer click en VER MAS MENSAJES");
					throw e;
				}
			}
			
		}
		
		verMasMsjsLink = publication.findElements(By.xpath("//div[@class='_6iiz _77br']//a[@class='_4sxc _42ft']"));
		if(verMasMsjsLink.size() >= 1){
			try {
				verMasMsjsLink.get(0).click();
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
	
	//click "Ver todos los mensajes" de la publicacion...
	public void clickOnViewAllPublicationComments(WebElement pub, PublicationScrapper ps) {
		if (this.getDriver().findElements(By.xpath(ps.getXpath_mostrar_comments())).size() > 0) {
			try {
				pub.findElement(By.xpath(ps.getXpath_mostrar_comments())).click();
				// Poner un wait after click. (sumar al de extracción de comments...)
				this.waitUntilShowPublicationsCommentsClickLoad(pub, ps);
			} catch (Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					//do nothing
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
					System.out.println("ca vosrgo el overlay de la publicacion...");
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
	
	//extrae el nombre visual del usuario
	private String extractCommentUserScreenName(WebElement comentario) throws Exception {
		// Usuario Profile Url
		String userScreenName = "";
		if (this.getAccess() != null) {
			try {
				userScreenName = comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE)).getText();
				
				System.out.println("***USER: " + userScreenName);
				//return comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE)).getAttribute("href");
				return userScreenName;
			}catch(Exception e) {
				if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException")) {
					try {
						userScreenName = comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE_1)).getText();
						//return comentario.findElement(By.xpath("."+FacebookConfig.XPATH_USER_URL_PROFILE_1)).getAttribute("href");
						return userScreenName;
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
	
	/**
	 * Hace click en la opcion de mostrar "todos los comentarios" de la publicacion.
	 * Si no puede, toma los que se listen...
	 * @param publication
	 */
	private void listAllPubComments(WebElement publication) {
		if(publication.findElements(By.xpath("."+"//div[contains(@class,'uiPopover _6a _6b openToggler')]/a")).size()==1) {
			publication.findElement(By.xpath("."+"//div[contains(@class,'uiPopover _6a _6b openToggler')]/a")).click();
			this.waitAndClickAllCommentOption();
		}
	}
	
	
	private boolean waitAndClickAllCommentOption() {
		ExpectedCondition<Boolean> allCommentsClicked = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//ul[@class='_54nf']/li[3]/a")).size() == 1) {
					driver.findElement(By.xpath("//ul[@class='_54nf']/li[3]/a")).click();
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(allCommentsClicked);
	}
	

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}
	
	
	
	
	public void waitForPageLoaded() {
		ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(this.getDriver(), 30);
        wait.until(pageLoadCondition);
    }
	
	
	
	
	
	
	/**
	 * 
	 * @return las queries xpath que se deben ejecutar según el tipo de post.
	 * @throws Exception
	 */
	private PublicationScrapper xpathPublicationQueries() throws Exception{
		PublicationScrapper ps = new PublicationScrapper();
		try {
			this.waitForPageLoaded();
			//this.waitUntilContentLoad();
			
		}catch(Exception e) {
			if(e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
				//do nothing
			}else {
				throw e;
			}
		}
		//Según el tipo de container, extraigo todos los XPATH a utulizar.
		if(this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]")).size() > 0) { //&&
			//this.getDriver().findElements(By.xpath("//div[contains(@class,'_3ixn')]")).size() > 0){
			ps.setXpath_publication_container("//div[contains(@class,'userContentWrapper')]");
			ps.setXpath_all_comments(FacebookConfig.XPATH_COMMENTS); // or contains(data-testid, UFI2CommentsList/root_depth_0)
			ps.setXpath_ver_mas_comments(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
			//ps.setXpath_ver_mas_comments("//a[@class='_4sxc _42ft']");
			//Por lo general hay que pedirle que muestre los comentarios.
			ps.setXpath_mostrar_comments(null);
			ps.setXpath_publication_spinner_loader("//span[@role='progressbar']");
			ps.setXpath_publication_loaded("//form[contains(@class,'commentable_item')]"); //carga contenedor de comentarios.
		}else if(this.getDriver().findElements(By.xpath("//div[contains(@class,'fbPhotoSnowliftContainer snowliftPayloadRoot uiContextualLayerParent')]")).size() > 0 &&
				this.getDriver().findElements(By.xpath("//div[contains(@class,'_3ixn')]")).size() > 0){
			ps.setXpath_publication_container("//div[contains(@class,'fbPhotoSnowliftContainer snowliftPayloadRoot uiContextualLayerParent')]");
			//ps.setXpath_all_comments(FacebookConfig.XPATH_COMMENTS); // or contains(data-testid, UFI2CommentsList/root_depth_0)
			ps.setXpath_all_comments("//div[contains(@data-testid, 'UFI2Comment/body') and not(contains(@style,'hidden'))]");
			//ps.setXpath_ver_mas_comments(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
			ps.setXpath_ver_mas_comments("//div[@class='_6iiz _77br']//a[@class='_4sxc _42ft']");
			//Por lo general hay que pedirle que muestre los comentarios.
			ps.setXpath_mostrar_comments(null);
			ps.setXpath_publication_spinner_loader("//span[@role='progressbar']");
			ps.setXpath_publication_loaded("//form[contains(@class,'commentable_item')]");
		}else if(this.getDriver().findElements(By.xpath("//div[contains(@class,'Popup')]")).size() == 1) {
			ps.setXpath_publication_container("//div[contains(@class,'Popup')]");
			ps.setXpath_all_comments("//div[@class='_6iiv _6r_e']//div[@class=' _4eek _6ijk clearfix clearfix' and not(contains(@style,'hidden'))]"); // or contains(data-testid, UFI2CommentsList/root_depth_0)
			ps.setXpath_ver_mas_comments("//div[@class='_4swz _6ijj']/a[@class='_4sxc _42ft']");
			ps.setXpath_publication_spinner_loader("//img[contains(@class,'spotlight')]");
			//Por default ya se muestran comentarios y no es necesario un click inicial en "ver comentarios" para que los muestre.
			ps.setXpath_mostrar_comments(null);
			//esperar que cargue la seccion con comentarios...
			ps.setXpath_publication_loaded("//form[contains(@class,'commentable_item')]");
		}else if(this.getDriver().findElements(By.xpath("//div[@class='_wyj _20nr']")).size() == 1){
			
			this.hiddenChatTab(); //A veces esto tab tapa el "ver comentarios"
			
			
			ps.setXpath_publication_container("//div[@class='_wyj _20nr']");
			ps.setXpath_all_comments("//div[@data-testid='UFI2Comment/root_depth_0' and not(contains(@style,'hidden'))]"); // or @class=' _4eek clearfix _7gq4 clearfix' 
			ps.setXpath_ver_mas_comments("//a[contains(@class,'_4sxc _42ft')]");//--> si no aparecen comentarios, hacer click en all comments.
			//Por lo general hay que pedirle que muestre los comentarios.
			ps.setXpath_mostrar_comments("//a[contains(@class,'_3hg- _42ft')]"); //--->Devuelve más de 1, tomar el primero.
			ps.setXpath_publication_spinner_loader("//div[@class='_7gpu _7gpv']//span[@role='progressbar']");
			ps.setXpath_publication_loaded("//a[contains(@class,'_666h  _18vj _18vk _42ft')]");
		}else if(this.getDriver().findElements(By.xpath("//div[@class='_5-g-']")).size()==1) {
			ps.setXpath_publication_container("//div[@class='_5-g-']");
			ps.setXpath_all_comments(FacebookConfig.XPATH_COMMENTS); // or contains(data-testid, UFI2CommentsList/root_depth_0)
			ps.setXpath_ver_mas_comments(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
			//Por lo general hay que pedirle que muestre los comentarios.
			ps.setXpath_mostrar_comments(FacebookConfig.XPATH_VIEW_ALL_PUB_COMMENTS_LINK);
			ps.setXpath_publication_spinner_loader("//span[@role='progressbar']");
		}/*else if(this.getDriver().findElements(By.xpath("//div[contains(@class,'userContentWrapper')]")).size() > 0){
			ps.setXpath_publication_container("//div[contains(@class,'userContentWrapper')]");
			ps.setXpath_all_comments(FacebookConfig.XPATH_COMMENTS); // or contains(data-testid, UFI2CommentsList/root_depth_0)
			ps.setXpath_ver_mas_comments(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS);
			//ps.setXpath_ver_mas_comments("//a[@class='_4sxc _42ft']");
			//Por lo general hay que pedirle que muestre los comentarios.
			ps.setXpath_mostrar_comments(FacebookConfig.XPATH_VIEW_ALL_PUB_COMMENTS_LINK);
			ps.setXpath_publication_spinner_loader("//span[@role='progressbar']");
			ps.setXpath_publication_loaded("//a[contains(@class,'comment_link _5yxe')]");
		}*/else {
				if(this.getDriver().getCurrentUrl().contains("/events/")) {
					throw new Exception("PUB_EVENT");
				}else {
					throw new Exception("[ERROR] FORMATO DE PUBLICACION NO RECONOCIDO: " + this.getDriver().getCurrentUrl());
				}
			
		}
		
		if (debug)
			System.out.println("** publication container: " + ps.getXpath_publication_container());
		
		if(ps.getXpath_publication_loaded()==null) {
			ps.setXpath_publication_loaded("//a[@class=' _666h  _18vj _18vk _42ft']"); //Espero hasta que aparezca el boton comentar...
		}
		
		return ps;	
	}
	
	private boolean waitUntilPublicationLoad(final PublicationScrapper ps) {
		ExpectedCondition<Boolean> pubLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(ps.getXpath_publication_loaded())).size() >0) {
					//driver.findElement(By.xpath(ps.getXpath_publication_spinner_loader())).click();
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(pubLoad);
	}
	
	private boolean waitUntilContentLoad() {
		ExpectedCondition<Boolean> pubLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath("//div[contains(@class,'Popup')]")).size() >0 || driver.findElements(By.xpath("//form[contains(@class,'commentable_item')]")).size() >0 || driver.findElements(By.xpath("//div[@class='_5-g-']")).size() >0) {
					//driver.findElement(By.xpath(ps.getXpath_publication_spinner_loader())).click();
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(15)).pollingEvery(Duration.ofSeconds(1));
		return wait.until(pubLoad);
	}
	
	private void hiddenChatTab() {
		//En algunos casos bloquea el click a "Mostrar mensajes"
		//((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath("//div[@id='BuddylistPagelet']")));
		//((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath("//div[@class='clearfix nubContainer rNubContainer']")));
		((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath("//div[@class='_48gf fbDockWrapper fbDockWrapperRight']")));
		//_48gf fbDockWrapper fbDockWrapperRight
	}

	/**
	 * Recibe la publicación y le da click en el mostrar todos los comentarios. (si no, quedan solo los más relevantes).
	 * @param publication
	 */
	private void pubShowAllComments(WebElement pub) throws Exception{
		try {
			if(pub.findElements(By.xpath("//a[@data-testid='UFI2ViewOptionsSelector/link']")).size()>0) {
				pub.findElements(By.xpath("//a[@data-testid='UFI2ViewOptionsSelector/link']")).get(0).click();
			}else if(pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).size()>0) {
				((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).get(0));
				try {
					if (this.getDriver().findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
						(new Actions(this.getDriver())).sendKeys(Keys.ESCAPE).perform();
					}
					Thread.sleep(500);
					this.overlayHandler();
					pub.findElements(By.xpath("//div[@class='_3scs uiPopover _6a _6b']/a")).get(0).click();
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			//Espero a que cargue las opciones del menu y le hago click a la ultima opcion.
			this.waitUntilMenuOptionsAppear();
		}catch(Exception e) {
			throw e;
		}
	}
	
	private boolean waitUntilMenuOptionsAppear() {
		ExpectedCondition<Boolean> pubOptionsLoad = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				
				if (driver.findElements(By.xpath("//ul[@class='_54nf']")).size() >0) {
					WebElement link = driver.findElements(By.xpath("//ul[@class='_54nf']//a[@class='_54nc']")).get(driver.findElements(By.xpath("//ul[@class='_54nf']//a[@class='_54nc']")).size()-1);
					//tomo el ultimo item...
					((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", link);
					try {
						if (driver.findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
							(new Actions(driver)).sendKeys(Keys.ESCAPE).perform();
							return false;
						}
						Thread.sleep(500);
						link.click();
						Thread.sleep(500);
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
	}
	
	
}
