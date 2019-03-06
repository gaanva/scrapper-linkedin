package com.rocasolida.scrapperfacebook.scrap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.rocasolida.scrapperfacebook.entities.Credential;
import com.rocasolida.scrapperfacebook.scrap.util.Driver;
import com.rocasolida.scrapperfacebook.scrap.util.DriverFinder;
import com.rocasolida.scrapperfacebook.scrap.util.DriverType;

import lombok.Data;

@Data
public class Scrap {

	private static Integer SCREEN_WIDTH = 1920;
	private static Integer SCREEN_HEIGHT = 1080;

	private WebDriver driver;
	private Credential access;
	private WebDriverWait waitDriver;
	private Actions actions;
	private Driver driverResource;
	protected boolean debug;

	public Scrap(final Driver driver, boolean debug) throws MalformedURLException {
		this.driverResource = driver;
		this.debug = debug;
		// Creo el webdriver
		if (driver.getSeleniumHost() != null && driver.getSeleniumPort() != null) {
			this.setupWebDriver(driver);
		} else {
			this.initLocalDriver(driver);
		}
	}

	public void setupWebDriver(Driver driver) throws MalformedURLException {
		setupRemoteDriver(driver);

	}

	private void setupRemoteDriver(Driver driverResource) throws MalformedURLException {
		DesiredCapabilities capabilities;
		if (driverResource.getType().equals(DriverType.FIREFOX)) {
			capabilities = DesiredCapabilities.firefox();
			capabilities.setBrowserName("firefox");
		} else if (driverResource.getType().equals(DriverType.CHROME)) {
			capabilities = DesiredCapabilities.chrome();
			capabilities.setBrowserName("chrome");
		} else if (driverResource.getType().equals(DriverType.FIREFOX_HEADLESS)) {
			capabilities = DesiredCapabilities.chrome();
			capabilities.setBrowserName("firefox");
		} else if (driverResource.getType().equals(DriverType.CHROME_HEADLESS)) {
			capabilities = DesiredCapabilities.chrome();
			capabilities.setBrowserName("chrome");
		} else {
			throw new RuntimeException("Browser type unsupported");
		}

		if (driverResource.getOs().equals("Mac")) {
			capabilities.setPlatform(Platform.MAC);
		} else if (driverResource.getOs().equals("Linux")) {
			capabilities.setPlatform(Platform.LINUX);
		} else if (driverResource.getOs().equals("Windows")) {
			capabilities.setPlatform(Platform.WINDOWS);
		} else {
			throw new RuntimeException("SO type unsupported");
		}

		this.driver = new RemoteWebDriver(new URL("http://" + driverResource.getSeleniumHost() + ":" + driverResource.getSeleniumPort() + "/wd/hub"), capabilities);
		((RemoteWebDriver) this.driver).setFileDetector(new LocalFileDetector());
		this.configureDriver();
	}

	public void initLocalDriver(Driver driver) {
		if (driver != null && (driver.getType().equals(DriverType.FIREFOX_HEADLESS) || driver.getType().equals(DriverType.FIREFOX))) {
			System.setProperty("webdriver.gecko.driver", DriverFinder.findFirefoxDriver(driver.getOs()));
			System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "logs/logs.txt");

			FirefoxBinary firefoxBinary = null;

			if (System.getenv("CI") != null) {
				firefoxBinary = new FirefoxBinary(new File("firefox/firefox"));
			} else {
				firefoxBinary = new FirefoxBinary();
			}

			if (driver.getType().equals(DriverType.FIREFOX_HEADLESS)) {
				firefoxBinary.addCommandLineOptions("--headless");
			}

			FirefoxOptions firefoxOptions = new FirefoxOptions();
			firefoxOptions.setBinary(firefoxBinary);
			FirefoxProfile firefoxProfile = new FirefoxProfile();
			firefoxProfile.setPreference("media.volume_scale", "0.0");
			firefoxOptions.setProfile(firefoxProfile);
			firefoxOptions.setCapability("permissions.default.image", 2);
			this.driver = new FirefoxDriver(firefoxOptions);
			this.configureDriver();
		} else if (driver != null && (driver.getType().equals(DriverType.CHROME_HEADLESS) || driver.getType().equals(DriverType.CHROME))) {
			ChromeOptions chromeOptions = new ChromeOptions();
			System.setProperty("webdriver.chrome.driver", DriverFinder.findChromeDriver(driver.getOs()));
			if (driver.getType().equals(DriverType.CHROME_HEADLESS)) {
				chromeOptions.addArguments("--headless");
			}
			// chromeOptions.addArguments("--disable-gpu");
			this.driver = new ChromeDriver(chromeOptions);
		}
	}

	public void quit() {
		try {
			this.driver.quit();
		} catch (Exception ex) {
		}
	}

	private void configureDriver() {
		this.driver.manage().window().setSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
		this.driver.manage().window().maximize();
		this.actions = new Actions(this.driver);
	}

}
