package com.rocasolida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Page;
import com.rocasolida.scrap.FacebookScrap;
import com.rocasolida.scrap.util.Driver;
import com.rocasolida.scrap.util.DriverType;

public class ApplicationTest {

	private static String OS;

	@BeforeClass
	public static void loadOs() {
		OS = System.getenv("OS");
		if (OS == null || OS.equalsIgnoreCase("Windows_NT")) {
			OS = "Windows";
		}
	}

	@Test
	public void herbalifeConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00");
		Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		fs.login(access);
		Page page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		// System.out.println(publications);
		assertNotNull(page);
		assertEquals(1, page.getPublications().size());
		// Total de comentarios con el filtro aplicado:
		assertEquals(4, page.getPublications().get(0).getComments().size());
	}

	@Test
	public void herbalifeSinLoginConFiltroComments() throws MalformedURLException {
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00");
		Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		Page page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
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
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		fs.login(access);
		Page page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		// System.out.println(publications);
		assertNotNull(page);
		assertEquals(1, page.getPublications().size());
		// Total de comentarios con el filtro aplicado:
		assertEquals(9, page.getPublications().get(0).getComments().size());
	}

	@Test
	public void herbalifeSinLoginSinFiltroComments() throws MalformedURLException {
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = null;
		Long COMMENTS_uTIME_FIN = null;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		Page page = fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		// System.out.println(publications);
		assertNotNull(page);
		assertEquals(1, page.getPublications().size());
		// Total de comentarios con el filtro aplicado:
		assertEquals(9, page.getPublications().get(0).getComments().size());
	}
	// @Test
	// public void herbalifeSinLogin() {
	// Long uTIME_INI = getFecha("15/3/2018 00:00");
	// Long uTIME_FIN = getFecha("15/3/2018 23:59");
	// FacebookScrap fs = new FacebookScrap(DriverType.FIREFOX_HEADLESS);
	// List<Publication> publications = fs.obtainPublications("HerbalifeLatino",
	// uTIME_INI, uTIME_FIN);
	// fs.quit();
	// assertNotNull(publications);
	// assertEquals(1, publications.size());
	// assertEquals(10, publications.get(0).getComments().size());
	// }

	@Test
	public void teamisurusConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00");
		Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		fs.login(access);
		Page page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(1, page.getPublications().size());
		assertEquals(2, page.getPublications().get(0).getComments().size());
	}

	@Test
	public void teamisurusSinLoginConFiltroComments() throws MalformedURLException {
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = getFecha("15/3/2018 00:00");
		Long COMMENTS_uTIME_FIN = getFecha("16/3/2018 00:00");
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		Page page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(1, page.getPublications().size());
		assertEquals(2, page.getPublications().get(0).getComments().size());
		
	}

	@Test
	public void teamisurusConLoginSinFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = null;
		Long COMMENTS_uTIME_FIN = null;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		fs.login(access);
		Page page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(1, page.getPublications().size());
		assertEquals(2, page.getPublications().get(0).getComments().size());
	}

	@Test
	public void teamisurusSinLoginSinFiltroComments() throws MalformedURLException {
		Long uTIME_INI = getFecha("15/3/2018 00:00");
		Long uTIME_FIN = getFecha("16/3/2018 00:00");
		Long COMMENTS_uTIME_INI = null;
		Long COMMENTS_uTIME_FIN = null;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));

		Page page = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(1, page.getPublications().size());
		assertEquals(2, page.getPublications().get(0).getComments().size());
	}
	
	
	@Test
	public void mauriciomacriSinLoginConFiltroComments() throws MalformedURLException {
		Long uTIME_INI = 1524106800L; // 04/19/2018 @ 03:00:00
		Long uTIME_FIN = 1524381248L; // 04/22/2018 @ 07:14:08
		Long COMMENTS_uTIME_INI = 1524106800L;
		Long COMMENTS_uTIME_FIN = 1524381248L;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));

		Page page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(2, page.getPublications().size());
		assertTrue(page.getPublications().get(0).getComments().size()>184);
		assertTrue(page.getPublications().get(1).getComments().size()>500);
	}
	
	@Test
	public void mauriciomacriConLoginConFiltroComments() throws MalformedURLException {
		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		Long uTIME_INI = 1524106800L; // 04/19/2018 @ 03:00:00
		Long uTIME_FIN = 1524381248L; // 04/22/2018 @ 07:14:08
		Long COMMENTS_uTIME_INI = 1524106800L;
		Long COMMENTS_uTIME_FIN = 1524381248L;
		FacebookScrap fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS));
		fs.login(access);
		Page page = fs.obtainPageInformation("mauriciomacri", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
		fs.quit();
		assertNotNull(page.getPublications());
		assertEquals(2, page.getPublications().size());
		assertTrue(page.getPublications().get(0).getComments().size()>184);
		assertTrue(page.getPublications().get(1).getComments().size()>500);
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

}
