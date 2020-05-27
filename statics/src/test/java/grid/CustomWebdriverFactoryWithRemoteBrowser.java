package grid;

import com.codeborne.selenide.Browser;
import com.codeborne.selenide.Config;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.webdriver.ChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Selenide.$$;

public class CustomWebdriverFactoryWithRemoteBrowser extends AbstractGridTest {
  @Test
  void customWebdriverProviderCanUseRemoteWebdriver() {
    MyFactory.port = hubPort;
    Configuration.browser = MyFactory.class.getName();
    openFile("page_with_selects_without_jquery.html");
    $$("#radioButtons input").shouldHave(size(4));
  }

  static class MyFactory extends ChromeDriverFactory {
    static int port;

    @Override
    public WebDriver create(Config config, Browser browser, Proxy proxy) {
      ChromeOptions options = new ChromeOptions();
      options.setHeadless(true);
      addSslErrorIgnoreCapabilities(options);

      RemoteWebDriver webDriver = new RemoteWebDriver(toURL("http://localhost:" + port + "/wd/hub"), options);
      webDriver.setFileDetector(new LocalFileDetector());
      return webDriver;
    }

    private static URL toURL(String url) {
      try {
        return new URL(url);
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
