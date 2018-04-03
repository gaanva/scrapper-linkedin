package com.rocasolida;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Publication;
import com.rocasolida.scrap.FacebookScrap;
import com.rocasolida.scrap.util.DriverType;

public class ApplicationTest {

//	@Test
//	public void herbalifeConLogin() {
//		Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
//		Long uTIME_INI = getFecha("15/3/2018 00:00");
//		Long uTIME_FIN = getFecha("15/3/2018 23:59");
//		FacebookScrap fs = new FacebookScrap(DriverType.FIREFOX_HEADLESS);
//		fs.login(access);
//		List<Publication> publications = fs.obtainPublications("HerbalifeLatino", uTIME_INI, uTIME_FIN);
//		fs.quit();
//		System.out.println(publications);
//		assertNotNull(publications);
//		assertEquals(1, publications.size());
//		assertEquals(10, publications.get(0).getComments().size());
//	}
//
//	@Test
//	public void herbalifeSinLogin() {
//		Long uTIME_INI = getFecha("15/3/2018 00:00");
//		Long uTIME_FIN = getFecha("15/3/2018 23:59");
//		FacebookScrap fs = new FacebookScrap(DriverType.FIREFOX_HEADLESS);
//		List<Publication> publications = fs.obtainPublications("HerbalifeLatino", uTIME_INI, uTIME_FIN);
//		fs.quit();
//		assertNotNull(publications);
//		assertEquals(1, publications.size());
//		assertEquals(10, publications.get(0).getComments().size());
//	}
	
	@Test
	public void teamisurusSinLogin() {
		Long uTIME_INI = getFecha("24/3/2018 00:00");
		Long uTIME_FIN = getFecha("24/3/2018 23:59");
		FacebookScrap fs = new FacebookScrap(DriverType.FIREFOX_HEADLESS);
		List<Publication> publications = fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN).getPublications();
		System.out.println(publications);
		fs.quit();
		assertNotNull(publications);
		assertEquals(4, publications.size());
		assertEquals(1, publications.get(0).getComments().size());
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
