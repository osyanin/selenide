package com.codeborne.selenide.drivercommands;

import com.codeborne.selenide.Config;
import com.codeborne.selenide.proxy.SelenideProxyServer;
import org.openqa.selenium.WebDriver;

public class SelenideDriverFinalCleanupThread extends Thread {
  private final Config config;
  private final WebDriver driver;
  private final SelenideProxyServer proxy;
  private final CloseDriverCommand closeDriverCommand;

  SelenideDriverFinalCleanupThread(Config config, WebDriver driver, SelenideProxyServer proxy) {
    this(config, driver, proxy, new CloseDriverCommand());
  }

  SelenideDriverFinalCleanupThread(Config config, WebDriver driver, SelenideProxyServer proxy, CloseDriverCommand closeDriverCommand) {
    this.config = config;
    this.driver = driver;
    this.proxy = proxy;
    this.closeDriverCommand = closeDriverCommand;
  }

  @Override
  public void run() {
    closeDriverCommand.closeAsync(config, driver, proxy);
  }
}
