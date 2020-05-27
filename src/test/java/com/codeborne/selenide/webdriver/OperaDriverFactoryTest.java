package com.codeborne.selenide.webdriver;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.SelenideConfig;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.InvalidArgumentException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.opera.OperaOptions;

import java.util.Map;

import static org.mockito.Mockito.mock;

class OperaDriverFactoryTest implements WithAssertions {
  private final Proxy proxy = mock(Proxy.class);
  private final SelenideConfig config = new SelenideConfig().headless(false);
  private final Browser browser = new Browser(config.browser(), config.headless());

  @Test
  void browserBinaryCanBeSet() {
    config.browserBinary("c:/browser.exe");
    Capabilities caps = new OperaDriverFactory().createCapabilities(config, browser, proxy);
    Map options = (Map) caps.asMap().get(OperaOptions.CAPABILITY);
    assertThat(options.get("binary"))
      .isEqualTo("c:/browser.exe");
  }

  @Test
  void headlessCanNotBeSet() {
    config.headless(true);
    assertThatThrownBy(() -> new OperaDriverFactory().createCapabilities(config, browser, proxy))
      .isInstanceOf(InvalidArgumentException.class);
  }
}
