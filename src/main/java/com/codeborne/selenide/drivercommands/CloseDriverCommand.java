package com.codeborne.selenide.drivercommands;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.impl.Cleanup;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloseDriverCommand {
  private static final Logger log = LoggerFactory.getLogger(CloseDriverCommand.class);

  public void closeAsync(Config config, WebDriver webDriver, SelenideProxyServer selenideProxyServer) {
    long threadId = Thread.currentThread().getId();
    if (config.holdBrowserOpen()) {
      log.info("Hold browser and proxy open: {} -> {}, {}", threadId, webDriver, selenideProxyServer);
    }
    else if (webDriver != null) {
      log.info("Close webdriver: {} -> {}", threadId, webDriver);
      if (selenideProxyServer != null) {
        log.info("Close proxy server: {} -> {}", threadId, selenideProxyServer);
      }

      long start = System.currentTimeMillis();

      Thread t = new Thread(() -> close(webDriver, selenideProxyServer));
      t.setDaemon(true);
      t.start();

      try {
        t.join();
      } catch (InterruptedException e) {
        long duration = System.currentTimeMillis() - start;
        log.debug("Failed to close webdriver {} in {} ms", threadId, duration, e);
        Thread.currentThread().interrupt();
      }

      long duration = System.currentTimeMillis() - start;
      log.info("Closed webdriver {} in {} ms", threadId, duration);
    } else if (selenideProxyServer != null) {
      log.info("Close proxy server: {} -> {}", threadId, selenideProxyServer);
      selenideProxyServer.shutdown();
    }
  }

  private void close(WebDriver webdriver, SelenideProxyServer proxy) {
    try {
      log.info("Trying to close the browser {} ...", webdriver.getClass().getSimpleName());
      webdriver.quit();
    }
    catch (UnreachableBrowserException e) {
      // It happens for Firefox. It's ok: browser is already closed.
      log.debug("Browser is unreachable", e);
    }
    catch (WebDriverException cannotCloseBrowser) {
      log.error("Cannot close browser normally: {}", Cleanup.of.webdriverExceptionMessage(cannotCloseBrowser));
    }

    if (proxy != null) {
      log.info("Trying to shutdown {} ...", proxy);
      proxy.shutdown();
    }
  }
}
