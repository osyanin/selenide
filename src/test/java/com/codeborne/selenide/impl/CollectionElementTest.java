package com.codeborne.selenide.impl;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.DriverStub;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CollectionElementTest implements WithAssertions {
  private Driver driver = new DriverStub();

  @Test
  void wrap() {
    WebElement mockedWebElement = mock(WebElement.class);
    when(mockedWebElement.getTagName()).thenReturn("a");
    when(mockedWebElement.isDisplayed()).thenReturn(true);
    when(mockedWebElement.getText()).thenReturn("selenide");

    WebElementsCollection collection = new WebElementsCollectionWrapper(driver, singletonList(mockedWebElement));
    SelenideElement selenideElement = CollectionElement.wrap(collection, 0);
    assertThat(selenideElement)
      .hasToString("<a>selenide</a>");
  }

  @Test
  void getWebElement() {
    WebElementsCollection collection = mock(WebElementsCollection.class);
    WebElement mockedWebElement1 = mock(WebElement.class);
    WebElement mockedWebElement2 = mock(WebElement.class);
    List<WebElement> listOfMockedElements = asList(mockedWebElement1, mockedWebElement2);
    when(collection.getElements()).thenReturn(listOfMockedElements);
    CollectionElement collectionElement = new CollectionElement(collection, 1);

    assertThat(collectionElement.getWebElement())
      .isEqualTo(mockedWebElement2);
  }

  @Test
  void getSearchCriteria() {
    String collectionDescription = "Collection description";
    int index = 1;
    WebElementsCollection collection = mock(WebElementsCollection.class);
    when(collection.description()).thenReturn(collectionDescription);
    CollectionElement collectionElement = new CollectionElement(collection, 1);
    assertThat(collectionElement.getSearchCriteria())
      .isEqualTo(String.format("%s[%s]", collectionDescription, index));
  }

  @Test
  void testToString() {
    WebElementsCollection collection = mock(WebElementsCollection.class);
    String collectionDescription = "Collection description";
    when(collection.description()).thenReturn(collectionDescription);
    int index = 1;
    CollectionElement collectionElement = new CollectionElement(collection, 1);
    assertThat(collectionElement)
      .hasToString(String.format("%s[%s]", collectionDescription, index));
  }

  @Test
  void createElementNotFoundErrorWithEmptyCollection() {
    WebElementsCollection collection = mock(WebElementsCollection.class);
    when(collection.driver()).thenReturn(driver);
    when(collection.description()).thenReturn("Collection description");
    CollectionElement collectionElement = new CollectionElement(collection, 1);

    Condition mockedCollection = mock(Condition.class);
    ElementNotFound elementNotFoundError = collectionElement.createElementNotFoundError(mockedCollection, new Error("Error message"));

    assertThat(elementNotFoundError)
      .hasMessage(String.format("Element not found {Collection description}%n" +
        "Expected: visible%n" +
        "Screenshot: null%n" +
        "Timeout: 0 ms.%n" +
        "Caused by: java.lang.Error: Error message"));
  }

  @Test
  void createElementNotFoundErrorWithNonEmptyCollection() {
    WebElementsCollection collection = mock(WebElementsCollection.class);
    when(collection.driver()).thenReturn(driver);
    when(collection.description()).thenReturn("Collection description");
    when(collection.getElements()).thenReturn(singletonList(mock(WebElement.class)));
    CollectionElement collectionElement = new CollectionElement(collection, 1);

    Condition mockedCollection = mock(Condition.class);
    when(mockedCollection.toString()).thenReturn("Reason description");
    ElementNotFound elementNotFoundError = collectionElement.createElementNotFoundError(mockedCollection, new Error("Error message"));

    assertThat(elementNotFoundError)
      .hasMessage(String.format("Element not found {Collection description[1]}%n" +
        "Expected: Reason description%n" +
        "Screenshot: null%n" +
        "Timeout: 0 ms.%n" +
        "Caused by: java.lang.Error: Error message"));
  }
}
