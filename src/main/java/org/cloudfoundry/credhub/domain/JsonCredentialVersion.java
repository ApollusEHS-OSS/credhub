package org.cloudfoundry.credhub.domain;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.cloudfoundry.credhub.credential.JsonCredentialValue;
import org.cloudfoundry.credhub.entity.JsonCredentialVersionData;
import org.cloudfoundry.credhub.exceptions.ParameterizedValidationException;
import org.cloudfoundry.credhub.request.GenerationParameters;
import org.cloudfoundry.credhub.util.JsonObjectMapper;

public class JsonCredentialVersion extends CredentialVersion<JsonCredentialVersion> {

  private final JsonObjectMapper objectMapper;
  private final JsonCredentialVersionData delegate;

  public JsonCredentialVersion() {
    this(new JsonCredentialVersionData());
  }

  public JsonCredentialVersion(JsonCredentialVersionData delegate) {
    super(delegate);
    this.delegate = delegate;
    /*
    It is alright to use a "new" ObjectMapper here because the JSON data does
    not have properties that is maps to, thereby not facing a problem with the
    casing being defined. Using JsonObjectMapper for consistency across project.
     */
    this.objectMapper = new JsonObjectMapper();
  }

  public JsonCredentialVersion(String name) {
    this(new JsonCredentialVersionData(name));
  }

  public JsonCredentialVersion(JsonCredentialValue jsonValue, Encryptor encryptor) {
    this();
    this.setEncryptor(encryptor);
    this.setValue(jsonValue.getValue());
  }

  @Override
  public String getCredentialType() {
    return delegate.getCredentialType();
  }

  @Override
  public void rotate() {
    JsonNode value = this.getValue();
    this.setValue(value);
  }

  public JsonNode getValue() {
    String serializedValue = (String) super.getValue();
    try {
      return objectMapper.readTree(serializedValue);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public JsonCredentialVersion setValue(JsonNode value) {
    if (value == null) {
      throw new ParameterizedValidationException("error.missing_value");
    }

    try {
      String serializedString = objectMapper.writeValueAsString(value);

      return super.setValue(serializedString);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean matchesGenerationParameters(GenerationParameters generationParameters) {
    if (generationParameters == null) {
      return true;
    }
    return false;
  }
}
