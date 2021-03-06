package com.rocasolida;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.scrap.FacebookPostScrap;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookPostsTest {
	private static String OS = ScrapUtils.getOSName();
	private static final boolean DEBUG = true;
	private static DriverType dt = DriverType.FIREFOX_HEADLESS;

	@BeforeClass
	public static void loadOs() {
	}

	// @Test
	// public void larretaPost() throws Exception {
	// FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("horaciorodriguezlarreta", "10156879212501019", null, null, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// System.out.println("Cantidad mensajes: " + pub.getComments().size());
	// assertTrue(pub.getComments().size() > 50);
	//
	// }
	//
	// @Test
	// public void eldocetvPost() throws Exception {
	// FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("eldocetv", "2017359911633663", null, null, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// assertNotNull(pub);
	// assertNotNull(pub.getComments());
	// System.out.println("Cantidad mensajes: " + pub.getComments().size());
	// assertTrue(pub.getComments().size() > 50);
	//
	// }
	//
	// @Test
	// public void todonoticiasPost() throws Exception {
	// FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
	// Publication pub = fs.obtainPostInformation("todonoticias", "10157849791049863", null, null, 1000, CommentsSort.NEW);
	// fs.quit();
	//
	// assertNotNull(pub);
	// assertNotNull(pub.getComments());
	// System.out.println("Cantidad mensajes: " + pub.getComments().size());
	// assertTrue(pub.getComments().size() > 50);
	//
	// }
	
	@Test
	public void RosanaBertoneTDFPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = 1559681900L; //desde este momento hay 1 live... para probar.
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		//Long maxPostUtime = 1559681910L;
		Page page = fs.scrapePage("RosanaBertoneTDF", minPostUtime, maxPostUtime, null, null, null, null, null);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}
	

	@Test
	public void SchiaretiPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("SchiarettiOk", minPostUtime, maxPostUtime, null, null, null, null, null);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	/**
	 * canalseisposadas
	 */
	@Test
	public void canalSeisPosadasPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		// Calendar cal = Calendar.getInstance();
		// cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = 1558051200L; // 17/05
		// 28/04 00:00-->1556409600
		// 28/04 23:59-->1556495999
		// Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = 1558137599L;
		// Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("canalseisposadas", minPostUtime, maxPostUtime, null, null, null, null, null);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	@Test
	public void CentraldenoticiasmisionessPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		// Calendar cal = Calendar.getInstance();
		// cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = 1559433600L; // 17/05
		// 28/04 00:00-->1556409600
		// 28/04 23:59-->1556495999
		// Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = 1559520000L;
		// Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("Centraldenoticiasmisiones", minPostUtime, maxPostUtime, null, null, null, null, null);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	/**
	 * QUINTLY PROFILE TESTS: misionesonline
	 * 
	 * @throws Exception
	 */
	@Test
	public void misionesOnlinePage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("misionesonline", minPostUtime, maxPostUtime, null, null, null, null, null);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	// giselamarziottaperiodista
	/**
	 * QUINTLY PROFILE TESTS: giselamarziottaperiodista
	 * 
	 * @throws Exception
	 */
	@Test
	public void giselaMarziottaPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("giselamarziottaperiodista", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	/**
	 * QUINTLY PROFILE TESTS: vsm2.0 (victor santamaria)
	 * 
	 * @throws Exception
	 */
	@Test
	public void victorSantaMariaPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		// 04/28/2019 00:00:00: 1556409600
		// 04/28/2019 23:59:59: 1556495999
		// post to test: https://www.facebook.com/permalink.php?story_fbid=10156265872779142&id=45177919141

		// Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long minPostUtime = 1556409600L;
		// Long maxPostUtime = System.currentTimeMillis() / 1000;
		Long maxPostUtime = 1556495999L;
		Page page = fs.scrapePage("vsm2.0", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	@Test
	public void mauriciomacriPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("mauriciomacri", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	@Test
	public void mauriciomacriPage_allComments() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("mauriciomacri", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());

	}

	@Test
	public void todonoticiasPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("todonoticias", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		assertTrue(page.getPublications().size() > 0);
	}

	@Test
	public void todonoticiasPage_allComments() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("todonoticias", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		assertTrue(page.getPublications().size() > 0);
	}

	@Test
	public void dembattlesclPage() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Long maxCommentUtime = System.currentTimeMillis() / 1000;
		Long minCommentUtime = maxCommentUtime - 60 * 60 * 8;

		Page page = fs.scrapePage("dembattlescl", minPostUtime, maxPostUtime, minCommentUtime, maxCommentUtime, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

	@Test
	public void dembattlesclPage_allComments() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Long maxCommentUtime = System.currentTimeMillis() / 1000;
		Long minCommentUtime = maxCommentUtime - 60 * 60 * 8;

		Page page = fs.scrapePage("dembattlescl", minPostUtime, maxPostUtime, minCommentUtime, maxCommentUtime, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
	}

}
