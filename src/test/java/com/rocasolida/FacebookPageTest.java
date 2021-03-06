package com.rocasolida;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.scrap.FacebookScrap2;
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
	public void mauriciomacriPage() throws Exception {
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("mauriciomacri", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		for (int i = 0; i < page.getPublications().size(); i++) {
			assertTrue(page.getPublications().get(i).getComments().size() > 0);
		}
	}

	@Test
	public void mauriciomacriPage_allComments() throws Exception {
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Page page = fs.scrapePage("mauriciomacri", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		for (int i = 0; i < page.getPublications().size(); i++) {
			if (page.getPublications().get(i).getComments() != null) {
				assertTrue(page.getPublications().get(i).getComments().size() > 0);
			} else {
				System.out.println("La publicación no tiene comentarios. URL: " + page.getPublications().get(i).getUrl());
			}
		}
	}

	@Test
	public void todonoticiasPage() throws Exception {
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
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
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
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
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
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
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
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
	public void RosanaBertoneTDFPage_allComments() throws Exception {
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Long maxCommentUtime = null;
		Long minCommentUtime = null;

		Page page = fs.scrapePage("RosanaBertoneTDF", minPostUtime, maxPostUtime, minCommentUtime, maxCommentUtime, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		for (Publication p : page.getPublications()) {
			System.out.println("coments: " + (p.getComments() == null ? null : p.getComments().size()));
		}
	}

	@Test
	public void alferdezok_allComments() throws Exception {
		FacebookScrap2 fs = new FacebookScrap2(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;
		Long maxCommentUtime = null;
		Long minCommentUtime = null;

		Page page = fs.scrapePage("alferdezok", minPostUtime, maxPostUtime, minCommentUtime, maxCommentUtime, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		for (Publication p : page.getPublications()) {
			System.out.println("coments: " + (p.getComments() == null ? null : p.getComments().size()));
		}
	}
}
