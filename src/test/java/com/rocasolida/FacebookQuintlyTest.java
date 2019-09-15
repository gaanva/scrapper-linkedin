package com.rocasolida;

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rocasolida.scrapperfacebook.entities.Page;
import com.rocasolida.scrapperfacebook.entities.Publication;
import com.rocasolida.scrapperfacebook.scrap.FacebookPostScrap;
import com.rocasolida.scrapperfacebook.scrap.util.CommentsSort;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;
import com.rocasolida.scrapperfacebook.scrap.util.ScrapUtils;

public class FacebookQuintlyTest {

	private static String OS = ScrapUtils.getOSName();
	private static final boolean DEBUG = true;
	private static DriverType dt = DriverType.FIREFOX_HEADLESS;

	@BeforeClass
	public static void loadOs() {
	}

	@Test
	public void alejandrocacace() throws Exception {
		FacebookPostScrap fs = new FacebookPostScrap(Driver.from(dt, OS), DEBUG);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -4);
		Long minPostUtime = cal.getTimeInMillis() / 1000;
		Long maxPostUtime = System.currentTimeMillis() / 1000;

		Page page = fs.scrapePage("alejandrocacace", minPostUtime, maxPostUtime, null, null, 200, CommentsSort.RELEVANCE, 10);
		fs.quit();

		assertNotNull(page);
		assertNotNull(page.getPublications());
		System.out.println("Cantidad de pubs: " + page.getPublications().size());
		for (Publication p : page.getPublications()) {
			System.out.println(p.getId() + " likes: " + p.getCantLikes() + ". loves: " + p.getCantLoves());
		}
	}
}
