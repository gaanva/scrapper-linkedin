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
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.entities.User;
import com.rocasolida.scrapperfacebook.entities.UserLike;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookLinkType;
import com.rocasolida.scrapperfacebook.scrap.util.FacebookPostType;

public class FacebookNewUsersExtract extends Scrap {
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	
	private static Integer WAIT_UNTIL_SPINNER = 10;
	//Cada cuanto el waiter chequea la condicion.
	private static Integer CHECK_DELAY_MS = 10;
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
		long tardo = System.currentTimeMillis();
		try {
			Page page = new Page();
			page.setName(facebookPage);
			//Cargo las publicaciones
			this.LoadPublications(facebookPage, page);
			//A este punto ya me cargo las publicaciones...
			//Revisar el metodo processPagePosts, donde busca posts en un rango de fechas...
		    //Deberia solo extraer las que estan en un array.
			//Recorrer los posts y extraer comments users.
			//si no cumpli la cantidad de usrs con la primer extraccion [repetir proceso]
			
			
			
			if (publicationsElements != null) {
				List<Publication> publicationsImpl = new ArrayList<Publication>();
				for (int i = 0; i < publicationsElements.size(); i++) {
					if (this.waitForJStoLoad()) {
						this.moveTo(publicationsElements.get(i));
						publicationsImpl.add(this.extractPublicationData(facebookPage, publicationsElements.get(i)));
					} else {
						System.out.println("[ERROR] PROBLEMAS AL EXTRAER DATOS DEL POST.");
						this.saveScreenShot("PROBLEMA_EXTRAER_DATOSPOST");
					}

				}
				// Recorro publicaciones encontradas
				Publication result = null;
				for (int i = 0; i < publicationsImpl.size(); i++) {
					result = obtainPostInformation(facebookPage, publicationsImpl.get(i).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, null, null);
					merge(publicationsImpl.get(i), result);
				}
				page.setPublications(publicationsImpl);
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
	
	//Va al pagina de posts de la page y carga las publicaciones...
	public void LoadPublications(String facebookPage, Page page) throws Exception {
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
				if (debug)
					System.out.println("[INFO] Es una Página.");
				//Por ahí antes se podria cargar la espera del container de publications...
				try {
					if (debug)
						System.out.println("[INFO] SPINNER ACTIVE?...");
					this.waitUntilNotSpinnerLoading();
				} catch (Exception e1) {
					if (e1.getClass().getSimpleName().equalsIgnoreCase("TimeoutException")) {
						if (debug)
							System.out.println("[WARN] TIEMPO ESPERA NOTSPINNER EXCEDIDO.");
					}
				}
			default:
				throw new Exception("[WARNING] No se reconoce el tipo de página para hacer SCRAP. En LoadPublications().");
			}
		} finally {
			aux = System.currentTimeMillis() - aux;
			System.out.println("LoadPublications con wait, tardo: " + aux);
		}
	}
	
	public Publication obtainPostInformation(String pageName, String postId, Long COMMENTS_uTIME_INI, Long COMMENTS_uTIME_FIN, Integer cantComments, CommentsSort cs) throws Exception {
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
			ocultarBannerLogin();
			String currentURL = this.getDriver().getCurrentUrl();
			FacebookPostType fpt = getPostType(currentURL);
			System.out.println("currentURL: " + currentURL + ". fpt: " + fpt);
			if (fpt != null && fpt.equals(FacebookPostType.PHOTO)) {
				try {
					waitUntilOverlayPhotoAppears();
					return obtainPostTypePhotoInformation(pageName, postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub);
				} catch (org.openqa.selenium.TimeoutException ex) {
					// Si tira timemout es porque no tiene overlay entonces proceso como la otra
					// forma
					return obtainPostTypeOtherInformation(pageName, postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub);
				}
			} else {
				return obtainPostTypeOtherInformation(pageName, postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs, pub);
			}
		} catch (Exception e) {
			if (debug) {
				System.out.println("[ERROR] AL ACCEDER AL POST.");
				this.saveScreenShot("ERR_ACCESO_POST");
			}
			throw e;
		}
	}	
	public List<UserLike> obtainProfileLikes(String profile) throws Exception {
		
		
		// this.validateProfileLink(profile);
		List<UserLike> listaLikes = new ArrayList<UserLike>();
		// voy a los likes derecho viejo, de una a lo guapo.
		// ir al link con el /likes_all
		this.navigateTo(FacebookConfig.URL + profile + "/likes_all");
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XP_HAS_LIKES_CONTENT)).size() > 0) {
			System.out.println("Entro a " + FacebookConfig.URL + profile + "/likes_all");

			// Me fijo que no aparezca un overlay...
			if (this.getDriver().findElements(By.xpath("//div[@class='_3ixn']")).size() > 0) {
				this.getActions().sendKeys(Keys.ESCAPE).perform();
				this.getActions().sendKeys(Keys.ESCAPE).perform();
			}

			List<WebElement> aux = this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES));
			if (aux.size() == 0) {
				System.out.println("No likes.");
				return listaLikes;
			}
			do {

				for (int i = 0; i < aux.size(); i++) {
					try {
						UserLike aux1 = new UserLike();
						aux1.setUrl(aux.get(i).findElement(By.xpath("./div[@class='_3owb']//div[@class='fsl fwb fcb']/a")).getAttribute("href"));
						aux1.setTitle(aux.get(i).findElement(By.xpath(".//div[@class='_42ef']//div[@class='fsl fwb fcb']/a")).getText());
						aux1.setCategory(aux.get(i).findElement(By.xpath(".//div[@class='_42ef']//div[@class='fsm fwn fcg']")).getText());
						listaLikes.add(aux1);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				for (int i = 0; i < aux.size(); i++) {
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", aux.get(i));
				}
				System.out.println("Obtengo likes current_cant: " + listaLikes.size());
				this.scrollDown();
				Thread.sleep(500);
				aux = this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES));
				// aux = this.getDriver().findElements(By.xpath("//li[contains(@class,'_5rz _5k3a _5rz3 _153f') and not(contains(@style,'hidden'))]/div/div/a"));
				// mientras haya spinner loader...
			} while (this.getDriver().findElements(By.xpath(FacebookConfig.XP_LIKES_LOADING)).size() > 0 || this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES)).size() > 0);

			aux = this.getDriver().findElements(By.xpath("//li[contains(@class,'_5rz _5k3a _5rz3 _153f') and not(contains(@style,'hidden'))]"));

		} else {
			System.out.println("NO entro a " + FacebookConfig.URL + profile + "/likes_all");
			if (this.getDriver().getCurrentUrl().equalsIgnoreCase(FacebookConfig.URL + profile)) {
				throw new Exception("SIN_PERMISOS_VER_LIKES");
			} else {
				this.saveScreenShot("PROFILE_ErrorNotHandled");
				throw new Exception("[ERROR] UNHANDLED ERROR! check log snapshot: 'PROFILE_ErrorNotHandled'");
			}
		}
		System.out.println("Termino bien y con likes: " + listaLikes.size());
		return listaLikes;
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

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

}
