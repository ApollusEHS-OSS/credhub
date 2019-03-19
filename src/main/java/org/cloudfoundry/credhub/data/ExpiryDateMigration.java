package org.cloudfoundry.credhub.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cloudfoundry.credhub.entity.CertificateCredentialVersionData;
import org.cloudfoundry.credhub.entity.CredentialVersionData;
import org.cloudfoundry.credhub.exceptions.MalformedCertificateException;
import org.cloudfoundry.credhub.exceptions.MissingCertificateException;
import org.cloudfoundry.credhub.repository.CredentialVersionRepository;
import org.cloudfoundry.credhub.util.CertificateReader;

@Component
public class ExpiryDateMigration {
  private static final Logger LOGGER = LogManager.getLogger(ExpiryDateMigration.class);

  private final CredentialVersionRepository credentialVersionRepository;

  @Autowired
  public ExpiryDateMigration(final CredentialVersionRepository credentialVersionRepository) {
    super();
    this.credentialVersionRepository = credentialVersionRepository;
  }

  public void migrate() {
    final List<CredentialVersionData> data = credentialVersionRepository.findAllVersionsWithNullExpirationDate();

    for (final CredentialVersionData version : data) {
      if (version instanceof CertificateCredentialVersionData) {
        final CertificateCredentialVersionData certificateVersion = (CertificateCredentialVersionData) version;
        final String certificate = certificateVersion.getCertificate();
        try {
          final CertificateReader reader = new CertificateReader(certificate);
          ((CertificateCredentialVersionData) version).setExpiryDate(reader.getNotAfter());
        } catch (MalformedCertificateException e) {
          final String message = String.format("can't read certificate with name %s", certificateVersion.getName());
          LOGGER.warn(message);
        } catch (MissingCertificateException e) {
          final String message = String.format("missing certificate with name %s", certificateVersion.getName());
          LOGGER.warn(message);
        } catch (RuntimeException e) {
          final String message = String.format("Unexpected exception reading certificate with name %s: %s", certificateVersion.getName(), e.toString());
          LOGGER.warn(message);
        }
      }
    }
    credentialVersionRepository.saveAll(data);
  }
}
