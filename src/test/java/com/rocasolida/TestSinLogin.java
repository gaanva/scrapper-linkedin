package com.rocasolida;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.rocasolida.scrapperfacebook.entities.GroupPublication;
import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.scrap.FacebookGroupScrap;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class TestSinLogin {

	private static String OS;
	private static final boolean DEBUG = true;

	@BeforeClass
	public static void loadOs() {
		OS = ScrapUtils.getOSName();
	}
	
	/*
	@Test
	public void groupPublicationsHTMLFilteredByUTime() throws MalformedURLException {
		System.out.println("------> groupPublicationsHTMLFilteredByUTime");
		Long PUBSuTIME_INI = 1532476800L; // 25/07/2018 @ 00:00:00
		Long PUBSuTIME_FIN = 1532822400L; // 28/07/2018 @ 24:00:00

		//Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
		//Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00

		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		Page page = null;
		List<WebElement> aux = new ArrayList<WebElement>();
		try {
			//CAFE RACER ARGENTINA
			aux = fg.obtainGroupPublicationsHTML("2279543105434261", PUBSuTIME_INI, PUBSuTIME_FIN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fg.quit();
		System.out.println("Cantidad de pubs: " + aux.size());
	}
	
	@Test
	public void groupNewsFeedPublicationsHTMLFilteredByUTime() throws MalformedURLException {
		System.out.println("------> groupNewsFeedPublicationsHTMLFilteredByUTime");
		Long PUBSuTIME_INI = 1532476800L; // 25/07/2018 @ 00:00:00
		Long PUBSuTIME_FIN = 1532822400L; // 28/07/2018 @ 24:00:00

		//Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
		//Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00

		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		Page page = null;
		List<WebElement> aux = new ArrayList<WebElement>();
		try {
			//CAFE RACER ARGENTINA
			aux = fg.obtainGroupNewsFeedPublicationsHTML("2279543105434261", PUBSuTIME_INI, PUBSuTIME_FIN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fg.quit();
		System.out.println("Cantidad de pubs: " + aux.size());
	}
	*/
	
	@Test
	public void groupPublicationsIDFilteredByUTime() throws MalformedURLException {
		System.out.println("-----> groupPublicationsIDFilteredByUTime");
		Long PUBSuTIME_INI = 1532476800L; // 25/07/2018 @ 00:00:00
		Long PUBSuTIME_FIN = 1532822400L; // 28/07/2018 @ 24:00:00

		//Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
		//Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00

		FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		Page page = null;
		List<GroupPublication> aux = new ArrayList<GroupPublication>();
		try {
			//CAFE RACER ARGENTINA
			aux = fg.obtainGroupPubsWithoutComments("2279543105434261", PUBSuTIME_INI, PUBSuTIME_FIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i=0; i<aux.size(); i++) {
			System.out.println("URL("+i+") " + aux.get(i).getUrl());
			System.out.println("ID("+i+") " + aux.get(i).getId());
			System.out.println("VENDIBLE("+i+") " + aux.get(i).isSalePost());
			System.out.println("UTIME: ("+i+") " + aux.get(i).getUTime());
			System.out.println("LIKES: ("+i+") " + aux.get(i).getCantLikes());
			System.out.println("SHARES: ("+i+") " + aux.get(i).getCantShare());
		}
		
		fg.quit();
		System.out.println("Cantidad de pubs: " + aux.size());
	}
	

//	@Test
//	public void mauriciomacriConFiltroComments1Mes() throws MalformedURLException {
//		// Test para 1 solo comentario el 06/06.
//		Long uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
//		Long uTIME_FIN = 1528917098L; // 13/06/2018 @ 24:00:00
//
//		Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
//		Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00
//
//		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
//		Page page = null;
//		try {
//			page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		fs.quit();
//		System.out.println("page: " + page);
//
//		assertNotNull(page.getPublications());
//		assertEquals(8, page.getPublications().size());
//	}

//	@Test
//	public void teamisurusConFiltroComments() throws MalformedURLException {
//		// Test para 1 solo comentario el 06/06.
//		long aux = System.currentTimeMillis();
//		Long uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs;
//		Long uTIME_FIN = 1528329599L;// 06/06/2018 23:59:59hs;
//
//		Long COMMENTS_uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs
//		Long COMMENTS_uTIME_FIN = 1528761600L;// 11/06/2018 24:00:00hs
//		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
//		Page page = null;
//		try {
//			page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		fs.quit();
//		System.out.println("page: " + page);
//		aux = System.currentTimeMillis() - aux;
//		System.out.println("Tardo total: " + aux);
//		assertNotNull(page.getPublications());
//		assertEquals(2, page.getPublications().size());
//		System.out.println("comentarios 1: " + page.getPublications().get(0).getComments().size());
//		assertEquals(1, page.getPublications().get(0).getComments().size());
//		System.out.println("comentarios 2: " + page.getPublications().get(1).getComments().size());
//		assertEquals(22, page.getPublications().get(1).getComments().size());
//	}
//
//	@Test
//	public void teamisurusSinFiltroComments() throws MalformedURLException {
//		// Test para 1 solo comentario el 06/06.
//		Long uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs;
//		Long uTIME_FIN = 1528329599L;// 06/06/2018 23:59:59hs;
//
//		Long COMMENTS_uTIME_INI = null;// 06/06/2018 00:00:00hs
//		Long COMMENTS_uTIME_FIN = null;// 11/06/2018 24:00:00hs
//		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
//		Page page = null;
//		try {
//			page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		fs.quit();
//		System.out.println("page: " + page);
//
//		assertNotNull(page.getPublications());
//		assertEquals(2, page.getPublications().size());
//
//		assertEquals(1, page.getPublications().get(0).getComments().size());
//		assertEquals(22, page.getPublications().get(1).getComments().size());
//	}
//
//	@Test
//	public void centroKinesiologiaConFiltroComments() throws MalformedURLException {
//		// Test para controlar post SIN comentarios.
//		Long uTIME_INI = 1526428800L; // 16/05/2018 @ 00:00:00
//		Long uTIME_FIN = 1526515200L; // 16/05/2018 @ 24:00:00
//
//		Long COMMENTS_uTIME_INI = 1526428800L; // 16/05/2018 @ 00:00:00
//		Long COMMENTS_uTIME_FIN = 1528761600L; // 11/06/2018 24:00:00
//		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
//		Page page = null;
//		try {
//			page = fs.obtainPageInformation("CKED-Centro-kinésico-y-Entrenamiento-Deportivo-154152138076469", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		fs.quit();
//		System.out.println("page: " + page);
//
//		assertNotNull(page.getPublications());
//		assertEquals(1, page.getPublications().size());
//
//		assertEquals(null, page.getPublications().get(0).getComments());
//
//	}
//
//	@Test
//	public void mauriciomacriConFiltroComments() throws MalformedURLException {
//		// Test para 1 solo comentario el 06/06.
//		Long uTIME_INI = 1528156800L; // 05/06/2018 @ 00:00:00
//		Long uTIME_FIN = 1528243200L; // 05/06/2018 @ 24:00:00
//
//		Long COMMENTS_uTIME_INI = 1528588800L; // 10/06/2018 @ 00:00:00
//		Long COMMENTS_uTIME_FIN = 1528761600L; // 11/06/2018 24:00:00
//
//		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
//		Page page = null;
//		try {
//			page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		fs.quit();
//		System.out.println("page: " + page);
//
//		assertNotNull(page.getPublications());
//		assertEquals(1, page.getPublications().size());
//
//		System.out.println("Cantidad mensajes: " + page.getPublications().get(0).getComments().size());
//		assertEquals(28, page.getPublications().get(0).getComments().size());
//
//	}

	/*
	 * @Test public void teamisurusSinLoginSinFiltroComments() throws MalformedURLException { Long uTIME_INI = getFecha("15/3/2018 00:00"); Long uTIME_FIN = getFecha("16/3/2018 00:00"); Long COMMENTS_uTIME_INI = null; Long COMMENTS_uTIME_FIN = null; FacebookScrap fs = new
	 * FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; try { page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * fs.quit(); assertNotNull(page.getPublications()); assertEquals(1, page.getPublications().size()); assertEquals(2, page.getPublications().get(0).getComments().size()); }
	 * 
	 * 
	 * @Test public void herbalifeSinLoginConFiltroComments() throws MalformedURLException { Long uTIME_INI = getFecha("15/3/2018 00:00"); Long uTIME_FIN = getFecha("16/3/2018 00:00"); Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00"); Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
	 * FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; try { page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } fs.quit(); // System.out.println(publications); assertNotNull(page); assertEquals(1, page.getPublications().size()); // Total de comentarios con el filtro aplicado: assertEquals(4, page.getPublications().get(0).getComments().size()); }
	 * 
	 * @Test public void herbalifeSinLoginSinFiltroComments() throws MalformedURLException { Long uTIME_INI = getFecha("15/3/2018 00:00"); Long uTIME_FIN = getFecha("16/3/2018 00:00"); Long COMMENTS_uTIME_INI = null; Long COMMENTS_uTIME_FIN = null; FacebookScrap fs = new
	 * FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; try { page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * fs.quit(); // System.out.println(publications); assertNotNull(page); assertEquals(1, page.getPublications().size()); // Total de comentarios con el filtro aplicado: assertEquals(10, page.getPublications().get(0).getComments().size()); }
	 * 
	 * 
	 * @Test public void mauriciomacriSinLoginConFiltroComments() throws MalformedURLException { Long uTIME_INI = 1524106800L; // 04/19/2018 @ 03:00:00 Long uTIME_FIN = 1524381248L; // 04/22/2018 @ 07:14:08 Long COMMENTS_uTIME_INI = 1524106800L; Long COMMENTS_uTIME_FIN = 1524381248L; FacebookScrap
	 * fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG); Page page = null; try { page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } fs.quit(); assertNotNull(page.getPublications()); assertEquals(2, page.getPublications().size()); assertTrue(page.getPublications().get(0).getComments().size() > 184); assertTrue(page.getPublications().get(1).getComments().size() > 500); }
	 * 
	 * private Long getFecha(String string) { try { String pattern = "dd/MM/yyyy HH:mm"; SimpleDateFormat format = new SimpleDateFormat(pattern); Date date = format.parse(string); return date.getTime() / 1000; } catch (Exception ex) { ex.printStackTrace(); } return null; }
	 */
}
