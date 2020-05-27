package com.codeborne.selenide.webdriver;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.Config;
import com.codeborne.selenide.SelenideDriver;
import org.openqa.selenium.BuildInfo;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Browsers.CHROME;
import static com.codeborne.selenide.Browsers.EDGE;
import static com.codeborne.selenide.Browsers.FIREFOX;
import static com.codeborne.selenide.Browsers.IE;
import static com.codeborne.selenide.Browsers.INTERNET_EXPLORER;
import static com.codeborne.selenide.Browsers.LEGACY_FIREFOX;
import static com.codeborne.selenide.Browsers.OPERA;

public class WebDriverFactory {
  private static final Logger log = LoggerFactory.getLogger(WebDriverFactory.class);

  protected Map<String, Class<? extends AbstractDriverFactory>> factories = factories();
  protected RemoteDriverFactory remoteDriverFactory = new RemoteDriverFactory();
  protected BrowserResizer browserResizer = new BrowserResizer();

  private Map<String, Class<? extends AbstractDriverFactory>> factories() {
    Map<String, Class<? extends AbstractDriverFactory>> result = new HashMap<>();
    result.put(CHROME, ChromeDriverFactory.class);
    result.put(LEGACY_FIREFOX, LegacyFirefoxDriverFactory.class);
    result.put(FIREFOX, FirefoxDriverFactory.class);
    result.put(EDGE, EdgeDriverFactory.class);
    result.put(INTERNET_EXPLORER, InternetExplorerDriverFactory.class);
    result.put(IE, InternetExplorerDriverFactory.class);
    result.put(OPERA, OperaDriverFactory.class);
    return result;
  }

  public WebDriver createWebDriver(Config config, Proxy proxy) {
    log.debug("browser={}", config.browser());
    log.debug("browser.version={}", config.browserVersion());
    log.debug("remote={}", config.remote());
    log.debug("browserSize={}", config.browserSize());
    log.debug("startMaximized={}", config.startMaximized());

    Browser browser = new Browser(config.browser(), config.headless());
    WebDriver webdriver = createWebDriverInstance(config, proxy, browser);

    browserResizer.adjustBrowserSize(config, webdriver);
    browserResizer.adjustBrowserPosition(config, webdriver);

    logBrowserVersion(webdriver);
    log.info("Selenide v. {}", SelenideDriver.class.getPackage().getImplementationVersion());
    logSeleniumInfo(config);
    return webdriver;
  }

  private WebDriver createWebDriverInstance(Config config, Proxy proxy, Browser browser) {
    DriverFactory webdriverFactory = findFactory(browser);

    if (config.remote() != null) {
      MutableCapabilities capabilities = webdriverFactory.createCapabilities(config, browser, proxy);
      return remoteDriverFactory.create(config, browser, capabilities);
    }
    else {
      if (config.driverManagerEnabled()) {
        webdriverFactory.setupWebdriverBinary();
      }
      return webdriverFactory.create(config, browser, proxy);
    }
  }

  private DriverFactory findFactory(Browser browser) {
    Class<? extends AbstractDriverFactory> factoryClass = factories.getOrDefault(
      browser.name.toLowerCase(), DefaultDriverFactory.class);
    try {
      return factoryClass.getConstructor().newInstance();
    }
    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      throw new RuntimeException("Failed to initialize " + factoryClass.getName(), e);
    }
  }

  protected void logSeleniumInfo(Config config) {
    if (config.remote() == null) {
      BuildInfo seleniumInfo = new BuildInfo();
      log.info("Selenium WebDriver v. {} build time: {}", seleniumInfo.getReleaseLabel(), seleniumInfo.getBuildTime());
    }
  }

  protected void logBrowserVersion(WebDriver webdriver) {
    if (webdriver instanceof HasCapabilities) {
      Capabilities capabilities = ((HasCapabilities) webdriver).getCapabilities();
      log.info("BrowserName={} Version={} Platform={}",
        capabilities.getBrowserName(), capabilities.getVersion(), capabilities.getPlatform());
    } else {
      log.info("BrowserName={}", webdriver.getClass().getName());
    }
  }
}
