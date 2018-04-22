package com.rocasolida.scrap.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

public class DriverUtils {

	private static Path tempPath;

	private static WebDriver driver;

	static {
		try {
			tempPath = Files.createTempDirectory("");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static WebDriver getFFDriver(boolean headless) {
		if (driver != null) {
			return driver;
		}

		FirefoxBinary firefoxBinary = new FirefoxBinary();
		if (headless) {
			firefoxBinary.addCommandLineOptions("--headless");
		}

		FirefoxOptions firefoxOptions = new FirefoxOptions();
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("browser.download.dir", getTempPath().toString());
		profile.setPreference("browser.download.folderList", 2);
		// Set Preference to not show file download confirmation dialogue using MIME
		// types Of different file extension types.
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf;");

		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("pdfjs.disabled", true);

		firefoxOptions.setProfile(profile);

		firefoxOptions.setBinary(firefoxBinary);

		WebDriver nuevoDriver = new FirefoxDriver(firefoxOptions);

		driver = nuevoDriver;

		return driver;
	}

//	public static WebDri FI

	public static Path getTempPath() {
		return tempPath;
	}

	public static void takeSnapShot(WebDriver webdriver, String fileWithPath) throws Exception {
		// Convert web driver object to TakeScreenshot
		TakesScreenshot scrShot = ((TakesScreenshot) webdriver);

		// Call getScreenshotAs method to create image file
		File SrcFile = scrShot.getScreenshotAs(OutputType.FILE);

		// Move image file to new destination
		File DestFile = new File(fileWithPath + ".png");

		// Copy file at destination
		FileUtils.copyFile(SrcFile, DestFile);

		// Write page source
		File destHTML = new File(fileWithPath + ".txt");
		FileUtils.writeStringToFile(destHTML, webdriver.getPageSource(), Charset.forName("UTF-8"));
	}

}
