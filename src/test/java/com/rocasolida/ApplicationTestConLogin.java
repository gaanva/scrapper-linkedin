package com.rocasolida;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.scrap.FacebookUserLikesScrap;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class ApplicationTestConLogin {

	private static String OS;
	private static final boolean DEBUG = false;

	@BeforeClass
	public static void loadOs() {
		OS = ScrapUtils.getOSName();
	}

	
	@Test
	public void LikesProfile() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookUserLikesScrap fu = new FacebookUserLikesScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		try {
			fu.login(access);
			List<String> likespages = fu.ObtainProfileLikes("gitana.camino.3");
			for(int i=0; i<likespages.size(); i++) {
				System.out.println("LIKE "+i+":" + likespages.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		fu.quit();

	}
/*
	@Test
	public void herbalifeConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		try {
			fs.login(access);
			fs.ob("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
			fs.quit();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println(publications);
		assertNotNull(page);
		assertEquals(1, page.getPublications().size());
		// Total de comentarios con el filtro aplicado:
		assertEquals(4, page.getPublications().get(0).getComments().size());

	}

	@Test
	public void herbalifeConLoginSinFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = null;
		Long COMMENTS_uTIME_FIN = null;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		fs.login(access);
		Page page = null;
		try {
			page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);

		} catch (Exception e) {
			e.printStackTrace();
		}

		fs.quit();
		// System.out.println(publications);
		assertNotNull(page);
		assertEquals(1, page.getPublications().size());
		// Total de comentarios con el filtro aplicado:
		assertEquals(10, page.getPublications().get(0).getComments().size());

	}

	@Test
	public void teamisurusConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00");
		Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		fs.login(access);
		Page page = null;
		try {
			page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(1, page.getPublications().size());
		assertEquals(2, page.getPublications().get(0).getComments().size());

	}

	@Test
	public void mauriciomacriConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = 1524106800L; // 04/19/2018 @ 03:00:00
		Long uTIME_FIN = 1524381248L; // 04/22/2018 @ 07:14:08
		Long COMMENTS_uTIME_INI = 1524106800L;
		Long COMMENTS_uTIME_FIN = 1524381248L;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		fs.login(access);
		Page page = null;
		try {
			page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);

		} catch (Exception e) {
			e.printStackTrace();
		}

		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(2, page.getPublications().size());
		assertTrue(page.getPublications().get(0).getComments().size() > 184);
		assertTrue(page.getPublications().get(1).getComments().size() > 500);

	}

	private Long getFecha(String string) {
		try {
			String pattern = "dd/MM/yyyy HH:mm";
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			Date date = format.parse(string);
			return date.getTime() / 1000;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
*/
}
