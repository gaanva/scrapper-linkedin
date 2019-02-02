package com.rocasolida.scrapperfacebook.scrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Comment;
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.entities.User;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookLinkType;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookPostType;

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

	public FacebookLinkType facebookLinkType() throws Exception{
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

	public List<User> obtainUsersCommentInformation(String facebookPage, int cantUsuarios) throws Exception {
		List<User> users = new ArrayList<User>();
		List<Publication> publicaciones = new ArrayList<Publication>();
		//Por las dudas me guardo todas las pubs procesadas...
		List<Publication> auxPubs = new ArrayList<Publication>();
		
		try {
			//Verifico que sea link de pagina
			this.verifyPage(facebookPage);
			do{
				//Obtengo las publicaciones a procesar...
				//La segunda vez, toma como referencia el ultimo utime, para procesar a partir de esa publicacion 
				//a las de mas abajo.
				auxPubs = this.loadPublicationsToBeProcessed(publicaciones.isEmpty()?null:publicaciones.get(publicaciones.size()-1));
				for(int i=0; i<auxPubs.size();i++) {
					this.navigateTo(auxPubs.get(i).getUrl());
					users = this.processPublicationComments(users, cantUsuarios);
					if(this.encontroCantUsers){
						break;
					}
				}
				//Esto como auditoria de control, no haria falta pero para los primeros tests no va a venir mal...
				publicaciones.addAll(auxPubs);
			
			}while(!encontroCantUsers || hayMasPubs);
			return users;
		}catch(Exception e) {
			throw e;
		}
		
	}
	
	public List<Publication> extractPublicationsInfo(List<WebElement> pubsHtml) {
		List<Publication> pubs = new ArrayList<Publication>();
		if(debug)
			System.out.println("[INFO] SE PROCESARAN "+pubsHtml.size()+" PUBLICACIONES");
		
		for(int i=0; i<pubsHtml.size(); i++) {
			Publication aux = new Publication();
			aux.setUrl(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_HEADER_CONTAINER+FacebookConfig.XPATH_PUBLICATION_LINK)).getAttribute("href"));
			aux.setUTime(Long.parseLong(pubsHtml.get(i).findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_HEADER_CONTAINER+FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
			pubs.add(aux);
		}
		
		return pubs;
	}
	
	
	
	public boolean waitUntilNotSpinnerLoading() {
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
	
	//controla que sea una pagina...
	public void verifyPage(String facebookPage) throws Exception {
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
			default:
				throw new Exception("[WARNING] No se reconoce el tipo de página para hacer SCRAP. En LoadPublications().");
			}
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("LoadPublications con wait, tardo: " + aux);
		}
	}
	
	//Carga las publicaciones a ser procesadas.
	public List<Publication> loadPublicationsToBeProcessed(Publication lastPubProcessed) throws Exception{
		List<WebElement> pubs = new ArrayList<WebElement>();
		if(lastPubProcessed == null) {
			//Entonces cargo las publiciones que me trae la pagina
			try {
				if (debug)
					System.out.println("[INFO] espera a la carga de las publicaciones de la pagina...");
				this.waitUntilNotSpinnerLoading();
			} catch (Exception e1) {
				if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
					if (debug)
						System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEDIDO.");
					if(!(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER)).size()>0)) {
						throw new Exception("[WARN] NO SE CARGARON PUBLICACIONES.");
					}else {
						pubs = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATIONS_CONTAINER));
						System.out.println("[INFO] SE CARGARON:"+ pubs.size() +" PUBLICACIONES.");
											}
				}else {
					System.out.println("[ERROR] al cargar la pagina.");
					throw e1;
				}
			}
		}else {
			int intentosCargaPubs=0;
			//Tengo que cargar a partir de la ultima procesada...
			while(!((this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(null, lastPubProcessed.getUTime()))).size()) > 0)
					&& intentosCargaPubs<3) {
				try {
					this.waitUntilShowMorePubsAppears(this);
				} catch (Exception e) {
					if (e.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TimeoutException. Waiting ShowmorePublications button");
					} else {
						System.out.println("[ERROR] error al esperar el boton de show more en la pagina ppal de la page.");
						
					}
					intentosCargaPubs++;
				}
				if ((this.existElement(null, FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE))) {
					this.scrollMainPublicationsPage();
					try {
						System.out.println("[INFO] SPINNER ACTIVE? esperando a que carguen mas publicaciones...");
						if (debug)							
							this.saveScreenShot("SPINNER ACTIVE");
						this.waitUntilNotSpinnerLoading();
					} catch (Exception e1) {
						if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
							if (debug)
								System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEEDED");
						}else {
							System.out.println("[ERROR] wait until not spinner loading.");
							//throw e1;
						}
						intentosCargaPubs++;
					}
				} else {
					if (debug) {
						this.saveScreenShot("posts");
						System.out.println("[INFO] YA SE RECORRIERON TODAS LAS PUBLICACIONES DE LA PÁGINA. NO SE ENCONTRÓ BTN SHOW MORE: " + FacebookConfig.XPATH_PPAL_BUTTON_SHOW_MORE);
					}
					this.hayMasPubs=false;
					break;
				}
				
			}
			
			pubs=this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(null, lastPubProcessed.getUTime())));
		}
		
		return this.extractPublicationsInfo(pubs);
		
	}
	
	public boolean waitUntilShowMorePubsAppears(final FacebookNewUsersExtract fs) {
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
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(200)).ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class);
		return wait.until(loadMore);
	}
	
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
	
	
	private void scrollMainPublicationsPage() {
		((JavascriptExecutor) this.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight)");
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
	
	
	//Hacer iteracion para que lea todos los comentarios.
	public List<User> processPublicationComments(List<User> users, int cantUsers) throws Exception {
		WebElement showMoreCommentsLink;
		List<WebElement> pubComments;
		int totUsersProcessed = users.size();
		boolean hayMasComentarios = true;
		do {
			
			if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS)).size() > 0) {
				try {
					this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_VER_MAS_MSJS)).click();
					// Poner un wait after click. (sumar al de extracción de comments...)
					this.waitUntilMoreCommentsClickLoad();
				} catch (Exception e) {
					throw e;
				}
			}else {
				hayMasComentarios=false;
			}
			//Si no existe botón de whowmore... entonces, va a tomar los comentarios visibles de la publicación...
			pubComments = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
			for(int j=0; j<pubComments.size(); j++) {
				User auxUser = this.extractCommentUserProfileLink(pubComments.get(j));
				//Ya procese el comentario, entonces lo pongo en hidden.
				((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", pubComments.get(j));
				
				if(!users.contains(auxUser)) {
					users.add(auxUser);
					totUsersProcessed ++;
				}
				if(totUsersProcessed == cantUsers) {
					this.encontroCantUsers = true;
					break;
				}
				
			}
		}while(!this.encontroCantUsers && hayMasComentarios);
		
		return users;
		
	}
	
	public User extractCommentUserProfileLink(WebElement comentario) throws Exception {
		User auxUser = new User();
		// Usuario Profile Url
		if (this.getAccess() != null) {
			String userProfileUrl = comentario.findElement(By.xpath(FacebookConfig.XPATH_USER_URL_PROFILE)).getAttribute("href");
			auxUser.setUrlPerfil(userProfileUrl);
		}
		return auxUser;
	}

	

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

}
