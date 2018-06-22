package com.rocasolida;

import java.net.MalformedURLException;

import com.rocasolida.entities.Credential;
import com.rocasolida.entities.Page;
import com.rocasolida.entities.Publication;
import com.rocasolida.scrap.FacebookScrap;
import com.rocasolida.scrap.util.CommentsSort;
import com.rocasolida.scrap.util.Driver;
import com.rocasolida.scrap.util.DriverType;
import com.rocasolida.scrap.util.ScrapUtils;

/*
 * NOTA: Si estás Loggeado, no te muestra por default las historias (o tal vez mi profile tenga algo).
 * Si no estás loggeado, te carga una banda de publicaciones.
 */

public class Application {

	public static void main(String[] args) throws MalformedURLException {
		try {
			Long aux = System.currentTimeMillis();
			Boolean debug = true;
			Credential access = null;
			// Credential access = new Credential("estelaquilmes2018@gmail.com",
			// "qsocialnow2018", 0L, "");
			String os = ScrapUtils.getOSName();
			String user = System.getenv("LOGIN_FACEBOOK");
			String password = System.getenv("PASSWORD_FACEBOOK");
			String seleniumHost = System.getenv("SELENIUM_HOST");
			String seleniumPort = System.getenv("SELENIUM_PORT");

			if (user != null && password != null) {
				access = new Credential(user, password, 0L, "");
			}
			String pageName = "mauriciomacri";
			String postId = "10156654894768478";
			// 10156628922723478 mauri no anda queda spinner girando 10156654894768478
			// POST: 1528747836 (Mauri - mama luchetti) 10156656338013478
			// VIDEO: 1528835710 (Mauri)
			// Long uTIME_INI = 1528156800L; // 05/06/2018 @ 00:00:00
			// Long uTIME_FIN = 1528243200L; // 05/06/2018 @ 24:00:00
			Long uTIME_INI = 1528747836L; // 05/06/2018 @ 00:00:00
			Long uTIME_FIN = 1528747836L; // 05/06/2018 @ 24:00:00
			// Long uTIME_INI = 1521072000L; // 03/15/2018 @ 12:00am (UTC) - Desde las 0hs
			// del 15/03
			// Long uTIME_FIN = 1521158400L; // 03/15/2018 @ 12:59:59pm (UTC) - Hasta las
			// 0hs dle 16/03

			// Long COMMENTS_uTIME_INI = 1528747836L; // 10/06/2018 @ 00:00:00
			// Long COMMENTS_uTIME_FIN = 1529069440L; // 11/06/2018 24:00:00
			Long COMMENTS_uTIME_INI = null;
			Long COMMENTS_uTIME_FIN = null;
			Integer cantComments = 100;
			CommentsSort cs = CommentsSort.NEW;
			DriverType dt = DriverType.FIREFOX;
			FacebookScrap fs = null;
			if (seleniumHost != null && seleniumPort != null) {
				fs = new FacebookScrap(Driver.from(dt, os, seleniumHost, seleniumPort), debug);
			} else {
				fs = new FacebookScrap(Driver.from(dt, os), debug);
			}
			Page page = null;
			Publication publication = null;
			if (access != null) {
				System.out.println("[APP]Por hacer login");
				if (fs.login(access)) {
				} else {
					System.out.println("Error en LOGIN con el usuario: " + access.getUser() + " PASS: " + access.getPass());
				}
			}
			page = fs.obtainPageInformation(pageName, uTIME_INI, uTIME_FIN, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN);
			page = fs.obtainPageInformationWithoutComments(pageName, uTIME_INI, uTIME_FIN);
			publication = fs.obtainPostInformation(postId, COMMENTS_uTIME_INI, COMMENTS_uTIME_FIN, cantComments, cs);
			// System.out.println(page);
			System.out.println(publication);
			if (publication != null && publication.getComments() != null) {
				System.out.println("cant comments extraidos: " + publication.getComments().size());
			}
			fs.quit();
			System.out.println("[APP] FIN");
			aux = System.currentTimeMillis() - aux;
			System.out.println("Tardo: " + aux);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}