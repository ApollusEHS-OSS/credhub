package org.cloudfoundry.credhub.entity;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;

import org.apache.commons.lang3.StringUtils;

import static org.cloudfoundry.credhub.entity.CertificateCredentialVersionData.CREDENTIAL_DATABASE_TYPE;

@Entity
@DiscriminatorValue(CREDENTIAL_DATABASE_TYPE)
@SecondaryTable(
  name = CertificateCredentialVersionData.TABLE_NAME,
  pkJoinColumns = {@PrimaryKeyJoinColumn(name = "uuid", referencedColumnName = "uuid")}
)
public class CertificateCredentialVersionData extends CredentialVersionData<CertificateCredentialVersionData> {

  public static final String CREDENTIAL_DATABASE_TYPE = "cert";
  public static final String CREDENTIAL_TYPE = "certificate";
  static final String TABLE_NAME = "certificate_credential";

  @Column(table = CertificateCredentialVersionData.TABLE_NAME, length = 7000)
  private String ca;

  @Column(table = CertificateCredentialVersionData.TABLE_NAME, length = 7000)
  private String certificate;

  @Column(table = CertificateCredentialVersionData.TABLE_NAME)
  private String caName;

  @Column(table = CertificateCredentialVersionData.TABLE_NAME)
  private boolean transitional;

  @Column(table = CertificateCredentialVersionData.TABLE_NAME)
  private Instant expiryDate;

  public CertificateCredentialVersionData() {
  }

  public CertificateCredentialVersionData(String name) {
    super(name);
  }

  public String getName() {
    return super.getCredential().getName();
  }

  public String getCa() {
    return ca;
  }

  public CertificateCredentialVersionData setCa(String ca) {
    this.ca = ca;
    return this;
  }

  public String getCertificate() {
    return certificate;
  }

  public CertificateCredentialVersionData setCertificate(String certificate) {
    this.certificate = certificate;
    return this;
  }

  public String getCaName() {
    return caName;
  }

  public CertificateCredentialVersionData setCaName(String caName) {
    this.caName = !StringUtils.isEmpty(caName) ? StringUtils.prependIfMissing(caName, "/") : caName;
    return this;
  }

  public Instant getExpiryDate() {
    return expiryDate;
  }

  public CertificateCredentialVersionData setExpiryDate(Instant expiryDate) {
    this.expiryDate = expiryDate;
    return this;
  }

  @Override
  public String getCredentialType() {
    return CREDENTIAL_TYPE;
  }

  public boolean isTransitional() {
    return transitional;
  }

  public CertificateCredentialVersionData setTransitional(boolean transitional) {
    this.transitional = transitional;
    return this;
  }

  @Override
  public String toString() {
    return "CertificateCredentialVersionData{" +
      "ca='" + ca + '\'' +
      ", certificate='" + certificate + '\'' +
      ", caName='" + caName + '\'' +
      ", transitional=" + transitional +
      ", expiryDate=" + expiryDate +
      '}';
  }
}
