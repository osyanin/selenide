package com.codeborne.selenide.drivercommands;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.Config;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import com.codeborne.selenide.webdriver.WebDriverFactory;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;

/**
 * A `Driver` implementation which opens browser on demand (on a first call).
 * May be created with its own config, proxy and listeners.
 */
public class LazyDriver implements Driver {
  private static final Logger log = LoggerFactory.getLogger(LazyDriver.class);

  private final Config config;
  private final BrowserHealthChecker browserHealthChecker;
  private final WebDriverFactory factory;
  private final CloseDriverCommand closeDriverCommand;
  private final CreateDriverCommand createDriverCommand;
  private final Proxy userProvidedProxy;
  private final List<WebDriverEventListener> listeners = new ArrayList<>();
  private final Browser browser;

  private boolean closed;
  private WebDriver webDriver;
  private SelenideProxyServer selenideProxyServer;

  public LazyDriver(Config config, Proxy userProvidedProxy, List<WebDriverEventListener> listeners) {
    this(config, userProvidedProxy, listeners, new WebDriverFactory(), new BrowserHealthChecker(),
      new CreateDriverCommand(), new CloseDriverCommand());
  }

  LazyDriver(Config config, Proxy userProvidedProxy, List<WebDriverEventListener> listeners,
             WebDriverFactory factory, BrowserHealthChecker browserHealthChecker,
             CreateDriverCommand createDriverCommand, CloseDriverCommand closeDriverCommand) {
    this.config = config;
    this.browser = new Browser(config.browser(), config.headless());
    this.userProvidedProxy = userProvidedProxy;
    this.listeners.addAll(listeners);
    this.factory = factory;
    this.browserHealthChecker = browserHealthChecker;
    this.closeDriverCommand = closeDriverCommand;
    this.createDriverCommand = createDriverCommand;
  }

  @Override
  public Config config() {
    return config;
  }

  @Override
  public Browser browser() {
    return browser;
  }

  @Override
  public boolean hasWebDriverStarted() {
    return webDriver != null;
  }

  @Override
  public synchronized WebDriver getWebDriver() {
    if (closed) {
      throw new IllegalStateException("Webdriver has been closed. You need to call open(url) to open a browser again.");
    }
    if (webDriver == null) {
      throw new IllegalStateException("No webdriver is bound to current thread: " + currentThread().getId() +
        ". You need to call open(url) first.");
    }
    return webDriver;
  }

  @Override
  public SelenideProxyServer getProxy() {
    return selenideProxyServer;
  }

  @Override
  public synchronized WebDriver getAndCheckWebDriver() {
    if (webDriver != null && config.reopenBrowserOnFail() && !browserHealthChecker.isBrowserStillOpen(webDriver)) {
      log.info("Webdriver has been closed meanwhile. Let's re-create it.");
      close();
      createDriver();
    }
    else if (webDriver == null) {
      log.info("No webdriver is bound to current thread: {} - let's create a new webdriver", currentThread().getId());
      createDriver();
    }
    return getWebDriver();
  }

  void createDriver() {
    CreateDriverCommand.Result result = createDriverCommand.createDriver(config, factory, userProvidedProxy, listeners);
    this.webDriver = result.webDriver;
    this.selenideProxyServer = result.selenideProxyServer;
    this.closed = false;
  }

  @Override
  public void close() {
    closeDriverCommand.closeAsync(config, webDriver, selenideProxyServer);
    webDriver = null;
    selenideProxyServer = null;
    closed = true;
  }
}
