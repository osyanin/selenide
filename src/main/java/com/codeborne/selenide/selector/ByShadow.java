package com.codeborne.selenide.selector;

import com.codeborne.selenide.impl.Cleanup;
import com.codeborne.selenide.impl.FileContent;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@ParametersAreNonnullByDefault
public class ByShadow {
  private static final FileContent jsSource = new FileContent("find-in-shadow-roots.js");

  /**
   * Find target elements inside shadow-root that attached to shadow-host.
   * <br/> Supports inner shadow-hosts.
   * <p>
   * <br/> For example: shadow-host &gt; inner-shadow-host &gt; target-element
   * (each shadow-host must be specified explicitly).
   *
   * @param target           CSS expression of target element
   * @param shadowHost       CSS expression of the shadow-host with attached shadow-root
   * @param innerShadowHosts subsequent inner shadow-hosts
   * @return A By which locates elements by CSS inside shadow-root.
   */
  @CheckReturnValue
  @Nonnull
  public static By cssSelector(String target, String shadowHost, String... innerShadowHosts) {
    return new ByShadowCss(target, shadowHost, innerShadowHosts);
  }

  @ParametersAreNonnullByDefault
  public static class ByShadowCss extends By implements Serializable {
    private final List<String> shadowHostsChain;
    private final String target;

    ByShadowCss(String target, String shadowHost, String... innerShadowHosts) {
      //noinspection ConstantConditions
      if (shadowHost == null || target == null) {
        throw new IllegalArgumentException("Cannot find elements when the selector is null");
      }
      shadowHostsChain = new ArrayList<>(1 + innerShadowHosts.length);
      shadowHostsChain.add(shadowHost);
      shadowHostsChain.addAll(asList(innerShadowHosts));
      this.target = target;
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public WebElement findElement(SearchContext context) {
      List<WebElement> found = findElements(context);
      if (found.isEmpty()) {
        throw new NoSuchElementException("Cannot locate an element " + target + " in shadow roots " + describeShadowRoots());
      }
      return found.get(0);
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public List<WebElement> findElements(SearchContext context) {
      try {
        if (context instanceof JavascriptExecutor) {
          return findElementsInDocument((JavascriptExecutor) context);
        }
        else {
          return findElementsInElement(context);
        }
      }
      catch (JavascriptException e) {
        throw new NoSuchElementException(Cleanup.of.webdriverExceptionMessage(e));
      }
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> findElementsInDocument(JavascriptExecutor context) {
      return (List<WebElement>) context.executeScript(
        "return " + jsSource.content() + "(arguments[0], arguments[1])", target, shadowHostsChain
      );
    }

    @SuppressWarnings("unchecked")
    private List<WebElement> findElementsInElement(SearchContext context) {
      JavascriptExecutor js = (JavascriptExecutor) ((WrapsDriver) context).getWrappedDriver();
      return (List<WebElement>) js.executeScript(
        "return " + jsSource.content() + "(arguments[0], arguments[1], arguments[2])", target, shadowHostsChain, context
      );
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String toString() {
      return "By.cssSelector: " + describeShadowRoots() + " -> " + target;
    }

    @CheckReturnValue
    @Nonnull
    private String describeShadowRoots() {
      return shadowHostsChain.stream().collect(joining(" -> "));
    }
  }
}
