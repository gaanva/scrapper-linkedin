package com.rocasolida;

import java.net.MalformedURLException;

import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Page;
import com.rocasolida.scrap.FacebookScrap;
import com.rocasolida.scrap.util.Driver;
import com.rocasolida.scrap.util.DriverType;

/*
 * NOTA: Si estás Loggeado, no te muestra por default las historias (o tal vez mi profile tenga algo).
 * Si no estás loggeado, te carga una banda de publicaciones.
 */

public class Application {

	public static void main(String[] args) throws MalformedURLException {
		Long aux = System.currentTimeMillis();
		Boolean debug = true;
		Credential access = null;
//		 Credential access = new Credential("estelaquilmes2018@gmail.com", "qsocialnow2018", 0L, "");
		String os = System.getenv("OS");
		String user = System.getenv("LOGIN_FACEBOOK");
		String password = System.getenv("PASSWORD_FACEBOOK");
		String seleniumHost = System.getenv("SELENIUM_HOST");
		String seleniumPort = System.getenv("SELENIUM_PORT");

		if (os == null || os.equalsIgnoreCase("Windows_NT")) {
			os = "Windows";
		}

		if (user != null && password != null) {
			access = new Credential(user, password, 0L, "");
		}

		Long uTIME_INI = 1527122578L; // 04/19/2018 @ 03:00:00
		Long uTIME_FIN = System.currentTimeMillis() / 1000; // 04/22/2018 @ 07:14:08
		// Long uTIME_INI = 1521072000L; // 03/15/2018 @ 12:00am (UTC) - Desde las 0hs del 15/03
		// Long uTIME_FIN = 1521158400L; // 03/15/2018 @ 12:59:59pm (UTC) - Hasta las 0hs dle 16/03

		Long COMMENTS_uTIME_INI = uTIME_INI;
		Long COMMENTS_uTIME_FIN = uTIME_FIN;
		for (int i = 0; i < 1; i++) {
			FacebookScrap fs = null;
			if (seleniumHost != null && seleniumPort != null) {
				fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, os, seleniumHost, seleniumPort), debug);
				// fs = new FacebookScrap(Driver.from(DriverType.FIREFOX, os, seleniumHost, seleniumPort));
			} else {
				fs = new FacebookScrap(Driver.from(DriverType.FIREFOX_HEADLESS, os), debug);
				// fs = new FacebookScrap(Driver.from(DriverType.FIREFOX, os));
			}

			System.out.println(i + "***********************************************************");
			Page page = null;
			if (access != null) {
				System.out.println("[APP]Por hacer login");
				if (fs.login(access)) {
					try {
						page = fs.obtainPageInformation("CKED-Centro-kinésico-y-Entrenamiento-Deportivo-154152138076469", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
						// fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN,
						// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
						// fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN,
						// COMMENTS_uTIME_INI,COMMENTS_uTIME_FIN);
						// fs.obtainPageInformation("cocacola", uTIME_INI, uTIME_FIN,
						// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
						// fs.obtainPageInformation("marcelotinelli", uTIME_INI, uTIME_FIN,
						// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
						// fs.obtainPageInformation("brunoli", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI,
						// COMMENTS_uTIME_FIN);
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					System.out.println("Error en LOGIN con el usuario: " + access.getUser() + " PASS: " + access.getPass());
				}
			} else {
				try {
					page = fs.obtainPageInformation("CKED-Centro-kinésico-y-Entrenamiento-Deportivo-154152138076469", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
					// fs.obtainPageInformation("teamisurus", uTIME_INI, uTIME_FIN,
					// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
					// fs.obtainPageInformation("HerbalifeLatino", uTIME_INI, uTIME_FIN,
					// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
					// fs.obtainPageInformation("cocacola", uTIME_INI, uTIME_FIN,
					// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
					// fs.obtainPageInformation("marcelotinelli", uTIME_INI, uTIME_FIN,
					// COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
					// fs.obtainPageInformation("brunoli", uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI,
					// COMMENTS_uTIME_FIN);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println(page);
			fs.quit();
		}
		System.out.println("[APP] FIN");
		aux = System.currentTimeMillis() - aux;
		System.out.println("Tardo: " + aux);
	}
}