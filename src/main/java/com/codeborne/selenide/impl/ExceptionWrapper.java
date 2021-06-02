package com.codeborne.selenide.impl;

import com.codeborne.selenide.ex.ElementIsNotClickableException;
import com.codeborne.selenide.ex.InvalidStateException;
import com.codeborne.selenide.ex.UIAssertionError;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.exist;

@ParametersAreNonnullByDefault
class ExceptionWrapper {
  @CheckReturnValue
  @Nonnull
  Throwable wrap(Throwable lastError, WebElementSource webElementSource) {
    if (lastError instanceof UIAssertionError) {
      return lastError;
    }
    else if (lastError instanceof InvalidElementStateException) {
      return new InvalidStateException(webElementSource.driver(), lastError);
    }
    else if (isElementNotClickableException(lastError)) {
      return new ElementIsNotClickableException(webElementSource.driver(), lastError);
    }
    else if (lastError instanceof StaleElementReferenceException || lastError instanceof NotFoundException) {
      return webElementSource.createElementNotFoundError(exist, lastError);
    }
    return lastError;
  }

  @CheckReturnValue
  private boolean isElementNotClickableException(Throwable e) {
    return e instanceof WebDriverException && e.getMessage().contains("is not clickable");
  }
}
