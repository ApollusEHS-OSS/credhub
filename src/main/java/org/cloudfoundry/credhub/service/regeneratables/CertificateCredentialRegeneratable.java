package org.cloudfoundry.credhub.service.regeneratables;

import org.cloudfoundry.credhub.domain.CertificateCredentialVersion;
import org.cloudfoundry.credhub.domain.CertificateGenerationParameters;
import org.cloudfoundry.credhub.domain.CredentialVersion;
import org.cloudfoundry.credhub.exceptions.ParameterizedValidationException;
import org.cloudfoundry.credhub.request.BaseCredentialGenerateRequest;
import org.cloudfoundry.credhub.request.CertificateGenerateRequest;
import org.cloudfoundry.credhub.util.CertificateReader;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CertificateCredentialRegeneratable implements Regeneratable {

  @Override
  public BaseCredentialGenerateRequest createGenerateRequest(CredentialVersion credentialVersion) {
    CertificateCredentialVersion certificateCredential = (CertificateCredentialVersion) credentialVersion;
    CertificateReader reader = certificateCredential.getParsedCertificate();

    if ((isEmpty(certificateCredential.getCaName()) && !reader.isSelfSigned())) {
      throw new ParameterizedValidationException(
        "error.cannot_regenerate_non_generated_certificate");
    }

    CertificateGenerationParameters certificateGenerationParameters = new CertificateGenerationParameters(
      reader,
      certificateCredential.getCaName()
    );

    CertificateGenerateRequest generateRequest = new CertificateGenerateRequest();
    generateRequest.setName(certificateCredential.getName());
    generateRequest.setType(certificateCredential.getCredentialType());
    generateRequest.setCertificateGenerationParameters(certificateGenerationParameters);
    generateRequest.setOverwrite(true);
    return generateRequest;
  }
}
