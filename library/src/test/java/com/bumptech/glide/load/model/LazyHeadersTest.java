package com.bumptech.glide.load.model;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.EqualsTester;

import com.bumptech.glide.load.model.LazyHeaders.Builder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 18)
public class LazyHeadersTest {

  @Test
  public void testIncludesEagerHeaders() {
    Map<String, String> headers = new Builder()
        .addHeader("key", "value")
        .build()
        .getHeaders();
    assertThat(headers).containsEntry("key", "value");
  }

  @Test
  public void testIncludesLazyHeaders() {
    LazyHeaderFactory factory = mock(LazyHeaderFactory.class);
    when(factory.buildHeader()).thenReturn("value");
    Map<String, String> headers = new Builder()
        .addHeader("key", factory)
        .build()
        .getHeaders();

    assertThat(headers).containsEntry("key", "value");
  }

  @Test
  public void testMultipleEagerValuesAreSeparatedByCommas() {
    Map<String, String> headers = new Builder()
        .addHeader("key", "first")
        .addHeader("key", "second")
        .build()
        .getHeaders();

    assertThat(headers).containsEntry("key", "first,second");
  }

  @Test
  public void testMultipleLazyValuesAreSeparatedByCommas() {
    LazyHeaderFactory first = mock(LazyHeaderFactory.class);
    when(first.buildHeader()).thenReturn("first");
    LazyHeaderFactory second = mock(LazyHeaderFactory.class);
    when(second.buildHeader()).thenReturn("second");

    Map<String, String> headers = new Builder()
        .addHeader("key", first)
        .addHeader("key", second)
        .build()
        .getHeaders();
    assertThat(headers).containsEntry("key", "first,second");
  }

  @Test
  public void testMixedEagerAndLazyValuesAreIncluded() {
    LazyHeaderFactory factory = mock(LazyHeaderFactory.class);
    when(factory.buildHeader()).thenReturn("first");
    Map<String, String> headers = new Builder()
        .addHeader("key", factory)
        .addHeader("key", "second")
        .build()
        .getHeaders();

    assertThat(headers).containsEntry("key", "first,second");

    headers = new Builder()
        .addHeader("key", "second")
        .addHeader("key", factory)
        .build()
        .getHeaders();

    assertThat(headers).containsEntry("key", "second,first");
  }

  @Test
  public void testCanAddMultipleKeys() {
    LazyHeaderFactory factory = mock(LazyHeaderFactory.class);
    when(factory.buildHeader()).thenReturn("lazy");
    Map<String, String> headers = new Builder()
        .addHeader("first", factory)
        .addHeader("second", "eager")
        .build()
        .getHeaders();

    assertThat(headers).containsEntry("first", "lazy");
    assertThat(headers).containsEntry("second", "eager");
  }

  @Test
  public void testUpdatingBuilderAfterBuildingDoesNotModifyOriginalHeaders() {
    Builder builder = new Builder();
    builder.addHeader("key", "firstValue");
    builder.addHeader("otherKey", "otherValue");
    LazyHeaders first = builder.build();

    LazyHeaderFactory factory = mock(LazyHeaderFactory.class);
    when(factory.buildHeader()).thenReturn("otherValue");
    builder.addHeader("key", "secondValue");
    builder.setHeader("otherKey", factory);
    LazyHeaders second = builder.build();

    assertThat(first.getHeaders()).isNotEqualTo(second.getHeaders());

    assertThat(first.getHeaders()).containsEntry("key", "firstValue");
    assertThat(first.getHeaders()).containsEntry("otherKey", "otherValue");

    assertThat(second.getHeaders()).containsEntry("key", "firstValue,secondValue");
    assertThat(second.getHeaders()).containsEntry("otherKey", "otherValue");
  }

  @Test
  public void testSetHeaderReplacesExistingHeaders() {
    Builder builder = new Builder();
    builder.addHeader("key", "first")
        .addHeader("key", "second")
        .setHeader("key", "third");
    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("key", "third");
  }

  @Test
  public void testSetHeaderWithNullStringRemovesExistingHeader() {
    Builder builder = new Builder();
    builder.addHeader("key", "first")
        .addHeader("key", "second")
        .setHeader("key", (String) null);
    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).doesNotContainKey("key");
  }

  @Test
  public void testSetHeaderWithNullLazyHeaderFactoryRemovesExistingHeader() {
    Builder builder = new Builder();
    builder.addHeader("key", "first")
        .addHeader("key", "second")
        .setHeader("key", (LazyHeaderFactory) null);
    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).doesNotContainKey("key");
  }

  @Test
  public void testAddingEncodingHeaderReplacesDefaultThenAppends() {
    Builder builder = new Builder();
    builder.addHeader("Accept-Encoding", "false");

    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("Accept-Encoding", "false");

    builder.addHeader("Accept-Encoding", "true");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("Accept-Encoding", "false,true");
  }

  @Test
  public void testRemovingAndAddingEncodingHeaderReplacesDefaultThenAppends() {
    Builder builder = new Builder();
    builder.setHeader("Accept-Encoding", (String) null);
    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).doesNotContainKey("Accept-Encoding");

    builder.addHeader("Accept-Encoding", "false");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("Accept-Encoding", "false");

    builder.addHeader("Accept-Encoding", "true");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("Accept-Encoding", "false,true");
  }

  @Test
  public void testAddingUserAgentHeaderReplacesDefaultThenAppends() {
    Builder builder = new Builder();
    builder.addHeader("User-Agent", "false");

    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("User-Agent", "false");

    builder.addHeader("User-Agent", "true");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("User-Agent", "false,true");
  }

  @Test
  public void testRemovingAndAddingUserAgentHeaderReplacesDefaultThenAppends() {
    Builder builder = new Builder();
    builder.setHeader("User-Agent", (String) null);
    LazyHeaders headers = builder.build();
    assertThat(headers.getHeaders()).doesNotContainKey("User-Agent");

    builder.addHeader("User-Agent", "false");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("User-Agent", "false");

    builder.addHeader("User-Agent", "true");
    headers = builder.build();
    assertThat(headers.getHeaders()).containsEntry("User-Agent", "false,true");
  }

  @Test
  public void testEquals() {
    LazyHeaderFactory firstLazyFactory = mock(LazyHeaderFactory.class);
    LazyHeaderFactory secondLazyFactory = mock(LazyHeaderFactory.class);
    new EqualsTester()
        .addEqualityGroup(
            new Builder().build(),
            new Builder().build()
        )
        .addEqualityGroup(
            new Builder().addHeader("key", "value").build(),
            new Builder().addHeader("key", "value").build()
        )
        .addEqualityGroup(
            new Builder().addHeader("key", "value").addHeader("key", "value").build()
        )
        .addEqualityGroup(
            new Builder().addHeader("key", firstLazyFactory).build(),
            new Builder().addHeader("key", firstLazyFactory).build()
        )
        .addEqualityGroup(
            new Builder()
                .addHeader("key", firstLazyFactory)
                .addHeader("key", firstLazyFactory)
                .build()
        )
        .addEqualityGroup(
            new Builder()
                .addHeader("firstKey", "value")
                .addHeader("secondKey", firstLazyFactory)
                .build(),
            new Builder()
                .addHeader("secondKey", firstLazyFactory)
                .addHeader("firstKey", "value")
                .build()
        )
        .addEqualityGroup(
            new Builder().addHeader("key", "secondValue")
        )
        .addEqualityGroup(
            new Builder().addHeader("secondKey", "value")
        )
        .addEqualityGroup(
            new Builder().addHeader("key", secondLazyFactory)
        )
        .addEqualityGroup(
            new Builder().addHeader("secondKey", firstLazyFactory)
        )
        .addEqualityGroup(
            new Builder()
                .addHeader("firstKey", "firstValue")
                .addHeader("secondKey", "secondValue")
                .build(),
            new Builder()
                .addHeader("firstKey", "firstValue")
                .addHeader("secondKey", "secondValue")
                .build(),
            new Builder()
                .addHeader("secondKey", "secondValue")
                .addHeader("firstKey", "firstValue")
                .build()
        )
        .addEqualityGroup(
            new Builder()
                .addHeader("firstKey", firstLazyFactory)
                .addHeader("secondKey", secondLazyFactory)
                .build(),
            new Builder()
                .addHeader("firstKey", firstLazyFactory)
                .addHeader("secondKey", secondLazyFactory)
                .build(),
            new Builder()
                .addHeader("secondKey", secondLazyFactory)
                .addHeader("firstKey", firstLazyFactory)
                .build()
        )
        .testEquals();
  }
}
