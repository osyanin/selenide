package com.codeborne.selenide.impl;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverProvider;
import com.codeborne.selenide.WebDriverRunner;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static org.mockito.Mockito.mock;

class WebDriverThreadLocalContainerTest implements WithAssertions {
  private final WebDriverThreadLocalContainer container = new WebDriverThreadLocalContainer();

  @BeforeEach
  void mockWebDriver() {
    WebDriverRunner.setProxy(null);
    Configuration.holdBrowserOpen = false;
    Configuration.reopenBrowserOnFail = true;
    Configuration.browserSize = null;
    Configuration.startMaximized = false;
    Configuration.browser = DummyProvider.class.getName();
  }

  @AfterEach
  void resetSetting() {
    Configuration.holdBrowserOpen = false;
    Configuration.reopenBrowserOnFail = true;
    Configuration.browser = "firefox";
  }

  @AfterEach
  void tearDown() {
    WebDriverRunner.setProxy(null);
    closeWebDriver();
  }

  @Test
  void shouldNotOpenANewBrowser_ifSettingIsDisabled() {
    Configuration.reopenBrowserOnFail = false;

    try {
      container.getAndCheckWebDriver();
      fail("expected IllegalStateException");
    }
    catch (IllegalStateException expected) {
      assertThat(expected)
        .hasMessageContaining("reopenBrowserOnFail=false");
    }
  }

  @Test
  void hasWebDriverStarted_false_ifNoDriverBoundToCurrentThread() {
    assertThat(container.hasWebDriverStarted()).isFalse();
  }

  @Test
  void hasWebDriverStarted_true_if_browserIsOpened() {
    assertThat(container.getAndCheckWebDriver()).isNotNull();
    assertThat(container.hasWebDriverStarted()).isTrue();
  }

  @Test
  void close_doesNothing_ifBrowserIsNotOpened() {
    container.closeWebDriver();
    assertThat(container.hasWebDriverStarted()).isFalse();
  }

  @Test
  void close_unbinds_webdriver_from_current_thread() {
    assertThat(container.getAndCheckWebDriver()).isNotNull();
    assertThat(container.hasWebDriverStarted()).isTrue();

    container.closeWebDriver();

    assertThat(container.hasWebDriverStarted()).isFalse();
  }

  @Test
  void holdsAllBrowsers_toAutomaticallyCloseThem() {
    WebDriver webDriver = container.getAndCheckWebDriver();

    assertThat(webDriver).isNotNull();
    assertThat(container.allWebDriverThreads).hasSize(1);
    assertThat(container.threadWebDriver).hasSize(1);
    assertThat(container.threadWebDriver.get(container.allWebDriverThreads.iterator().next().getId())).isSameAs(webDriver);
    assertThat(container.cleanupThreadStarted.get()).isTrue();
  }

  @Test
  void doesNotCloseBrowsers_ifHoldBrowserOpenSettingIsTrue() {
    Configuration.holdBrowserOpen = true;

    WebDriver webDriver = container.getAndCheckWebDriver();

    assertThat(webDriver).isNotNull();
    assertThat(container.allWebDriverThreads).hasSize(0);
    assertThat(container.cleanupThreadStarted.get()).isFalse();
  }

  private static class DummyProvider implements WebDriverProvider {
    @Override
    public WebDriver createDriver(DesiredCapabilities desiredCapabilities) {
      return mock(WebDriver.class);
    }
  }
}
