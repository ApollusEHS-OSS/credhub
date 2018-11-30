package org.cloudfoundry.credhub.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cloudfoundry.credhub.audit.CEFAuditRecord;
import org.cloudfoundry.credhub.credential.CredentialValue;
import org.cloudfoundry.credhub.domain.CredentialVersion;
import org.cloudfoundry.credhub.request.BaseCredentialGenerateRequest;
import org.cloudfoundry.credhub.service.PermissionedCredentialService;
import org.cloudfoundry.credhub.view.CredentialView;

@Service
public class GenerateHandler {

  private final PermissionedCredentialService credentialService;
  private final UniversalCredentialGenerator credentialGenerator;
  private CEFAuditRecord auditRecord;

  @Autowired
  public GenerateHandler(
    PermissionedCredentialService credentialService,
    UniversalCredentialGenerator credentialGenerator,
    CEFAuditRecord auditRecord) {
    this.credentialService = credentialService;
    this.credentialGenerator = credentialGenerator;
    this.auditRecord = auditRecord;
  }

  public CredentialView handle(BaseCredentialGenerateRequest generateRequest) {
    CredentialVersion existingCredentialVersion = credentialService.findMostRecent(generateRequest.getName());
    CredentialValue value = credentialGenerator.generate(generateRequest);

    final CredentialVersion credentialVersion = credentialService.save(existingCredentialVersion, value, generateRequest);

    auditRecord.setVersion(credentialVersion);
    auditRecord.setResource(credentialVersion.getCredential());
    return CredentialView.fromEntity(credentialVersion);
  }
}
