package org.cloudfoundry.credhub.audit.entity;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.cloudfoundry.credhub.audit.OperationDeviceAction;

public class SetCredential implements RequestDetails {
  private String name;
  private String type;

  public SetCredential(String credentialName, String credentialType) {
    name = credentialName;
    type = credentialType;
  }

  public SetCredential() {

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SetCredential that = (SetCredential) o;

    return new EqualsBuilder()
      .append(name, that.name)
      .append(type, that.type)
      .isEquals();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type);
  }

  @Override
  public OperationDeviceAction operation() {
    return OperationDeviceAction.SET;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
