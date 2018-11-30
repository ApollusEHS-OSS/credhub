package org.cloudfoundry.credhub.domain;

import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.credhub.credential.CertificateCredentialValue;
import org.cloudfoundry.credhub.entity.CertificateCredentialVersionData;
import org.cloudfoundry.credhub.request.GenerationParameters;
import org.cloudfoundry.credhub.util.CertificateReader;

public class CertificateCredentialVersion extends CredentialVersion<CertificateCredentialVersion> {

  private CertificateCredentialVersionData delegate;
  private CertificateReader parsedCertificate;

  public CertificateCredentialVersion(CertificateCredentialVersionData delegate) {
    super(delegate);
    this.delegate = delegate;
    this.setCertificate(delegate.getCertificate());
  }

  public CertificateCredentialVersion(String name) {
    this(new CertificateCredentialVersionData(name));
  }

  public CertificateCredentialVersion() {
    this(new CertificateCredentialVersionData());
  }

  public CertificateCredentialVersion(CertificateCredentialValue certificate, Encryptor encryptor) {
    this();
    this.setEncryptor(encryptor);
    this.setCa(certificate.getCa());
    this.setPrivateKey(certificate.getPrivateKey());
    this.setCaName(certificate.getCaName());
    this.setCertificate(certificate.getCertificate());
    this.setTransitional(certificate.isTransitional());
    this.setExpiryDate(certificate.getExpiryDate());
  }

  public CertificateReader getParsedCertificate() {
    return this.parsedCertificate;
  }

  public String getCa() {
    return delegate.getCa();
  }

  public CertificateCredentialVersion setCa(String ca) {
    delegate.setCa(ca);
    return this;
  }

  public String getCertificate() {
    return delegate.getCertificate();
  }

  public CertificateCredentialVersion setCertificate(String certificate) {
    delegate.setCertificate(certificate);
    if (StringUtils.isNotEmpty(delegate.getCertificate())) {
      parsedCertificate = new CertificateReader(certificate);
    }
    return this;
  }

  public String getPrivateKey() {
    return (String) super.getValue();
  }

  public CertificateCredentialVersion setPrivateKey(String privateKey) {
    if (privateKey != null) {
      super.setValue(privateKey);
    }
    return this;
  }

  public String getCaName() {
    return delegate.getCaName();
  }

  public CertificateCredentialVersion setCaName(String caName) {
    delegate.setCaName(caName);
    return this;
  }

  @Override
  public String getCredentialType() {
    return delegate.getCredentialType();
  }

  public void rotate() {
    String decryptedPrivateKey = this.getPrivateKey();
    this.setPrivateKey(decryptedPrivateKey);
  }

  public boolean matchesGenerationParameters(GenerationParameters generationParameters) {
    if (generationParameters == null) {
      return true;
    }

    final CertificateGenerationParameters parameters = (CertificateGenerationParameters) generationParameters;
    final CertificateGenerationParameters existingGenerationParameters = new CertificateGenerationParameters(getParsedCertificate(), getCaName());
    return existingGenerationParameters.equals(parameters);
  }

  private CertificateCredentialVersion setTransitional(boolean transitional) {
    delegate.setTransitional(transitional);
    return this;
  }

  public Instant getExpiryDate() {
    return delegate.getExpiryDate();
  }

  public CertificateCredentialVersion setExpiryDate(Instant expiryDate) {
    delegate.setExpiryDate(expiryDate);
    return this;
  }

  public boolean isVersionTransitional() {
    return delegate.isTransitional();
  }
}
