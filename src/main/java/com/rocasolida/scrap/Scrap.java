package com.rocasolida.scrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.rocasolida.entities.Credential;
import com.rocasolida.scrap.util.DriverType;

import lombok.Data;

public @Data class Scrap {
	/**
	 * An IMPLICIT wait is to tell WebDriver to poll the DOM for a certain amount of time when trying to find an element or elements if they are not immediately available.
	 */
	private static Integer IMPLICIT_WAIT = 2;
	/**
	 * An EXPLICIT wait is code you define to wait for a certain condition to occur before proceeding further in the code.
	 */
	private static Integer EXPLICIT_WAIT = 2;

	// private static String PATH_GHOST_DRIVER = "/home/brunoli/Downloads/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
	private static String PATH_GHOST_DRIVER = "./drivers/binaries/windows/phantomjs/64bit/phantomjs.exe";
	// private static String SETTINGS_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.120 Safari/537.36";
	private static String SETTINGS_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";

	private static String SETTINGS_LOAD_IMAGE = "false";
	private static String NAVIGATION_DATA_PATH = "C:\\tmp\\"; // Guarda datos de la navegación. Session, Cookies, etc.
	private static String NAVIGATION_DATA_STORAGE_QUOTA = "20000";

	private static Integer SCREEN_WIDTH = 1920;
	private static Integer SCREEN_HEIGHT = 1080;

	private WebDriver driver;
	private Credential access;
	private WebDriverWait waitDriver;
	private Actions actions;
	private DriverType driverType;

	public Scrap(DriverType driverType) {
		this.driverType = driverType;
		// Creo el webdriver
		this.initGhostDriver(driverType);
	}

	public void initGhostDriver(DriverType driverType) {
		if (driverType == null || driverType.equals(DriverType.PHANTOM_JS)) {
			this.driver = new PhantomJSDriver(this.getDriverCapabilities());
			this.configureDriver();
		} else if (driverType != null && driverType.equals(DriverType.FIREFOX_HEADLESS)) {
			FirefoxBinary firefoxBinary = new FirefoxBinary();
			firefoxBinary.addCommandLineOptions("--headless");
			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setBinary(firefoxBinary);
			this.driver = new FirefoxDriver(firefoxOptions);
			this.configureDriver();
		}
	}

	public void quit() {
		this.driver.quit();
	}

	// Libera recursos y me guarda la session.
	public void refresh() {
		Set<Cookie> session = this.driver.manage().getCookies();
		this.driver.close();
		this.driver.quit();
		this.initGhostDriver(driverType);
		for (Cookie cookie : session)
			this.driver.manage().addCookie(cookie);
	}

	private DesiredCapabilities getDriverCapabilities() {
		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		List<String> cliArgs = new ArrayList<String>();
		cliArgs.add("--local-storage-quota=" + NAVIGATION_DATA_STORAGE_QUOTA);
		cliArgs.add("--local-storage-path=" + NAVIGATION_DATA_PATH);
		cliArgs.add("--web-security=false");
		cliArgs.add("--ssl-protocol=any");
		cliArgs.add("--ignore-ssl-errors=true");
		cliArgs.add("--webdriver-loglevel=ERROR");
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgs);
		capabilities.setCapability("phantomjs.binary.path", PATH_GHOST_DRIVER);
		capabilities.setCapability("phantomjs.page.settings.userAgent", SETTINGS_USER_AGENT);
		capabilities.setCapability("phantomjs.page.settings.loadImages", SETTINGS_LOAD_IMAGE);

		return capabilities;
	}

	private void configureDriver() {
		this.driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
		this.waitDriver = new WebDriverWait(this.driver, EXPLICIT_WAIT);
		this.driver.manage().window().setSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		this.driver.manage().window().maximize();
		this.actions = new Actions(this.driver);
	}

}
