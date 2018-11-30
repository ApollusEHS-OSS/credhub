package org.cloudfoundry.credhub.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.cloudfoundry.credhub.audit.CEFAuditRecord;
import org.cloudfoundry.credhub.domain.CredentialVersion;
import org.cloudfoundry.credhub.domain.JsonCredentialVersion;
import org.cloudfoundry.credhub.exceptions.EntryNotFoundException;
import org.cloudfoundry.credhub.exceptions.ParameterizedValidationException;
import org.cloudfoundry.credhub.service.PermissionedCredentialService;

@Service
public class InterpolationHandler {

  private PermissionedCredentialService credentialService;
  private CEFAuditRecord auditRecord;

  @Autowired
  public InterpolationHandler(PermissionedCredentialService credentialService, CEFAuditRecord auditRecord) {
    this.credentialService = credentialService;
    this.auditRecord = auditRecord;
  }

  public Map<String, Object> interpolateCredHubReferences(Map<String, Object> servicesMap) {
    for (Object serviceProperties : servicesMap.values()) {
      if (serviceProperties == null || !(serviceProperties instanceof ArrayList)) {
        continue;
      }
      for (Object properties : (ArrayList) serviceProperties) {
        if (!(properties instanceof Map)) {
          continue;
        }
        Map<String, Object> propertiesMap = (Map) properties;
        Object credentials = propertiesMap.get("credentials");
        if (credentials == null || !(credentials instanceof Map)) {
          continue;
        }
        // Allow either snake_case or kebab-case
        Object credhubRef = ((Map) credentials).get("credhub_ref");
        if (credhubRef == null) {
          credhubRef = ((Map) credentials).get("credhub-ref");
        }

        if (credhubRef == null || !(credhubRef instanceof String)) {
          continue;
        }
        String credentialName = getCredentialNameFromRef((String) credhubRef);

        List<CredentialVersion> credentialVersions = credentialService
          .findNByName(credentialName, 1);

        if (credentialVersions.isEmpty()) {
          throw new EntryNotFoundException("error.credential.invalid_access");
        }

        CredentialVersion credentialVersion = credentialVersions.get(0);

        auditRecord.addResource(credentialVersion.getCredential());
        auditRecord.addVersion(credentialVersion);

        if (credentialVersion instanceof JsonCredentialVersion) {
          propertiesMap.put("credentials", ((JsonCredentialVersion) credentialVersion).getValue());
        } else {
          throw new ParameterizedValidationException("error.interpolation.invalid_type",
            credentialName);
        }
      }
    }
    return servicesMap;
  }

  private String getCredentialNameFromRef(String credhubRef) {
    return credhubRef.replaceFirst("^\\(\\(", "").replaceFirst("\\)\\)$", "");
  }
}
