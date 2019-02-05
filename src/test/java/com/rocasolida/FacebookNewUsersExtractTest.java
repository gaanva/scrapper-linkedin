package com.rocasolida;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.ArrayList;
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
		int CANT_USERS = 5;
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
			users = fu.obtainUsersCommentInformation(page,CANT_USERS);
			usuarios = fu.obtainUserProfileInformation(users);
			for (int i = 0; i < usuarios.size(); i++) {
				System.out.println("USUARIO"+(i+1)+"): " + usuarios.get(i).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fu.quit();

		assertNotNull(users);
		assertTrue(users.size() == CANT_USERS);

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
