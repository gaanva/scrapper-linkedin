package com.rocasolida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.scrap.FacebookScrap;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookPageTest {
	private static String OS = ScrapUtils.getOSName();
	private static final boolean DEBUG = true;
	private static DriverType dt = DriverType.FIREFOX_HEADLESS;

	@BeforeClass
	public static void loadOs() {
	}

	// @Test
	// public void mauriciomacriConFiltroComments1Mes() throws MalformedURLException {
	// // Test para 1 solo comentario el 06/06.
	// Long uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
	// Long uTIME_FIN = 1528917098L; // 13/06/2018 @ 24:00:00
	//
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = null;
	// try {
	// page = fs.obtainPageInformationWithoutComments("mauriciomacri", uTIME_INI, uTIME_FIN);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// fs.quit();
	// System.out.println("page: " + page);
	//
	// assertNotNull(page.getPublications());
	// assertEquals(8, page.getPublications().size());
	// }
	//
	// @Test
	// public void teamisurusConFiltroComments() throws Exception {
	// // Test para 1 solo comentario el 06/06.
	// long aux = System.currentTimeMillis();
	// Long uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs;
	// Long uTIME_FIN = 1528329599L;// 06/06/2018 23:59:59hs;
	//
	// Long COMMENTS_uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs
	// Long COMMENTS_uTIME_FIN = 1528761600L;// 11/06/2018 24:00:00hs
	//
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = null;
	// page = fs.obtainPageInformationWithoutComments("teamisurus", uTIME_INI, uTIME_FIN);
	// fs.quit();
	//
	// System.out.println("page: " + page);
	// aux = System.currentTimeMillis() - aux;
	// System.out.println("Tardo total: " + aux);
	// assertNotNull(page.getPublications());
	// assertEquals(2, page.getPublications().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("teamisurus", page.getPublications().get(0).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// System.out.println("comentarios 1: " + pub.getComments().size());
	// assertEquals(1, pub.getComments().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// pub = fs.obtainPostInformation("teamisurus", page.getPublications().get(1).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// System.out.println("comentarios 2: " + pub.getComments().size());
	// assertEquals(20, pub.getComments().size());
	// }
	//
	// @Test
	// public void teamisurusSinFiltroComments() throws Exception {
	// // Test para 1 solo comentario el 06/06.
	// Long uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs;
	// Long uTIME_FIN = 1528329599L;// 06/06/2018 23:59:59hs;
	//
	// Long COMMENTS_uTIME_INI = null;// 06/06/2018 00:00:00hs
	// Long COMMENTS_uTIME_FIN = null;// 11/06/2018 24:00:00hs
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = null;
	// page = fs.obtainPageInformationWithoutComments("teamisurus", uTIME_INI, uTIME_FIN);
	// fs.quit();
	// System.out.println("page: " + page);
	//
	// assertNotNull(page.getPublications());
	// assertEquals(2, page.getPublications().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("teamisurus", page.getPublications().get(0).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// assertEquals(1, pub.getComments().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// pub = fs.obtainPostInformation("teamisurus", page.getPublications().get(1).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// assertEquals(20, pub.getComments().size());
	// }
	//
	// @Test
	// public void centroKinesiologiaConFiltroComments() throws Exception {
	// // Test para controlar post SIN comentarios.
	// Long uTIME_INI = 1526428800L; // 16/05/2018 @ 00:00:00
	// Long uTIME_FIN = 1526515200L; // 16/05/2018 @ 24:00:00
	//
	// Long COMMENTS_uTIME_INI = 1526428800L; // 16/05/2018 @ 00:00:00
	// Long COMMENTS_uTIME_FIN = 1528761600L; // 11/06/2018 24:00:00
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = null;
	// page = fs.obtainPageInformationWithoutComments("CKED-Centro-kinésico-y-Entrenamiento-Deportivo-154152138076469", uTIME_INI, uTIME_FIN);
	//
	// fs.quit();
	// System.out.println("page: " + page);
	//
	// assertNotNull(page.getPublications());
	// assertEquals(1, page.getPublications().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("CKED-Centro-kinésico-y-Entrenamiento-Deportivo-154152138076469", page.getPublications().get(0).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// assertEquals(null, pub.getComments());
	//
	// }
	//
	// @Test
	// public void mauriciomacriConFiltroComments() throws Exception {
	// // Test para 1 solo comentario el 06/06.
	// Long uTIME_INI = 1528156800L; // 05/06/2018 @ 00:00:00
	// Long uTIME_FIN = 1528243200L; // 05/06/2018 @ 24:00:00
	//
	// Long COMMENTS_uTIME_INI = 1528588800L; // 10/06/2018 @ 00:00:00
	// Long COMMENTS_uTIME_FIN = 1528761600L; // 11/06/2018 24:00:00
	//
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = null;
	// page = fs.obtainPageInformationWithoutComments("mauriciomacri", uTIME_INI, uTIME_FIN);
	//
	// fs.quit();
	// System.out.println("page: " + page);
	//
	// assertNotNull(page.getPublications());
	// assertEquals(1, page.getPublications().size());
	//
	// fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("mauriciomacri", page.getPublications().get(0).getId(), COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// System.out.println("Cantidad mensajes: " + pub.getComments().size());
	// assertEquals(27, pub.getComments().size());
	//
	// }

	// @Test
	// public void larretaPost() throws Exception {
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("horaciorodriguezlarreta", "245744979463373", null, null, 100, CommentsSort.NEW);
	// fs.quit();
	//
	// System.out.println("Cantidad mensajes: " + pub.getComments().size());
	// assertTrue(pub.getComments().size() > 100);
	//
	// }

	// @Test
	// public void larretaPost() throws Exception {
	// FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	// Page page = fs.obtainPageInformationWithoutComments("horaciorodriguezlarreta", 1536156518L, 1536275772L);
	// fs.quit();
	//
	// System.out.println("Cpage: " + page);
	// assertTrue(true);
	//
	// }

	 @Test
	 public void larretaPost() throws Exception {
	 FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
	 Publication pub = fs.obtainPostInformation("horaciorodriguezlarreta", "10156879212501019", null, null, 1000, CommentsSort.NEW);
	 fs.quit();
	
	 System.out.println("Cantidad mensajes: " + pub.getComments().size());
	 assertTrue(pub.getComments().size() > 50);
	
	 }
//	@Test
//	public void larretaPost() throws Exception {
//		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
//		Publication pub = fs.obtainPostInformation("clarincom", "2136741736360921", 1536431726L, 1536436824L, 1000, CommentsSort.NEW);
//		fs.quit();
//
//		System.out.println("Cantidad mensajes: " + pub.getComments().size());
//		assertTrue(pub.getComments().size() == 0);
//
//	}
}
