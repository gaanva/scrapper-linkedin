package com.rocasolida.scrapperfacebook.scrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.UserLike;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;

public class FacebookUserLikesScrap extends Scrap{
	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);

	public FacebookUserLikesScrap(Driver driver, boolean debug) throws MalformedURLException {
		super(driver, debug);
	}
	
	public void login(Credential access) throws Exception{
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
			if (this.getDriver().findElements(By.xpath("//div[@id='entity_sidebar']//div//div[@data-key='tab_posts' or @data-key='tab_community' or @data-key='tab_home']//descendant::a")).size()>0) {
				return "PAGE";
			}
			// Asumo que SI (Encuentro: Biografía || Amigos EN EL TOP MENU) ES UN PERFIL
			if (this.getDriver().findElements(By.xpath("//div[@id='fbProfileCover']//child::div[contains(@id,'fbTimelineHeadline')]//descendant::li//a")).size()>0) {
				return "PROFILE";
			}
		} catch (Exception e) {
			System.out.println("[ERROR] AL COMPROBAR TIPO DE LINK (PAGINA | PERFIL)");
			this.saveScreenShot("ERR_COMPR_LINK");
		}

		return "";

	}

	public List<UserLike> obtainProfileLikes(String profile) throws Exception{
		//this.validateProfileLink(profile);
		List<UserLike> listaLikes = new ArrayList<UserLike>();
		//voy a los likes derecho viejo, de una a lo guapo.
		//ir al link con el /likes_all
		this.navigateTo(FacebookConfig.URL + profile + "/likes_all");
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_HAS_LIKES_CONTENT)).size()>0) {
			
			//Me fijo que no aparezca un overlay...
			if(this.getDriver().findElements(By.xpath("//div[@class='_3ixn']")).size()>0) {
				this.getActions().sendKeys(Keys.ESCAPE).perform();
				this.getActions().sendKeys(Keys.ESCAPE).perform();
			}
			
			List<WebElement> aux = this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES));
			if(aux.size()==0) {
				return listaLikes;
			}
			do {
				
				for(int i=0; i<aux.size(); i++) {
					UserLike aux1 = new UserLike();
					aux1.setUrl(aux.get(i).findElement(By.xpath("./div[@class='_3owb']//div[@class='fsl fwb fcb']/a")).getAttribute("href"));
					aux1.setTitle(aux.get(i).findElement(By.xpath(".//div[@class='_42ef']//div[@class='fsl fwb fcb']/a")).getText());
					aux1.setCategory(aux.get(i).findElement(By.xpath(".//div[@class='_42ef']//div[@class='fsm fwn fcg']")).getText());
					listaLikes.add(aux1);
				}
				
				for(int i =0; i<aux.size(); i++) {
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", aux.get(i));
				}
				
				this.scrollDown();
				aux = this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES));
				//aux = this.getDriver().findElements(By.xpath("//li[contains(@class,'_5rz _5k3a _5rz3 _153f') and not(contains(@style,'hidden'))]/div/div/a"));
			//mientras haya spinner loader...
			}while(this.getDriver().findElements(By.xpath(FacebookConfig.XP_LIKES_LOADING)).size()>0 || this.getDriver().findElements(By.xpath(FacebookConfig.XP_USER_LIKES)).size()>0);
			
			aux = this.getDriver().findElements(By.xpath("//li[contains(@class,'_5rz _5k3a _5rz3 _153f') and not(contains(@style,'hidden'))]"));
			
		}else {
			if(this.getDriver().getCurrentUrl().equalsIgnoreCase(FacebookConfig.URL + profile)) {
				throw new Exception("SIN_PERMISOS_VER_LIKES");
			}else {
				this.saveScreenShot("PROFILE_ErrorNotHandled");
				throw new Exception("[ERROR] UNHANDLED ERROR! check log snapshot: 'PROFILE_ErrorNotHandled'");
			}
		}
				
		return listaLikes;
	}
	
	public void validateProfileLink(String profile) throws Exception{
		//Esto es 1 like.
		//LI class="_5rz _5k3a _5rz3 _153f"
		// /div/div/a getAttribute("href")   --> Obtengo link.
		this.navigateTo(FacebookConfig.URL + profile);
		
		String linkType = this.facebookLinkType(); // POR AHORA CHEQUEA SI ES PAGINA O PERFIL
		switch (linkType) {
			case "PROFILE":
				if (debug)
					System.out.println("[INFO] Es un Perfil.");
			case "PAGE":
				throw new Exception("[ERROR] ES un página! ");			
			default:
				throw new Exception("[WARNING] No se reconoce el tipo de página para hacer SCRAP");
		}
		
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

	private void navigateTo(String URL) throws Exception{
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
				 * Este IF captura estos errores: - Si entra a un perfil inválido o inexistente, ej: https://www.facebook.com/slkndfskldnfsdnfl - a un post inválido o inexistente https://www.facebook.com/HerbalifeLatino/posts/123123123 (idpost inexistente) - id post válido, pero URL inválida
				 * https://www.facebook.com/herbalife/posts/1960450554267390 (idpost válido)
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
