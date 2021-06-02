package com.codeborne.selenide.proxy;

import com.codeborne.selenide.AuthenticationType;
import com.codeborne.selenide.Credentials;
import io.netty.handler.codec.http.DefaultHttpRequest;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static java.nio.charset.StandardCharsets.UTF_8;

final class AuthenticationFilterTest implements WithAssertions {
  private final AuthenticationFilter filter = new AuthenticationFilter();
  private final DefaultHttpRequest request = new DefaultHttpRequest(HTTP_1_1, GET, "/secured/page");

  @Test
  void hasNoEffectByDefault() {
    filter.filterRequest(request, null, null);

    assertThat(request.headers().entries().size()).isEqualTo(0);
  }

  @Test
  void canAddAuthentication() {
    filter.setAuthentication(AuthenticationType.BEARER, new Credentials("username", "password"));
    String expectedHeader = "Bearer " + Base64.getEncoder().encodeToString("username:password".getBytes(UTF_8));

    filter.filterRequest(request, null, null);

    assertThat(request.headers().entries().size()).isEqualTo(2);
    assertThat(request.headers().entries().get(0).getKey()).isEqualTo("Authorization");
    assertThat(request.headers().entries().get(0).getValue()).isEqualTo(expectedHeader);

    assertThat(request.headers().entries().get(1).getKey()).isEqualTo("Proxy-Authorization");
    assertThat(request.headers().entries().get(1).getValue()).isEqualTo(expectedHeader);
  }

  @Test
  void canRemoveAuthentication() {
    filter.setAuthentication(AuthenticationType.BEARER, new Credentials("username", "password"));
    filter.removeAuthentication();

    filter.filterRequest(request, null, null);

    assertThat(request.headers().entries().size()).isEqualTo(0);
  }
}
