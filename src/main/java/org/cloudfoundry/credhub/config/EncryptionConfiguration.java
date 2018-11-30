package org.cloudfoundry.credhub.config;

public class EncryptionConfiguration {
  private Integer port;
  private String host;
  private String partition;
  private String partitionPassword;
  private String serverCa;
  private String clientCertificate;
  private String clientKey;
  private String endpoint;

  public String getServerCa() {
    return serverCa;
  }

  public void setServerCa(String serverCa) {
    this.serverCa = serverCa;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public void setClientCertificate(String clientCertificate) {
    this.clientCertificate = clientCertificate;
  }

  public String getClientKey() {
    return clientKey;
  }

  public void setClientKey(String clientKey) {
    this.clientKey = clientKey;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPartition() {
    return partition;
  }

  public void setPartition(String partition) {
    this.partition = partition;
  }

  public String getPartitionPassword() {
    return partitionPassword;
  }

  public void setPartitionPassword(String partitionPassword) {
    this.partitionPassword = partitionPassword;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public String toString() {
    return "EncryptionConfiguration{" +
      "port=" + port +
      ", host='" + host + '\'' +
      ", partition='" + partition + '\'' +
      ", partitionPassword='" + partitionPassword + '\'' +
      ", endpoint='" + endpoint + '\'' +
      '}';
  }
}
