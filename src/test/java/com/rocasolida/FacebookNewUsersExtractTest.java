package com.rocasolida;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.entities.User;
import com.rocasolida.scrapperfacebook.scrap.FacebookNewUsersExtract;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookNewUsersExtractTest {

	private static String OS;
	private static final boolean DEBUG = true;

	@BeforeClass
	public static void loadOs() {
		OS = ScrapUtils.getOSName();
	}

	@Test
	/**
	 * Caso de prueba para buscar y devolver el total de usuarios pedido.
	 * 
	 * @throws Exception
	 */
	public void Page_con_cantUsuarios_objetivo() throws MalformedURLException {
		//Cantidad de usuarios a extraer...
		int CANT_USERS = 250;
		//pagina donde buscar usuarios.
		String page = "marcelotinelli";
		
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		// Credential access = new Credential("nahuelmontoya2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookNewUsersExtract fu = new FacebookNewUsersExtract(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		List<String> users = null;
		List<User> usuarios;
		System.out.println("PAGE: " + page);
		try {
			fu.login(access);
			users = fu.obtainUsersInformationFromComment(page, null, CANT_USERS);
			/*usuarios = fu.obtainUserProfileInformation(users);
			for (int i = 0; i < usuarios.size(); i++) {
				System.out.println("USUARIO"+(i+1)+"): " + usuarios.get(i).toString());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		assertNotNull(users);
		assertTrue(users.size() == CANT_USERS);

	}

	
	/**
	 * CASO DE PRUBEA CON LISTA DE POSTS URLS!
	 */
	@Test
	/**
	 * Caso de prueba para buscar en un urlPosty devolver el total de usuarios pedido.
	 * 
	 * @throws Exception
	 */
	public void post_con_cantUsuarios_objetivo() throws MalformedURLException {
		//Cantidad de usuarios a extraer...
		int CANT_USERS = 190;
		//pagina donde buscar usuarios.
		String URL_POST ="https://www.facebook.com/C5N.Noticias/posts/10157697944845839";
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		// Credential access = new Credential("nahuelmontoya2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookNewUsersExtract fu = new FacebookNewUsersExtract(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		List<String> users = null;
		List<User> usuarios;
		System.out.println("URL POST: " + URL_POST);
		
		try {
			fu.login(access);
			users = fu.obtainUsersInformationFromComment(null,URL_POST, CANT_USERS);
			/*usuarios = fu.obtainUserProfileInformation(users);
			for (int i = 0; i < usuarios.size(); i++) {
				System.out.println("USUARIO"+(i+1)+"): " + usuarios.get(i).toString());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		assertNotNull(users);
		assertTrue(users.size() == CANT_USERS);

	}
	
	@Test
	/**
	 * info de los usuarios de la lista, si es que interactuaron en la publicación indicada.
	 * 
	 * @throws Exception
	 */
	public void post_con_EspecificosUsuariosEnPub() throws MalformedURLException {
		//Cantidad de usuarios a extraer...
		int CANT_USERS_FOUND = 4;
		//pagina donde buscar usuarios.
		String URL_POST ="https://www.facebook.com/C5N.Noticias/posts/10157697944845839";
		List<String> USR_SCR_NAMES = new ArrayList<String>();
		USR_SCR_NAMES.add("Carlos Ariel Morales");
		USR_SCR_NAMES.add("Daniel Montiel");
		USR_SCR_NAMES.add("Luis Alberto Sallese Rossi");
		USR_SCR_NAMES.add("elroquefortdelomas de samora"); //Este es el FAKE.
		USR_SCR_NAMES.add("Chizzo Lmds"); 
		
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		// Credential access = new Credential("nahuelmontoya2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookNewUsersExtract fu = new FacebookNewUsersExtract(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		List<User> usuarios;
		List<String> users = new ArrayList<String>();
		List<String> usersFound = new ArrayList<String>();
		System.out.println("URL POST: " + URL_POST);
		
		try {
			fu.login(access);
			users = fu.obtainSpecificsUsersInformationFromComments(USR_SCR_NAMES,URL_POST);
			
			for(int i=0; i<users.size(); i++) {
				if(!(users.get(i).contains("NO SE ENCONTRO: "))) {
					//Si se encontrolo guardo...
					usersFound.add(users.get(i));
				}else {
					//Si no se encontró lo informo...
					System.out.println(users.get(i));
				}
			}
			//Paso la lista de encontrados para obtener el profile info:
			/*usuarios = fu.obtainUserProfileInformation(usersFound);
			for (int i = 0; i < usuarios.size(); i++) {
				System.out.println("USUARIO"+(i+1)+"): " + usuarios.get(i).toString());
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		
		assertTrue(usersFound.size() == CANT_USERS_FOUND);
		assertTrue(!usersFound.contains("elroquefortdelomas de samora"));
		
	}
	
	
	/**
	 * Caso de prueba para buscar hasta la cantidad que me pasan por parámetro.
	 * 
	 * @throws Exception
	 */
/*
	public void Page__con_cantUsuarios_objetivo() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		// Credential access = new Credential("nahuelmontoya2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookUserLikesScrap fu = new FacebookUserLikesScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		List<UserLike> likesPages = null;

		String perfil = "liliana.novello.9";
		System.out.println("PERFIL: " + perfil);
		try {
			fu.login(access);
			likesPages = fu.obtainProfileLikes(perfil);
			for (int i = 0; i < likesPages.size(); i++) {
				System.out.println("TITLE " + (i + 1) + ":" + likesPages.get(i).getTitle());
				System.out.println("URL " + (i + 1) + ":" + likesPages.get(i).getUrl());
				System.out.println("CATEGORY " + (i + 1) + ":" + likesPages.get(i).getCategory());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		assertNotNull(likesPages);
		assertTrue(likesPages.size() >= 27);

	}
*/	
	/**
	 * Caso de prueba para buscar en una página sin POSts / comentarios.
	 * 
	 * @throws Exception
	 */
/*	
	public void Page_sin_posts_o_comentarios() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		// Credential access = new Credential("nahuelmontoya2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookUserLikesScrap fu = new FacebookUserLikesScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		List<UserLike> likesPages = null;

		String perfil = "liliana.novello.9";
		System.out.println("PERFIL: " + perfil);
		try {
			fu.login(access);
			likesPages = fu.obtainProfileLikes(perfil);
			for (int i = 0; i < likesPages.size(); i++) {
				System.out.println("TITLE " + (i + 1) + ":" + likesPages.get(i).getTitle());
				System.out.println("URL " + (i + 1) + ":" + likesPages.get(i).getUrl());
				System.out.println("CATEGORY " + (i + 1) + ":" + likesPages.get(i).getCategory());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		assertNotNull(likesPages);
		assertTrue(likesPages.size() >= 27);

	}
*/
	// @Test
	// /**
	// * si no tenemos acceso a los likes del perfil, porque no tenemos permisos, Entonces se devuelve una Excepción. (Porque no se sabe si es que tiene o no likes.) Se podría devolver NULL... no se el brunoli crwaler nasa como lo quiere procesar.[LINEA #134 del scrapper.]
	// *
	// * @throws Exception
	// */
	// public void Profile_SINACCESO_Likes() throws Exception {
	// Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
	// FacebookUserLikesScrap fu = new FacebookUserLikesScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
	// List<UserLike> likesPages = null;
	//
	// String perfil = "lilianapatricia.locicero";
	//
	// try {
	// fu.login(access);
	// likesPages = fu.obtainProfileLikes(perfil);
	// for (int i = 0; i < likesPages.size(); i++) {
	// System.out.println("TITLE " + (i + 1) + ":" + likesPages.get(i).getTitle());
	// System.out.println("URL " + (i + 1) + ":" + likesPages.get(i).getUrl());
	// System.out.println("CATEGORY " + (i + 1) + ":" + likesPages.get(i).getCategory());
	// }
	// } catch (Exception e) {
	// if (e.getMessage().equals("SIN_PERMISOS_VER_LIKES")) {
	// System.out.println("NO TENEMOS PERMISOS PARA VER LOS 'ME GUSTA' DEL PERFIL REFERIDO. (" + perfil + ")");
	// } else {
	// e.printStackTrace();
	// }
	// }
	// fu.quit();
	//
	// assertNull(likesPages);
	//
	// }
	//
	// @Test
	// /**
	// * El usuario muestra opción de "Likes" Pero no le dio like a ninguna página. Entonces te devuelve un array de "UserLikes" con tamaño 0.
	// *
	// * @throws Exception
	// */
	// public void Profile_SIN_Likes() throws Exception {
	// Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
	// FacebookUserLikesScrap fu = new FacebookUserLikesScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
	// List<UserLike> likesPages = null;
	//
	// String perfil = "estela.quilmes.5";
	// System.out.println("PERFIL: " + perfil);
	// try {
	// fu.login(access);
	// likesPages = fu.obtainProfileLikes(perfil);
	// for (int i = 0; i < likesPages.size(); i++) {
	// System.out.println("TITLE " + (i + 1) + ":" + likesPages.get(i).getTitle());
	// System.out.println("URL " + (i + 1) + ":" + likesPages.get(i).getUrl());
	// System.out.println("CATEGORY " + (i + 1) + ":" + likesPages.get(i).getCategory());
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// fu.quit();
	// System.out.println("PROFILE SIN LIKES");
	// assertTrue(likesPages.size() == 0);
	//
	// }

}
