package com.codeborne.selenide.commands;

import com.codeborne.selenide.Command;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.impl.WebElementSource;
import org.openqa.selenium.WebElement;

public class GetInnerHtml implements Command<String> {
  @Override
  public String execute(SelenideElement proxy, WebElementSource locator, Object[] args) {
    WebElement element = locator.getWebElement();
    return element.getAttribute("innerHTML");
  }
}
