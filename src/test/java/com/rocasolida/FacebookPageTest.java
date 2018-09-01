package com.rocasolida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.scrap.FacebookScrap;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookPageTest {
	private static String OS = ScrapUtils.getOSName();
	private static final boolean DEBUG = false;
	private static DriverType dt = DriverType.FIREFOX;

	@BeforeClass
	public static void loadOs() {
	}

	@Test
	public void mauriciomacriConFiltroComments1Mes() throws MalformedURLException {
		// Test para 1 solo comentario el 06/06.
		Long uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
		Long uTIME_FIN = 1528917098L; // 13/06/2018 @ 24:00:00

		Long COMMENTS_uTIME_INI = 1528657898L; // 10/06/2018 @ 00:00:00
		Long COMMENTS_uTIME_FIN = 1528917098L; // 11/06/2018 24:00:00

		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
		Page page = null;
		try {
			page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fs.quit();
		System.out.println("page: " + page);

		assertNotNull(page.getPublications());
		assertEquals(8, page.getPublications().size());
	}

//	@Test
//	public void teamisurusConFiltroComments() throws MalformedURLException {
//		// Test para 1 solo comentario el 06/06.
//		long aux = System.currentTimeMillis();
//		Long uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs;
//		Long uTIME_FIN = 1528329599L;// 06/06/2018 23:59:59hs;
//
//		Long COMMENTS_uTIME_INI = 1528243200L;// 06/06/2018 00:00:00hs
//		Long COMMENTS_uTIME_FIN = 1528761600L;// 11/06/2018 24:00:00hs
//		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
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
//		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
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
//		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
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
//		FacebookScrap fs = new FacebookScrap(Driver.from(dt, OS), DEBUG);
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
}
