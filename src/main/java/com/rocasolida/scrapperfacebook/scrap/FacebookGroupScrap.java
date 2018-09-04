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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import com.rocasolida.scrapperfacebook.FacebookConfig;
import com.rocasolida.scrapperfacebook.entities.Comment;
import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.Group;
import com.rocasolida.scrapperfacebook.entities.GroupPublication;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;

public class FacebookGroupScrap extends Scrap {

	final String countRegex = "\\d+([\\d,.]?\\d)*(\\.\\d+)?";
	final Pattern pattern = Pattern.compile(countRegex);
	private static Integer WAIT_UNTIL_SECONDS = 10;


	public FacebookGroupScrap(Driver driver, boolean debug) throws MalformedURLException {
		super(driver, debug);
	}

	public boolean login(Credential access) {
		/*
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
		*/
		return true;
	}

	
	/**
	 * Me pasan el grupo a acceder y la cantidad de publicaciones a extraer...
	 * @param facebookGroup
	 * @param cantPublications
	 * @return Group (que contiene la lista de publicaciones.)
	 * @throws Exception
	 */
	public Group obtainGroupPubsWithoutComments(String facebookGroup, int cantPublications, Long uTimeFrom, Long uTimeTo) throws Exception {
		long tardo = System.currentTimeMillis();
		
		this.navigateTo(FacebookConfig.URL+FacebookConfig.URL_GROUP+facebookGroup);
		
		try {
			List<GroupPublication> groupPubs = new ArrayList<GroupPublication>();
			//Obtiene la cantidad de publicaciones de la sección de newsfeed.
			List<WebElement> groupPubsHTML = new ArrayList<WebElement>();
			
			if(cantPublications > 0) {
				if(debug)
					System.out.println("EXTRAYENDO PUBLICACIONES POR CANTIDAD: " + cantPublications);
				groupPubsHTML = this.obtainMainPageGroupPublicationsHTMLByQuantity(cantPublications, FacebookConfig.XP_GROUP_PUBLICATIONS_LASTNEWS_CONTAINER);
				
				int cantPubRestantes = cantPublications - groupPubsHTML.size();
				if(cantPubRestantes>0) {
					//Obtiene la cantidad de publicaciones desde la sección de 'old' publications o lo que no está
					//en la parte de newsFeed.
					groupPubsHTML.addAll(this.obtainMainPageGroupPublicationsHTMLByQuantity(cantPubRestantes, FacebookConfig.XP_GROUP_PUBLICATIONS_OLD_CONTAINER));
					
				}
				
				if(debug)
					System.out.println("SE EXTRAJERON: " + groupPubsHTML.size() + " PUBLICACIONES.");
				
				if(groupPubsHTML!=null) {
					for(int i=0; i<groupPubsHTML.size(); i++) {
						//Extraigo el ID-URL y UTIME de la publicacion encontrada
						groupPubs.add(this.extractMainPagePublicationID(groupPubsHTML.get(i)));
					}
				}
				
				Group grupo = new Group();
				grupo.setPublications(groupPubs.size()>0?groupPubs:null);
				return grupo;
								
			}else if(uTimeFrom!=null && uTimeTo!= null){
				if(debug)
					System.out.println("EXTRAYENDO PUBLICACIONES POR UTIME. From: " +uTimeFrom+ " To: "+uTimeTo);
				
				groupPubsHTML = this.obtainMainPageGroupPublicationsHTMLByUTime(uTimeFrom, uTimeTo);
				if(debug)
					System.out.println("SE EXTRAJERON: " + groupPubsHTML.size() + " PUBLICACIONES.");
				if(groupPubsHTML!=null) {
					for(int i=0; i<groupPubsHTML.size(); i++) {
						//Extraigo el ID-URL y UTIME de la publicacion encontrada
						groupPubs.add(this.extractMainPagePublicationID(groupPubsHTML.get(i)));
					}
				}
				
				Group grupo = new Group();
				grupo.setPublications(groupPubs.size()>0?groupPubs:null);
				return grupo;
				
			}else {
				throw new Exception("[ERROR] SE requiere la cantidad de publicaciones o el rango de fechas a buscar.");
			}
		}finally {
			tardo = System.currentTimeMillis() - tardo;
			System.out.println("obtainGroupPubsWithoutComments tardo: " + tardo);
		}
		
	}
	
	/**
	 * De la página ppal del Grupo, los datos identificatorios de una poublicación.
	 * @param publication
	 * @return
	 */
	public GroupPublication extractMainPagePublicationID(WebElement publication) {
		GroupPublication aux = new GroupPublication();
		
		//String postID = this.regexPostID(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
		String URL = publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href");
		String postID;
		
		
		//Extraigo el ID del post y la URL.
		String[] splitURL = URL.split("/");
		if(splitURL[splitURL.length-1].contains("sale_post_id")) {
			postID = splitURL[splitURL.length-2];
		}else {
			postID = splitURL[splitURL.length-1];
		}
		
		if (postID == "") {
			if (debug)
				System.out.println("[INFO] ERROR AL ENCONTRAR EL ID DEL POST: " + publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
		} else {
			aux.setId(postID);
			aux.setUrl(URL);
		}
		
		//Extraigo el UTIME DE LA PUBLICACION.
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)) {
			aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
		} else if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)) {
			aux.setUTime(Long.parseLong(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).getAttribute("data-utime")));
		}
		
		return aux.getId()==null?null:aux;
	}
	
	/**
	 * Extraigo el HTML de las publicaciones que lista la página principal de un grupo.
	 * El xpathExpression hace referencia a news feed o a la lista de publications comun.
	 * @param cantidadPublicacionesInicial
	 * @param xpathExpression
	 * @return
	 * @throws Exception
	 */
	public List<WebElement> obtainMainPageGroupPublicationsHTMLByQuantity(int cantidadPublicacionesInicial, String xpathExpression) throws Exception {
		
		List<WebElement> auxList = new ArrayList<WebElement>();
		auxList = this.getDriver().findElements(By.xpath(xpathExpression));
		
		int tot=0;
		if (auxList.size() > 0) {
			tot = auxList.size();
			//while(!(this.getDriver().findElements(By.xpath("//div[@class='_5pcb']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']"+FacebookConfig.XP_GROUP_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookGroup, uTIME_INI))).size() > 0)){
			while(!(this.getDriver().findElements(By.xpath(xpathExpression)).size()>= cantidadPublicacionesInicial)) {	
				this.scrollDown();
				auxList = this.getDriver().findElements(By.xpath(xpathExpression));
				
				//Control de corte por no haber más publicaciones...
				try {
					//this.waitUntilGroupPubsLoad(tot);
					this.waitUntilMainPageGroupPubsLoad(tot, xpathExpression);
					tot = auxList.size();
				}catch(Exception e) {
					break;
				}
				
			}
		} else {
			return auxList;
		}	
		
		//retorno hasta las primeras cantidad de publicaciones indicadas.
		return auxList.size()>cantidadPublicacionesInicial?auxList.subList(0, cantidadPublicacionesInicial):auxList;
	}
	
	/**
	 * La página ppal de un grupo tiene sección news feed o comun, por eso se espera
	 * en distintas xpathexpression.
	 * @param cantAnterior
	 * @param xpathExpression
	 * @return
	 */
	private boolean waitUntilMainPageGroupPubsLoad(final int cantAnterior, final String xpathExpression) {
		ExpectedCondition<Boolean> loadMorePublications = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(xpathExpression)).size()>cantAnterior) {
					
					return true;
				} else {
					return false;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(200));
		return wait.until(loadMorePublications);
	}

	
	/**
	 * Busco todas las publicaciones que cumplen con el filtro utime.
	 * @param uTimeFROM
	 * @param uTimeTo
	 * @return Lista de Post en formato WebElement.
	 * @throws Exception
	 */
	public List<WebElement> obtainMainPageGroupPublicationsHTMLByUTime(Long uTimeFROM, Long uTimeTo) throws Exception {
		List<WebElement> postsSelected = new ArrayList<WebElement>();
		//trae las publicaciones con utime...
		
		int cantScrolls = 0;
		//total de publicaciones...
		int tot = this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPMAINPUBLICATIONS_ALL)).size();
		
		
		while(cantScrolls<2) {	
			//Si hay posts con fecha mayor o igual al corte de inicio...
			if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPMAINPUBLICATIONS_TIMESTAMP_FROM_CONDITION(uTimeFROM))).size()>0) {
				//Si la cantidad de publicaciones entre el rango de fechas ha aumentado...
				if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPMAINPUBLICATIONS_TIMESTAMP_CONDITION(uTimeFROM, uTimeTo))).size()>postsSelected.size()) {
					postsSelected = this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPMAINPUBLICATIONS_TIMESTAMP_CONDITION(uTimeFROM, uTimeTo)));
				}else {
					//Contador de cantidad de scrolles cuando no hay posts entre lsa fechas buscadas.
					cantScrolls++;
				}
			}else {
				//Contador de cantidad de scrolles cuando no hay posts entre lsa fechas buscadas.
				cantScrolls++;
			}
			
			this.scrollDown();
			try {
				//Espero a que la cantidad total de 
				this.waitUntilMainPageGroupPubsLoad(tot, FacebookConfig.XP_GROUPMAINPUBLICATIONS_ALL);
			}catch(Exception e) {
				break;
			}
			
		}
		
		
		return postsSelected;
	}
	
	
	
	
	
	
	
	
	
	/**
	 * Extracción de los datos de cabecera y comentarios de una publicación
	 * @param groupPubURL
	 * @return
	 * @throws Exception
	 */
	public GroupPublication obtainFullPubInformation(String groupPubURL) throws Exception{
		this.navigateTo(groupPubURL);
		
		if(debug)
			System.out.println("[INFO] >>> Checking overlays...");
		//NO espero a que carge el overlay si existe en DOM, lo pongo invisile
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_OVERLAY)).size()>0) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_OVERLAY)));
			if(debug)
				System.out.println("[INFO] Hidding publication overlay...");
		}else {
			if(debug)
				System.out.println("[INFO] Overlay Not Found...");
		}
	
		//Puede cargar otro overlay de login, dependiendo la publicacion.
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_LOGIN_OVERLAY)).size()>0) {
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", this.getDriver().findElement(By.xpath(FacebookConfig.XP_LOGIN_OVERLAY)));
			if(debug)
				System.out.println("[INFO] Hidding Login overlay...");
		}else {
			if(debug)
				System.out.println("[INFO] Login Overlay Not Found...");
		}
		
		if(debug)
			System.out.println("[INFO] <<< FIN Checking overlays.");
	
		
		//Control del primer click sobre la sección de comentarios para que se desplegue
		//Todos los comentarios, likes y shares de la publicación.
		if(this.getAccess()==null) {
			if(this.getDriver().findElements(By.xpath("//form[@class='commentable_item collapsed_comments']/descendant::a")).size()>0) {
				this.getDriver().findElement(By.xpath("//form[@class='commentable_item collapsed_comments']/descendant::a")).click();
				try {
					this.waitUntilCommentsLoaded();
				}catch(Exception e) {
					if(e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
						System.err.println("[TIMEOUTEXCEPTION] ESPERA CARGA SECCION COMENTARIOS SUPERADA.");
						throw e;
					}else {
						throw e;
					}
				}
			}
		}
		
		GroupPublication publication = new GroupPublication();
		
		publication = this.publicationHeaderDataExtraction();
		publication.setComments(this.publicationCommentsDataExtraction());
		
		return publication;
		
	}
	
	private boolean waitUntilCommentsLoaded() {
		ExpectedCondition<Boolean> commentSection = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS)).size() > 0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(200));
		return wait.until(commentSection);
	}
	
	/**
	 * Extraigo los comentarios de una publicación
	 * @return
	 * @throws Exception
	 */
	public List<Comment> publicationCommentsDataExtraction() throws Exception{
		Long tardo = System.currentTimeMillis();
		
		//capturo comentarios
		List<WebElement> commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		List<Comment> auxListaComments = new ArrayList<Comment>();
		if(commentElements.size()>0) {
			do {
				for (int k = 0; k < commentElements.size(); k++) {
					Comment auxComment = new Comment();
					
					// Usuario id
					if (this.getAccess() != null) {
						String ini = "id=";
						String fin = "&";
						String pathUserID = commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT)).getAttribute("data-hovercard");
						auxComment.setUserId(pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));
					}
					// Usuario name
					auxComment.setUserName(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT2)).getText());
					
					// Extraer likes.
					if (this.getAccess() != null) {
						auxComment.setCantLikes(Integer.valueOf(commentElements.get(k).findElement(By.xpath(".//span[contains(@class,'UFICommentLikeButton')]")).getText()));
					}
					if(commentElements.get(k).findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size()>0) {
						auxComment.setMensaje(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).getText());
					}else {
						auxComment.setMensaje("");
					}
														
					String href = commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_COMMENT_ID)).getAttribute("href");
					String[] auxId = href.split("&");
					String commentId = "";
					for(int i=0; i < auxId.length; i++){
						if(auxId[i].contains("comment_id=")) {
							commentId = auxId[i].split("=")[1];
							break;
						}
					}
					
					auxComment.setId(commentId);
					auxComment.setUTime(Long.parseLong(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime")));
					
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", commentElements.get(k));
					
					auxListaComments.add(auxComment);
				}
				
				System.out.print(auxListaComments.size() + "|");
				for (int j = 0; j < commentElements.size(); j++) {
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", commentElements.get(j));
				}
				if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).size()>0) {
					try{
						this.getDriver().findElement(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).click();
						this.waitUntilMoreCommentsClickLoad();
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
							System.out.println("[INFO] desapareció spinner de load comments OK.");
						}else {
							throw e;
						}
					}
				}
				commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
			//Cuando desaparezca el link de ver más mensajes y haya procesado todos los mensajes, sale del loop.-	
			}while(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).size()>0 || commentElements.size()>0);
			if(debug)
				System.out.println("TOTAL COMENTARIOS ENCONTRADOS: " + auxListaComments.size());
		}else {
			if(debug)
				System.out.println("LA PUBLICACION NO TIENE COMENTARIOS.");
		}
		
		tardo = System.currentTimeMillis() - tardo;
		if(debug)
			System.out.println("publicationCommentsDataExtraction tardo: " + tardo);
		
		return auxListaComments;
	}
		
	/**
	 * Extraigo la información general de la publicación.
	 * @return
	 * @throws Exception
	 */
	public GroupPublication publicationHeaderDataExtraction() throws Exception{
		long tardo = System.currentTimeMillis();
		
		GroupPublication aux = new GroupPublication();
		String likes = "0";
		if(this.getAccess() == null) {
			if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_LIKES_NL)).size()>0) {
				likes = this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_LIKES_NL)).getText();
				String regexp = "\\d+";
				String cadena = likes;
				Pattern pattern = Pattern.compile(regexp);
				Matcher matcher = pattern.matcher(cadena);
				matcher.find(); //Encuentra la primera coincidencia del patron
				
				int totLikes =0;
				try {
					//A Marta Sajnin, Monica Olivera, Jorge Diaz y 8 personas más les gusta esto.
					totLikes = Integer.parseInt(matcher.group()) + (likes.split(",")).length;
				}catch(Exception e) {
					if(e.getClass().getSimpleName().equalsIgnoreCase("IllegalStateException")) {
						if(likes.contains(",")) {
							totLikes = likes.split(",").length + 2;
						}else if(likes.contains("and") || likes.contains("y")){
							totLikes = 2;
						}else {
							totLikes = 1;
						}
					}
				}
				
				aux.setCantLikes(totLikes);
				
			}
		}else {
			if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_LIKES)).size()>0) {
				/**
				 * TODO: contar la cantidad de likes en base a likes de personas...
				 */
				likes = this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_LIKES)).getText();
				//System.out.println("TIENE LIKES: " + likes);
			}
		}
		
		
		/**
		 * Extraigo LINK del post, que es su ID.
		 */
		String postID = this.regexGroupPostID(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
		if (postID == "") {
			if (debug)
				System.out.println("[INFO] ERROR AL ENCONTRAR EL ID DEL POST: " + this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href"));
		} else {
			aux.setId(postID);
			aux.setUrl(FacebookConfig.URL + postID);
		}
		
		if(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_ID_1)).getAttribute("href").contains("sale_post_id")) {
			aux.setSalePost(true);
		}

		/**
		 * TIMESTAMP El timestamp viene en GMT.
		 */
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).size()>0) {
			aux.setUTime(Long.parseLong(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP)).getAttribute("data-utime")));
		} else if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).size()>0) {
			aux.setUTime(Long.parseLong(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TIMESTAMP_1)).getAttribute("data-utime")));
		}

		/**
		 * TITULO TODO HAY QUE VER QUE PASA CUANDO EL TEXTO DEL TITULO ES MUY LARGO... SI RECARGA LA PAGINA O LA MANTIENE EN LA MISMA.
		 */
	/*
		if (this.existElement(publication, FacebookConfig.XPATH_PUBLICATION_TITLE)) {
			// puede ser que una publicación no tenga título y puede ser que tenga un link
			// de "ver más", al cual hacerle click.
			this.clickViewMoreTextContent(publication, FacebookConfig.XPATH_PUBLICATION_TITLE_VER_MAS);
			aux.setTitulo(publication.findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_TITLE)).getText());
		} else {
			aux.setTitulo(null);
		}
*/
		/**
		 * CANTIDAD DE REPRODUCCIONES
		 */
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)).size()>0) {
			aux.setCantReproducciones(Integer.parseInt(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_REPRO)).getText().replaceAll("\\D+", "")));
		} else {
			aux.setCantReproducciones(null);
		}
		
		
		/**
		 * CANTIDAD DE SHARES
		 */
		
		if (this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).size()>0) {
			aux.setCantShare(Integer.parseInt(this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_CANT_SHARE)).getText().replaceAll("\\D+", "")));
		} else {
			aux.setCantShare(0);
		}
		
		
		/**
		 * OWNER
		 */
		String owner = "";
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER)).size()>0) {
			owner = this.getDriver().findElement(By.xpath(FacebookConfig.XPATH_PUBLICATION_OWNER)).getText();
			//System.out.println("TIENE OWNER: " + owner);
			aux.setOwner(owner);
		}
		
		/**
		 * valor
		 */
		String price = "0";
		//sacar el precio del producto. -->>//div[@class='_l56']/div
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_SALEPRICE)).size()>0) {
			price = this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_SALEPRICE)).getText();
			aux.setValue(price);
			//System.out.println("TIENE PRECIO: " + price);
		}
		
		/**
		 * titulo
		 */
		//sacar el titulo -->>//div[@class='_l53']/span[last()]
		String title = "0";
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_TITLE)).size()>0) {
			title = this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_TITLE)).getText();
			aux.setTitulo(title);
			//System.out.println("TIENE TITULO: " + title);
		}
		/**
		 * ubicacion
		 */
		//sacar la ubicación -->> //div[@class='_l56']/div[last()]
		String ubicacion = "";
		if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_PUBLICATION_LOCATION)).size()>0) {
			ubicacion = this.getDriver().findElement(By.xpath(FacebookConfig.XP_PUBLICATION_LOCATION)).getText();
			aux.setUbication(ubicacion);
			//System.out.println("TIENE UBICACION: " + ubicacion);
		}
		if(debug)
			System.out.println("publicationHeaderDataExtraction tardo: " + (System.currentTimeMillis()-tardo));
		
		return aux;
	}
	
	/**
	 * Le pasas la URL de la publicación, un rango de fechas y te 
	 * devuelve la lista de comentarios enconetrados entre esos rangos.
	 * @param groupPublicationURL
	 * @param FROM_UTIME
	 * @param TO_UTIME
	 * @return
	 * @throws Exception
	 */
	public List<Comment> updateGroupPublicationComments(String groupPublicationURL, Long FROM_UTIME, Long TO_UTIME) throws Exception{
		this.navigateTo(groupPublicationURL);
		//Click en la seccion de comentarios...
		if(this.getAccess()==null) {
			if(this.getDriver().findElements(By.xpath("//form[@class='commentable_item collapsed_comments']/descendant::a")).size()>0) {
				this.getDriver().findElement(By.xpath("//form[@class='commentable_item collapsed_comments']/descendant::a")).click();
				try {
					this.waitUntilCommentsLoaded();
				}catch(Exception e) {
					if(e.getClass().getSimpleName().equalsIgnoreCase("timeoutexception")) {
						System.err.println("[TIMEOUTEXCEPTION] ESPERA CARGA COMENTARIO SUPERADA.");
						throw e;
					}else {
						throw e;
					}
				}
			}
		}
		return this.publicationCommentsDataUpdate(FROM_UTIME, TO_UTIME);		
	}
	

	
	/**
	 * Extraigo los comentarios de una publicación entre un rango de fechas.
	 * condición de corte: encuentro comentarios > al TO o que ya se recorrieron todos los comments. 
	 * @param FROM_UTIME
	 * @param TO_UTIME
	 * @return
	 * @throws Exception
	 */
	private List<Comment> publicationCommentsDataUpdate(Long FROM_UTIME, Long TO_UTIME) throws Exception{
		long tardo = System.currentTimeMillis();
		//capturo comentarios
		if(debug)
			System.out.println("[INFO] >>>> PROCESANDO COMENTARIOS (publicationCommentsDataUpdate).");
		List<WebElement> commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		List<Comment> auxListaComments = new ArrayList<Comment>();
		int cantAnterior =0;
		
		//Puedo cortar por 2 condiciones: que no hayan comentarios entre las fechas (Entonces tengo que Sí o Sí recorrer 
		//todos los comentarios.
		// O porque ya consumí todos los que estaban en el rango... 
		commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS));
		if(commentElements.size()>0) {
			do {
				//Si hay comments con fecha mayor igual al corte de inicio...
				if(this.getDriver().findElements(By.xpath(FacebookConfig.GROUPPUB_COMMENTS_TIMESTAMP_CONDITION(FROM_UTIME))).size()>0) {
					if(this.getDriver().findElements(By.xpath(FacebookConfig.GROUPPUB_COMMENTS_TIMESTAMP_CONDITION_FROMTO(FROM_UTIME, TO_UTIME))).size()>0) {
						commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.GROUPPUB_COMMENTS_TIMESTAMP_CONDITION_FROMTO(FROM_UTIME, TO_UTIME)));
						for (int k = 0; k < commentElements.size(); k++) {
							Comment auxComment = new Comment();
							
							// Usuario id
							if (this.getAccess() != null) {
								String ini = "id=";
								String fin = "&";
								String pathUserID = commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT)).getAttribute("data-hovercard");
								auxComment.setUserId(pathUserID.substring(pathUserID.indexOf(ini) + (ini.length() + 1), pathUserID.indexOf(fin)));
							}
							// Usuario name
							auxComment.setUserName(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_ID_COMMENT2)).getText());
							
							// Extraer likes.
							if (this.getAccess() != null) {
								auxComment.setCantLikes(Integer.valueOf(commentElements.get(k).findElement(By.xpath(".//span[contains(@class,'UFICommentLikeButton')]")).getText()));
							}
							
							if(commentElements.get(k).findElements(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).size()>0) {
								auxComment.setMensaje(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_USER_COMMENT)).getText());
							}else {
								auxComment.setMensaje("");
							}
							String href = commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_COMMENT_ID)).getAttribute("href");
							String[] auxId = href.split("&");
							String commentId = "";
							for(int i=0; i < auxId.length; i++){
								if(auxId[i].contains("comment_id=")) {
									commentId = auxId[i].split("=")[1];
									break;
								}
							}
							auxComment.setId(commentId);
							auxComment.setUTime(Long.parseLong(commentElements.get(k).findElement(By.xpath(FacebookConfig.XPATH_COMMENT_UTIME)).getAttribute("data-utime")));
							
							((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", commentElements.get(k));
							
							auxListaComments.add(auxComment);
						}
						cantAnterior = auxListaComments.size();
						
					}else{
						//No hay comentarios entre el rango de fechas... aunque sí hay mayores al from.
						//Entonces, corto.
						break;
					}
				}else {
					//Se supone que la fecha FROM es la más vieja... y TO la más nueva.
					//Si no hay publicaciones más nuevas al FROM, entonces corto...
					break;
				}
				
				System.out.print(auxListaComments.size() + "|");
				for (int j = 0; j < commentElements.size(); j++) {
					((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].setAttribute('style', 'visibility:hidden')", commentElements.get(j));
				}
				
				if(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).size()>0) {
					try{
						this.getDriver().findElement(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).click();
						//Poner un wait after click. (sumar al de extracción de comments...)
						this.waitUntilMoreCommentsClickLoad();
					}catch(Exception e) {
						if(e.getClass().getSimpleName().equalsIgnoreCase("NoSuchElementException") || e.getClass().getSimpleName().equalsIgnoreCase("StaleElementReferenceException")) {
							System.out.println("[INFO] desapareció spinner de load comments OK.");
						}else {
							throw e;
						}
					}
				}
				
				if(cantAnterior > 0) { //Entonces, se obtuvieron comentarios entre el rango...
					commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.GROUPPUB_COMMENTS_TIMESTAMP_CONDITION_FROMTO(FROM_UTIME, TO_UTIME))).size()>0?this.getDriver().findElements(By.xpath(FacebookConfig.GROUPPUB_COMMENTS_TIMESTAMP_CONDITION_FROMTO(FROM_UTIME, TO_UTIME))):new ArrayList<WebElement>();
				}else{
					commentElements = this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)).size()>0?this.getDriver().findElements(By.xpath(FacebookConfig.XPATH_COMMENTS)):new ArrayList<WebElement>();
				}
			//Cuando desaparezca el link de ver más mensajes y haya procesado todos los mensajes, sale del loop.-	
			//Entonces siempre van a haber comments elements... por lo que este puiede ser un AND!!!!!!!!!
			//EL VERMAS, no me dejará incluir los últimos comentarios.	
			}while(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_VER_MAS_MSJS)).size()>0 || commentElements.size()>0);
			//}while(commentElements.size()>0);
			if(debug)
				System.out.println("TOTAL COMENTARIOS ENCONTRADOS: " + auxListaComments.size());
		}else {
			if(debug)
				System.out.println("LA PUBLICACION NO TIENE COMENTARIOS.");
		}
		
		if(debug)
			System.out.println("publicationCommentsDataUpdate tardo: " + (System.currentTimeMillis() - tardo));
		return auxListaComments;
	}
	
	private boolean waitUntilMoreCommentsClickLoad() {
		ExpectedCondition<Boolean> loadMore = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				if (driver.findElements(By.xpath(FacebookConfig.XP_SPINNERLOAD_COMMENTS)).size()>0) {
					return false;
				} else {
					return true;
				}
			}
		};
		Wait<WebDriver> wait = new FluentWait<WebDriver>(this.getDriver()).withTimeout(Duration.ofSeconds(WAIT_UNTIL_SECONDS * 2)).pollingEvery(Duration.ofMillis(200));
		return wait.until(loadMore);
	}
	
	/**
	 * Extraer la información del grupo. (Cantidad de seguidores, comentarios generados, descripción, etc.)
	 */
	public Group ExtractGroupInfo(String groupName) throws Exception{
		Group aux = new Group();
		
		this.navigateTo(FacebookConfig.URL+FacebookConfig.URL_GROUP+groupName+"/about");
		//Si accedió a la página de Login...
		if(this.getDriver().getCurrentUrl().contains("/login/")) {
			throw new Exception("[INFO] SE NECESITA ESTAR LOGUEADO PARA VER LA INFO DEL GRUPO: " + groupName);
		}
		
		//se puede extraer:
		// Descripcion
		if(this.getDriver().findElements(By.xpath("//div[@class='_j1y']//descendant::div[@role='heading']/p/span")).size()>0) {
			aux.setDescription(this.getDriver().findElement(By.xpath("//div[@class='_j1y']//descendant::div[@role='heading']/p/span")).getText());
		}
		//div[@class='_j1y']//descendant::div[@role='heading']/p/span
		//miembros
		//div[@class='_4bl7']/div/div (CAntidad)
		if(this.getDriver().findElements(By.xpath("//div[@class='_4bl7']/div/div")).size()>0) {
			aux.setCantMembers(Integer.parseInt(this.getDriver().findElement(By.xpath("//div[@class='_4bl7']/div/div")).getText()));
		}
		//ACtividad (publicaciones nuevas hoy y en los ultimos dias)
		//div[@class='_4bl7']/div/div/following-sibling::div//following-sibling::div
		//Hace cuanto fue creado.(FEcha de creación. UTIME)
		//div[@class='_ifv']//descendant::abbr (getAttribute data-utime)
		if(this.getDriver().findElements(By.xpath("//div[@class='_ifv']//descendant::abbr")).size()>0) {
			aux.setDateCreationUTime(Long.valueOf(this.getDriver().findElement(By.xpath("//div[@class='_ifv']//descendant::abbr")).getAttribute("data-utime")));
		}
		//Es cerrado o abierto por el candadito
		//i[@class='_3ph1 img sp_wgvnRQRltZf sx_9d1f94']
		if(this.getDriver().findElements(By.xpath("//i[@class='_3ph1 img sp_wgvnRQRltZf sx_9d1f94']")).size()>0) {
			aux.setLock(true);
		}else {
			aux.setLock(false);
		}
		
		return aux;
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
			System.out.println("navigateTo( "+URL+" ) tardo: " + aux);
		}
	}

	
	public String regexGroupPostID(String link) {
		//https://www.facebook.com/groups/caferacerar/permalink/3327979500590611/?sale_post_id=3327979500590611
		String[] stringArray = link.split("/");
		System.out.println("LINK:" + link);
		return stringArray[6];
	}
	

	public void scrollDown() {
		JavascriptExecutor jsx = (JavascriptExecutor) this.getDriver();
		jsx.executeScript("window.scrollTo(0, document.body.scrollHeight)");
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
	
	//***********SIN USO... PERO SE PODRÍA HACER QUE SCROLLEE 2 VECES, BUSCANDO DE ESTAS PUBLICACIONES...
		//1) Scroll 2 veces 2) filtro rango fechas 3) scroll 4) si se sumaron nuevas, entonces sigo... hasta que 
		//no se incremente más la cantidad......
	public List<WebElement> obtainGroupPublicationsHTMLByUTIME(String facebookGroup, Long uTimeIni, Long uTimeFin) throws Exception {
		long tardo = System.currentTimeMillis();
		this.navigateTo(FacebookConfig.URL+FacebookConfig.URL_GROUP+facebookGroup);
		
		
		
		
		
		
			long aux = System.currentTimeMillis();
			aux = System.currentTimeMillis();
			System.out.println("obtainGroupPublicationsHTML");
				
			if (debug)
				System.out.println("[INFO] BUSCANDO LAS PUBLICACIONES ENTRE LAS FECHAS: "+uTimeIni+" Y "+uTimeFin+".");
			
			List<WebElement> auxList = new ArrayList<WebElement>();
			auxList = this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUPPUBLICATION_TIMESTAMP_CONDITION(facebookGroup, uTimeIni, uTimeFin)));
			
			
			int tot=0;
			if (auxList.size() > 0) {
				tot = auxList.size();
				//while(!(this.getDriver().findElements(By.xpath("//div[@class='_5pcb']/div[@class='_4-u2 mbm _4mrt _5jmm _5pat _5v3q _4-u8']"+FacebookConfig.XP_GROUP_PUBLICATION_TIMESTAMP_CONDITION_SATISFIED(facebookGroup, uTIME_INI))).size() > 0)){
				/*
				while(!(this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUP_PUBLICATIONS_OLD_CONTAINER)).size()>= cantidadPublicacionesInicial)) {	
					this.scrollDown();
					auxList = this.getDriver().findElements(By.xpath(FacebookConfig.XP_GROUP_PUBLICATIONS_OLD_CONTAINER));
					
					//Control de corte por no haber más publicaciones...
					try {
						this.waitUntilGroupPubsLoad(tot);
						tot = auxList.size();
					}catch(Exception e) {
						break;
					}
					
				}*/
			} else {
				if (debug) {
					System.err.println("[INFO] EL GRUPO NO TIENE NUNGUNA PUBLICACION");
					this.saveScreenShot("GRUPO_SIN_PUBS");
				}
				throw new Exception("[INFO] EL GRUPO NO TIENE NUNGUNA PUBLICACION");
			}	
			
			//retorno hasta las primeras cantidad de publicaciones indicadas.
			//return auxList.size()>=cantidadPublicacionesInicial?auxList.subList(0, cantidadPublicacionesInicial):auxList;
			return null;
			
				
		}

}
