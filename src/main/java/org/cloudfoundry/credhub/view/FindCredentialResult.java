package org.cloudfoundry.credhub.view;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FindCredentialResult {
  private final Instant versionCreatedAt;
  private final String name;

  public FindCredentialResult(Instant versionCreatedAt, String name) {
    this.versionCreatedAt = versionCreatedAt;
    this.name = name;
  }

  @JsonProperty
  public Instant getVersionCreatedAt() {
    return versionCreatedAt;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("name")
  public String getName() {
    return name;
  }
}
