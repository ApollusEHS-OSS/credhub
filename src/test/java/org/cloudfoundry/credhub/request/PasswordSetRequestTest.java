package org.cloudfoundry.credhub.request;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.cloudfoundry.credhub.helper.JsonTestHelper.deserialize;
import static org.cloudfoundry.credhub.helper.JsonTestHelper.deserializeAndValidate;
import static org.cloudfoundry.credhub.helper.JsonTestHelper.hasViolationWithMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

@RunWith(JUnit4.class)
public class PasswordSetRequestTest {
  @Test
  public void deserializesToPasswordSetRequest() {
    String json = "{"
      + "\"name\":\"some-name\","
      + "\"type\":\"password\","
      + "\"value\":\"fake-password\""
      + "}";
    PasswordSetRequest deserialize = deserialize(json, PasswordSetRequest.class);

    assertThat(deserialize, instanceOf(PasswordSetRequest.class));
  }

  @Test
  public void whenAllFieldsAreSet_shouldBeValid() {
    String json = "{"
      + "\"name\":\"some-name\","
      + "\"type\":\"password\","
      + "\"value\":\"fake-password\""
      + "}";
    Set<ConstraintViolation<PasswordSetRequest>> constraintViolations =
      deserializeAndValidate(json, PasswordSetRequest.class);

    assertThat(constraintViolations.size(), equalTo(0));
  }

  @Test
  public void whenTypeHasUnusualCasing_shouldBeValid() {
    String json = "{"
      + "\"name\":\"some-name\","
      + "\"type\":\"PasSWorD\","
      + "\"value\":\"fake-password\""
      + "}";
    Set<ConstraintViolation<PasswordSetRequest>> constraintViolations =
      deserializeAndValidate(json, PasswordSetRequest.class);

    assertThat(constraintViolations.size(), equalTo(0));
  }

  @Test
  public void whenPasswordIsNotSet_shouldBeInvalid() {
    String json = "{"
      + "\"name\":\"some-name\","
      + "\"type\":\"password\""
      + "}";
    Set<ConstraintViolation<PasswordSetRequest>> constraintViolations =
      deserializeAndValidate(json, PasswordSetRequest.class);

    assertThat(constraintViolations, contains(hasViolationWithMessage("error.missing_value")));
  }

  @Test
  public void whenPasswordIsEmpty_shouldBeInvalid() {
    String json = "{"
      + "\"name\":\"some-name\","
      + "\"type\":\"password\","
      + "\"value\":\"\""
      + "}";
    Set<ConstraintViolation<PasswordSetRequest>> constraintViolations =
      deserializeAndValidate(json, PasswordSetRequest.class);

    assertThat(constraintViolations, contains(hasViolationWithMessage("error.missing_value")));
  }
}
