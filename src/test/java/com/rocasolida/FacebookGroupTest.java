package com.rocasolida;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Comment;
import com.rocasolida.scrapperfacebook.entities.Group;
import com.rocasolida.scrapperfacebook.entities.GroupPublication;
import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.scrap.FacebookGroupScrap;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookGroupTest {

	private static String OS;
	private static final boolean DEBUG = true;

	@BeforeClass
	public static void loadOs() {
		OS = ScrapUtils.getOSName();
	}

	/*
	 * @Test public void groupPublicationsHTMLFilteredByUTime() throws MalformedURLException { System.out.println("------> groupPublicationsHTMLFilteredByUTime"); Long PUBSuTIME_INI = 1532476800L; // 25/07/2018 @ 00:00:00 Long PUBSuTIME_FIN = 1532822400L; // 28/07/2018 @ 24:00:00
	 * 
	 * //Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00 //Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00
	 * 
	 * FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; List<WebElement> aux = new ArrayList<WebElement>(); try { //CAFE RACER ARGENTINA aux = fg.obtainGroupPublicationsHTML("2279543105434261", PUBSuTIME_INI, PUBSuTIME_FIN); } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * fg.quit(); System.out.println("Cantidad de pubs: " + aux.size()); }
	 * 
	 * @Test public void groupNewsFeedPublicationsHTMLFilteredByUTime() throws MalformedURLException { System.out.println("------> groupNewsFeedPublicationsHTMLFilteredByUTime"); Long PUBSuTIME_INI = 1532476800L; // 25/07/2018 @ 00:00:00 Long PUBSuTIME_FIN = 1532822400L; // 28/07/2018 @ 24:00:00
	 * 
	 * //Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00 //Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00
	 * 
	 * FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; List<WebElement> aux = new ArrayList<WebElement>(); try { //CAFE RACER ARGENTINA aux = fg.obtainGroupNewsFeedPublicationsHTML("2279543105434261", PUBSuTIME_INI, PUBSuTIME_FIN); } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * fg.quit(); System.out.println("Cantidad de pubs: " + aux.size()); }
	 */

	/**
	 * Ejemplo de como se deben usar ambos métodos, 1) para recorrer todas(según cantidad ingresada) las publicaciones de la página principal. Y el 2) para tomar cada una página principal extraerle el resto de sus datos...
	 * 
	 * @throws Exception
	 */
	@Test
	public void groupPublicationsByQuantity() throws Exception {
		System.out.println("-----> groupPublicationsByQuantity");
		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		String GROUP = "cristinapresidenta";

		Page page = null;
		// Si no, se extrae la cantidad máxima que se encuentra en el grupo.
		int CANTPUBS = 10;
		Group aux = new Group();
		try {
			// Paso 1) Extraigo las publicaciones[ID,URL y UTIME] de la página principal, en base a una cantidad.
			// ACtualizar LIKES y ver por qué repite un mensaje 4 veceS?
			aux = fg.obtainGroupPubsWithoutComments(GROUP, CANTPUBS, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		GroupPublication gp = new GroupPublication();

		for (int i = 0; i < aux.getPublications().size(); i++) {
			System.out.println("*****************************************************************************************");

			System.out.println("URL(" + i + ") " + aux.getPublications().get(i).getUrl());
			System.out.println("ID(" + i + ") " + aux.getPublications().get(i).getId());
			System.out.println("UTIME: (" + i + ") " + aux.getPublications().get(i).getUTime());

			// PAso 2) Proceso cada una de las publications encontradas.
			gp = fg.obtainFullPubInformation(aux.getPublications().get(i).getUrl());
			System.out.println("ID PUB: " + gp.getId()); // ok
			System.out.println("OWNER: " + gp.getOwner()); // ok
			System.out.println("TITULO: " + gp.getTitulo()); // ok
			System.out.println("UBICACION: " + gp.getUbication()); // ok
			System.out.println("URL: " + gp.getUrl());// ok
			System.out.println("VALOR: " + gp.getValue());// ok
			System.out.println("LIKES: " + gp.getCantLikes()); //
			System.out.println("REPRODUCCIONES: " + gp.getCantReproducciones());
			System.out.println("SHARES: " + gp.getCantShare());
			System.out.println("UTIME: " + gp.getUTime());
			System.out.println("VENDIBLE: " + gp.isSalePost());

			for (int j = 0; j < gp.getComments().size(); j++) {
				System.out.println("ID(" + j + ") " + gp.getComments().get(j).getId());
				System.out.println("MENSAJE(" + j + ") " + gp.getComments().get(j).getMensaje());
				System.out.println("UTIME: (" + j + ") " + gp.getComments().get(j).getUTime());
				System.out.println("USER NAME: (" + j + ") " + gp.getComments().get(j).getUserName());
			}

			System.out.println("*****************************************************************************************");

		}

		fg.quit();
		System.out.println("Cantidad de pubs: " + aux.getPublications().size());
	}

	/**
	 * Se Procesa una Publicación, en base a su URL.
	 * 
	 * @throws Exception
	 */
	@Test
	public void groupPubFullDataExtraction() throws Exception {
		System.out.println("-----> groupPubFullDataExtraction");
		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		Page page = null;

		GroupPublication gp = new GroupPublication();
		try {
			// SE llama al proceso que extrae los datos de una publicación en base a la URL de la misma.
			// gp = fg.obtainFullPubInformation("https://www.facebook.com/cristinapresidenta/posts/483043521740444");
			gp = fg.obtainFullPubInformation("https://www.facebook.com/groups/cristinapresidenta/permalink/10156612898533850/");
		} catch (Exception e) {
			e.printStackTrace();
		}

		fg.quit();

		// PUBLICATION INFO
		System.out.println("CANT COMENTARIOS: " + gp.getComments().size());
		System.out.println("ID PUB: " + gp.getId()); // ok
		System.out.println("OWNER: " + gp.getOwner()); // ok
		System.out.println("TITULO: " + gp.getTitulo()); // ok
		System.out.println("UBICACION: " + gp.getUbication()); // ok
		System.out.println("URL: " + gp.getUrl());// ok
		System.out.println("VALOR: " + gp.getValue());// ok
		System.out.println("LIKES: " + gp.getCantLikes()); //
		System.out.println("REPRODUCCIONES: " + gp.getCantReproducciones());
		System.out.println("SHARES: " + gp.getCantShare());
		System.out.println("UTIME: " + gp.getUTime());
		System.out.println("VENDIBLE: " + gp.isSalePost());

		for (int i = 0; i < gp.getComments().size(); i++) {
			System.out.println("ID(" + i + ") " + gp.getComments().get(i).getId());
			System.out.println("MENSAJE(" + i + ") " + gp.getComments().get(i).getMensaje());
			System.out.println("UTIME: (" + i + ") " + gp.getComments().get(i).getUTime());
			System.out.println("USER NAME: (" + i + ") " + gp.getComments().get(i).getUserName());
		}

		// System.out.println("Cantidad de pubs: " + aux.size());
	}

	@Test
	public void updateGroupPublicationByUtimes() throws Exception {
		System.out.println("-----> updateGroupPublicationByUtimes()");
		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		Long FROM_uTIME = 1535024890L; // 23/08/2018 @ 08:48:00 //FECHA MÁS VIEJA.
		Long TO_uTIME = 1535113696L; // 24/08/2018 @ 09:28:00 //FECHA MÁS NUEVA.

		List<Comment> comentarios_nuevos = new ArrayList<Comment>();
		try {
			// SE llama al proceso que extrae los datos de una publicación en base a la URL de la misma.
			// gp = fg.obtainFullPubInformation("https://www.facebook.com/cristinapresidenta/posts/483043521740444");
			// fg.obtainFullPubInformation("https://www.facebook.com/groups/cristinapresidenta/permalink/10156612898533850/");
			comentarios_nuevos = fg.updateGroupPublicationComments("https://www.facebook.com/groups/cristinapresidenta/permalink/10156612898533850/", FROM_uTIME, TO_uTIME);

		} catch (Exception e) {
			e.printStackTrace();
		}

		fg.quit();
		System.out.println("CANTIDAD COMENTARIOS: " + comentarios_nuevos.size());
	}
	
	
	@Test
	public void groupPublicationsByUTime() throws Exception {
		System.out.println("-----> groupPublicationsByUTime");
		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		String GROUP = "cristinapresidenta";

		Page page = null;
		// Si no, se extrae la cantidad máxima que se encuentra en el grupo.
		int CANTPUBS = 0;
		Long UTIMEFROM = 1536004052L;//03092018 12:47
		Long UTIMETO = 1536025148L;//03092018 18:39 (Conte 11 pubs en este rango)
		Group aux = new Group();
		try {
			// Paso 1) Extraigo las publicaciones[ID,URL y UTIME] de la página principal, en base a una cantidad.
			// ACtualizar LIKES y ver por qué repite un mensaje 4 veceS?
			aux = fg.obtainGroupPubsWithoutComments(GROUP, CANTPUBS, UTIMEFROM, UTIMETO);
		} catch (Exception e) {
			e.printStackTrace();
		}

		GroupPublication gp = new GroupPublication();

		for (int i = 0; i < aux.getPublications().size(); i++) {
			System.out.println("*****************************************************************************************");

			System.out.println("URL(" + i + ") " + aux.getPublications().get(i).getUrl());
			System.out.println("ID(" + i + ") " + aux.getPublications().get(i).getId());
			System.out.println("UTIME: (" + i + ") " + aux.getPublications().get(i).getUTime());

			// PAso 2) Proceso cada una de las publications encontradas.
			gp = fg.obtainFullPubInformation(aux.getPublications().get(i).getUrl());
			System.out.println("ID PUB: " + gp.getId()); // ok
			System.out.println("OWNER: " + gp.getOwner()); // ok
			System.out.println("TITULO: " + gp.getTitulo()); // ok
			System.out.println("UBICACION: " + gp.getUbication()); // ok
			System.out.println("URL: " + gp.getUrl());// ok
			System.out.println("VALOR: " + gp.getValue());// ok
			System.out.println("LIKES: " + gp.getCantLikes()); //
			System.out.println("REPRODUCCIONES: " + gp.getCantReproducciones());
			System.out.println("SHARES: " + gp.getCantShare());
			System.out.println("UTIME: " + gp.getUTime());
			System.out.println("VENDIBLE: " + gp.isSalePost());

			for (int j = 0; j < gp.getComments().size(); j++) {
				System.out.println("ID(" + j + ") " + gp.getComments().get(j).getId());
				System.out.println("MENSAJE(" + j + ") " + gp.getComments().get(j).getMensaje());
				System.out.println("UTIME: (" + j + ") " + gp.getComments().get(j).getUTime());
				System.out.println("USER NAME: (" + j + ") " + gp.getComments().get(j).getUserName());
			}

			System.out.println("*****************************************************************************************");

		}

		fg.quit();
		System.out.println("Cantidad de pubs: " + aux.getPublications().size());
	}

}
