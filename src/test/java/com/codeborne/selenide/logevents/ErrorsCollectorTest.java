package com.codeborne.selenide.logevents;

import com.codeborne.selenide.ex.SoftAssertionError;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class ErrorsCollectorTest implements WithAssertions {
  private final ErrorsCollector errorsCollector = new ErrorsCollector();
  private final LogEvent mockedInProgressEvent = mock(LogEvent.class);
  private final LogEvent mockedPassedEvent = mock(LogEvent.class);
  private final LogEvent mockedFailedEvent = mock(LogEvent.class);
  private final String defaultErrorMessage = "Couldn't find an element";
  private final String defaultTestName = "ITestName";

  @BeforeEach
  void setup() {
    when(mockedInProgressEvent.getStatus()).thenReturn(LogEvent.EventStatus.IN_PROGRESS);
    when(mockedPassedEvent.getStatus()).thenReturn(LogEvent.EventStatus.PASS);
    when(mockedFailedEvent.getStatus()).thenReturn(LogEvent.EventStatus.FAIL);
    when(mockedFailedEvent.getError()).thenReturn(new StaleElementReferenceException(defaultErrorMessage));
  }

  @Test
  void onEvent() {
    List<Throwable> errors = errorsCollector.getErrors();

    errorsCollector.afterEvent(mockedInProgressEvent);
    assertThat(errors)
      .hasSize(0);

    errorsCollector.afterEvent(mockedPassedEvent);
    assertThat(errors)
      .hasSize(0);

    errorsCollector.afterEvent(mockedFailedEvent);
    assertThat(errors)
      .hasSize(1);
    Throwable error = errors.get(0);
    assertThat(error)
      .isInstanceOf(StaleElementReferenceException.class);
    assertThat(error)
      .hasMessageContaining(defaultErrorMessage);
  }

  @Test
  void clearMethod() {
    List<Throwable> errors = errorsCollector.getErrors();

    errorsCollector.afterEvent(mockedFailedEvent);
    errorsCollector.afterEvent(mockedFailedEvent);
    errorsCollector.afterEvent(mockedFailedEvent);

    assertThat(errors)
      .hasSize(3);
    errorsCollector.clear();

    assertThat(errors)
      .hasSize(0);
  }

  @Test
  void failIfErrorMethodWhenOnlyOneError() {
    errorsCollector.afterEvent(mockedFailedEvent);
    try {
      errorsCollector.failIfErrors(defaultTestName);
      fail("Expected SoftAssertionError");
    } catch (SoftAssertionError error) {
      assertThat(error)
        .withFailMessage("I couldn't find default error message in error message")
        .hasMessageContaining(defaultErrorMessage);
    }
  }

  @Test
  void failIfErrorMethodWhenMoreThenOneError() {
    LogEvent mockedFailedEvent2 = mock(LogEvent.class);
    String failedEvent2Message = "Second failure";
    when(mockedFailedEvent2.getStatus()).thenReturn(LogEvent.EventStatus.FAIL);
    when(mockedFailedEvent2.getError()).thenReturn(new StaleElementReferenceException(failedEvent2Message));

    errorsCollector.afterEvent(mockedFailedEvent);
    errorsCollector.afterEvent(mockedFailedEvent2);
    try {
      errorsCollector.failIfErrors(defaultTestName);
      fail("Expected SoftAssertionError");
    } catch (SoftAssertionError error) {
      assertThat(error)
        .as("Error title")
        .hasMessageContaining(String.format("Test %s failed.", defaultTestName));
      assertThat(error)
        .as("Record about number of failed checks")
        .hasMessageContaining("2 checks failed");
      assertThat(error)
        .as("First event message")
        .hasMessageContaining(String.format("FAIL #1: org.openqa.selenium.StaleElementReferenceException: %s", defaultErrorMessage));
      assertThat(error)
        .as("Second event message is missing")
        .hasMessageContaining(String.format("FAIL #2: org.openqa.selenium.StaleElementReferenceException: %s", failedEvent2Message));
    }
  }
}
