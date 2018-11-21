package org.cloudfoundry.credhub.service;

import org.cloudfoundry.credhub.audit.CEFAuditRecord;
import org.cloudfoundry.credhub.audit.entity.GetCredentialById;
import org.cloudfoundry.credhub.auth.UserContextHolder;
import org.cloudfoundry.credhub.constants.CredentialType;
import org.cloudfoundry.credhub.constants.CredentialWriteMode;
import org.cloudfoundry.credhub.credential.CredentialValue;
import org.cloudfoundry.credhub.data.CertificateAuthorityService;
import org.cloudfoundry.credhub.data.CredentialDataService;
import org.cloudfoundry.credhub.data.CredentialVersionDataService;
import org.cloudfoundry.credhub.domain.CertificateCredentialVersion;
import org.cloudfoundry.credhub.domain.CredentialFactory;
import org.cloudfoundry.credhub.domain.CredentialVersion;
import org.cloudfoundry.credhub.entity.Credential;
import org.cloudfoundry.credhub.exceptions.EntryNotFoundException;
import org.cloudfoundry.credhub.exceptions.InvalidQueryParameterException;
import org.cloudfoundry.credhub.exceptions.ParameterizedValidationException;
import org.cloudfoundry.credhub.exceptions.PermissionException;
import org.cloudfoundry.credhub.request.BaseCredentialRequest;
import org.cloudfoundry.credhub.request.PermissionEntry;
import org.cloudfoundry.credhub.request.PermissionOperation;
import org.cloudfoundry.credhub.view.FindCredentialResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cloudfoundry.credhub.request.PermissionOperation.DELETE;
import static org.cloudfoundry.credhub.request.PermissionOperation.READ;
import static org.cloudfoundry.credhub.request.PermissionOperation.WRITE;
import static org.cloudfoundry.credhub.request.PermissionOperation.WRITE_ACL;

@Service
public class PermissionedCredentialService {

  private final CredentialVersionDataService credentialVersionDataService;

  private final CredentialFactory credentialFactory;
  private final CertificateAuthorityService certificateAuthorityService;
  private PermissionCheckingService permissionCheckingService;
  private final UserContextHolder userContextHolder;
  private final CredentialDataService credentialDataService;
  private final CEFAuditRecord auditRecord;

  @Autowired
  public PermissionedCredentialService(
      CredentialVersionDataService credentialVersionDataService,
      CredentialFactory credentialFactory,
      PermissionCheckingService permissionCheckingService,
      CertificateAuthorityService certificateAuthorityService,
      UserContextHolder userContextHolder,
      CredentialDataService credentialDataService,
      CEFAuditRecord auditRecord) {
    this.credentialVersionDataService = credentialVersionDataService;
    this.credentialFactory = credentialFactory;
    this.permissionCheckingService = permissionCheckingService;
    this.certificateAuthorityService = certificateAuthorityService;
    this.userContextHolder = userContextHolder;
    this.credentialDataService = credentialDataService;
    this.auditRecord = auditRecord;
  }

  public CredentialVersion save(
      CredentialVersion existingCredentialVersion,
      CredentialValue credentialValue,
      BaseCredentialRequest generateRequest) {
    List<PermissionEntry> accessControlEntries = generateRequest.getAdditionalPermissions();
    boolean shouldWriteNewCredential = shouldWriteNewCredential(existingCredentialVersion, generateRequest);

    validateCredentialSave(generateRequest.getName(), generateRequest.getType(), accessControlEntries,
        existingCredentialVersion);

    if (!shouldWriteNewCredential) {
      return existingCredentialVersion;
    }

    return makeAndSaveNewCredential(existingCredentialVersion, credentialValue, generateRequest);
  }

  public boolean delete(String credentialName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, DELETE)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }
    return credentialVersionDataService.delete(credentialName);
  }

  public List<CredentialVersion> findAllByName(String credentialName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    List<CredentialVersion> credentialList = credentialVersionDataService.findAllByName(credentialName);

    for (CredentialVersion credentialVersion : credentialList) {
      auditRecord.addVersion(credentialVersion);
      auditRecord.addResource(credentialVersion.getCredential());
    }

    return credentialList;
  }

  public List<CredentialVersion> findNByName(String credentialName, Integer numberOfVersions) {
    if (numberOfVersions < 0) {
      throw new InvalidQueryParameterException("error.invalid_query_parameter", "versions");
    }

    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    return credentialVersionDataService.findNByName(credentialName, numberOfVersions);
  }

  public List<CredentialVersion> findActiveByName(String credentialName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }
    List<CredentialVersion> credentialList = credentialVersionDataService.findActiveByName(credentialName);

    for (CredentialVersion credentialVersion : credentialList) {
      auditRecord.addVersion(credentialVersion);
      auditRecord.addResource(credentialVersion.getCredential());
    }

    return credentialList;
  }

  public Credential findByUuid(UUID credentialUUID) {
    Credential credential = credentialDataService.findByUUID(credentialUUID);
    if (credential == null) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credential.getName(), READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }
    return credential;
  }

  public CredentialVersion findVersionByUuid(String credentialUUID) {
    CredentialVersion credentialVersion = credentialVersionDataService.findByUuid(credentialUUID);

    auditRecord.setRequestDetails(new GetCredentialById(credentialUUID));

    if (credentialVersion != null) {
      auditRecord.setVersion(credentialVersion);
      auditRecord.setResource(credentialVersion.getCredential());
    } else {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    String credentialName = credentialVersion.getName();

    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }
    return credentialVersionDataService.findByUuid(credentialUUID);
  }

  public List<String> findAllCertificateCredentialsByCaName(String caName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), caName, PermissionOperation.READ)) {
      throw new EntryNotFoundException("error.credential.invalid_access");
    }

    return credentialVersionDataService.findAllCertificateCredentialsByCaName(caName);
  }

  public List<FindCredentialResult> findStartingWithPath(String path) {
    return filterPermissions(credentialVersionDataService.findStartingWithPath(path));
  }

  public List<String> findAllPaths() {
    List<FindCredentialResult> filteredCredentials = filterPermissions(credentialVersionDataService.findStartingWithPath("/"));

    return splitAllPaths(filteredCredentials);
  }

  public List<FindCredentialResult> findContainingName(String name) {
    return filterPermissions(credentialVersionDataService.findContainingName(name));
  }

  public CredentialVersion findMostRecent(String credentialName) {
    return credentialVersionDataService.findMostRecent(credentialName);
  }

  private CredentialVersion makeAndSaveNewCredential(CredentialVersion existingCredentialVersion,
      CredentialValue credentialValue, BaseCredentialRequest request) {
    CredentialVersion newVersion = credentialFactory.makeNewCredentialVersion(
        CredentialType.valueOf(request.getType()),
        request.getName(),
        credentialValue,
        existingCredentialVersion,
        request.getGenerationParameters());
    return credentialVersionDataService.save(newVersion);
  }

  private boolean shouldWriteNewCredential(CredentialVersion existingCredentialVersion, BaseCredentialRequest request) {
    boolean shouldWriteNewCredential;
    if (existingCredentialVersion == null) {
      shouldWriteNewCredential = true;
    } else if (request.getOverwriteMode().equals(CredentialWriteMode.CONVERGE.mode)) {
      if (existingCredentialVersion instanceof CertificateCredentialVersion) {
        final CertificateCredentialVersion certificateCredentialVersion = (CertificateCredentialVersion) existingCredentialVersion;
        if (certificateCredentialVersion.getCaName() != null) {
          boolean updatedCA = !certificateCredentialVersion.getCa().equals(
              certificateAuthorityService.findActiveVersion(certificateCredentialVersion.getCaName()).getCertificate());
          if (updatedCA) {
            return true;
          }
        }
      }
      shouldWriteNewCredential = !existingCredentialVersion
          .matchesGenerationParameters(request.getGenerationParameters());
    } else {
      shouldWriteNewCredential = request.getOverwriteMode().equals(CredentialWriteMode.OVERWRITE.mode);
    }
    return shouldWriteNewCredential;
  }

  private void validateCredentialSave(String credentialName, String type, List<PermissionEntry> accessControlEntries,
      CredentialVersion existingCredentialVersion) {
    if (existingCredentialVersion != null) {
      verifyCredentialWritePermission(credentialName);
    }

    if (existingCredentialVersion != null && accessControlEntries.size() > 0) {
      verifyWritePermission(credentialName);
    }

    if (existingCredentialVersion != null && !existingCredentialVersion.getCredentialType().equals(type)) {
      throw new ParameterizedValidationException("error.type_mismatch");
    }
  }

  private void verifyCredentialWritePermission(String credentialName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, WRITE)) {
      throw new PermissionException("error.credential.invalid_access");
    }
  }

  private void verifyWritePermission(String credentialName) {
    if (!permissionCheckingService
        .hasPermission(userContextHolder.getUserContext().getActor(), credentialName, WRITE_ACL)) {
      throw new PermissionException("error.credential.invalid_access");
    }
  }

  private List<FindCredentialResult> filterPermissions(List<FindCredentialResult> unfilteredResult) {
    if (!permissionCheckingService.enforcePermissions()) {
      return unfilteredResult;
    }

    List<FindCredentialResult> filteredResult = new ArrayList<>(unfilteredResult);

    if(!unfilteredResult.isEmpty()) {
      for (FindCredentialResult credentialResult : unfilteredResult) {
        if (!permissionCheckingService
          .hasPermission(userContextHolder.getUserContext().getActor(), credentialResult.getName(), READ)) {
          filteredResult.remove(credentialResult);
        }
      }
    }
    return filteredResult;
  }

  private List<String> splitAllPaths(List<FindCredentialResult> filteredPaths) {
    return filteredPaths
      .stream()
      .map(FindCredentialResult::getName)
      .flatMap(PermissionedCredentialService::fullHierarchyForPath)
      .distinct()
      .sorted()
      .collect(Collectors.toList());
  }

  private static Stream<String> fullHierarchyForPath(String path) {
    String[] components = path.split("/");
    if (components.length > 1) {
      StringBuilder currentPath = new StringBuilder();
      List<String> pathSet = new ArrayList<>();
      for (int i = 0; i < components.length - 1; i++) {
        String element = components[i];
        currentPath.append(element).append('/');
        pathSet.add(currentPath.toString());
      }
      return pathSet.stream();
    } else {
      return Stream.of();
    }
  }

}
