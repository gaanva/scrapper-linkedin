package com.rocasolida;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Group;
import com.rocasolida.scrapperfacebook.scrap.FacebookGroupScrap2;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
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

	@Test
	public void MunicipioNecocheaQuejas() throws Exception {
		System.out.println("#################> GRUPO MUNICIPIO NECOCHEA QUEJAS");
		//FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		FacebookGroupScrap2 fg = new FacebookGroupScrap2(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		// Saco las publicaciones de 1 día atras...
		Calendar cal = Calendar.getInstance();	
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;//1560460368
		//Long minPostUtime = 1560124800L;//1560460368L;//1560460368
		Long maxPostUtime = System.currentTimeMillis() / 1000;//1560546768
		//Long maxPostUtime = 1560211199L;//1560546768L;
		int cantComments = 200;
		
		
		try {
			// Paso 1) Extraigo las publicaciones[ID,URL y UTIME] de la página principal, en base a una cantidad.
			// ACtualizar LIKES y ver por qué repite un mensaje 4 veceS?
			Group group = fg.scrapeGroup("1752433618412532", minPostUtime, maxPostUtime, null, null, cantComments, CommentsSort.RELEVANCE, null);
			fg.quit();
			System.out.println("group: " + group.toString());
			
			//resultPage = fg.obtainGroupPubsWithoutComments("1752433618412532", 0, minPostUtime, maxPostUtime);
			
			//si es NULL es porque arrojó error el código...
			assertNotNull(group);
			assertNotNull(group.getPublications());
			//assertTrue("17 publicaciones o mas",group.getPublications().size()>=17);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void Elecciones2019SinFraude() throws Exception {
		System.out.println("#################> GRUPO ELECCIONES 2019 SIN FRAUDE");
		//FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		FacebookGroupScrap2 fg = new FacebookGroupScrap2(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		// Saco las publicaciones de 1 día atras...
		Calendar cal = Calendar.getInstance();	
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;//1560460368
		//Long minPostUtime = 1560124800L;//1560460368L;//1560460368
		Long maxPostUtime = System.currentTimeMillis() / 1000;//1560546768
		//Long maxPostUtime = 1560211199L;//1560546768L;
		int cantComments = 200;
		
		
		try {
			// Paso 1) Extraigo las publicaciones[ID,URL y UTIME] de la página principal, en base a una cantidad.
			// ACtualizar LIKES y ver por qué repite un mensaje 4 veceS?
			Group group = fg.scrapeGroup("1172891786215164", minPostUtime, maxPostUtime, null, null, cantComments, CommentsSort.RELEVANCE, null);
			fg.quit();
			System.out.println("group: " + group.toString());
			
			//resultPage = fg.obtainGroupPubsWithoutComments("1752433618412532", 0, minPostUtime, maxPostUtime);
			assertNotNull(group);
			assertNotNull(group.getPublications());
			//assertTrue("4 publicaciones o mas",page.getPublications().size()>=4);
			//assertTrue("18 comentarios o mas de las publicacion 7", page.getPublications().get(0).getComments().size()>=17);
			

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	

	@Test
	public void FernandezFernandez() throws Exception {
		System.out.println("#################> GRUPO FERNANDEZ FERNANDEZ");
		//FacebookGroupScrap fg = new FacebookGroupScrap(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		FacebookGroupScrap2 fg = new FacebookGroupScrap2(Driver.from(DriverType.FIREFOX_HEADLESS, OS), DEBUG);
		
		// Saco las publicaciones de 1 día atras...
		Calendar cal = Calendar.getInstance();	
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;//1560460368
		//Long minPostUtime = 1560124800L;//1560460368L;//1560460368
		Long maxPostUtime = System.currentTimeMillis() / 1000;//1560546768
		//Long maxPostUtime = 1560211199L;//1560546768L;
		int cantComments = 200;
		
		
		try {
			// Paso 1) Extraigo las publicaciones[ID,URL y UTIME] de la página principal, en base a una cantidad.
			// ACtualizar LIKES y ver por qué repite un mensaje 4 veceS?
			Group group = fg.scrapeGroup("336716130365314", minPostUtime, maxPostUtime, null, null, cantComments, CommentsSort.RELEVANCE, null);
			
			System.out.println("group: " + group.toString());
			fg.quit();
			//resultPage = fg.obtainGroupPubsWithoutComments("1752433618412532", 0, minPostUtime, maxPostUtime);
			assertNotNull(group);
			assertNotNull(group.getPublications());
			//assertTrue("5 publicaciones o mas",page.getPublications().size()>=5);
			//assertTrue("18 comentarios o mas de las publicacion 7", page.getPublications().get(0).getComments().size()>=17);
			

		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
}
