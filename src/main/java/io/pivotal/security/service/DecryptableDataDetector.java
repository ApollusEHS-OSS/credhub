package io.pivotal.security.service;

import io.pivotal.security.data.CredentialDataService;
import java.util.ArrayList;
import java.util.UUID;

public class DecryptableDataDetector {

  private EncryptionKeyCanaryMapper encryptionKeyCanaryMapper;
  private CredentialDataService credentialDataService;

  DecryptableDataDetector(EncryptionKeyCanaryMapper encryptionKeyCanaryMapper,
                          CredentialDataService credentialDataService) {
    this.encryptionKeyCanaryMapper = encryptionKeyCanaryMapper;
    this.credentialDataService = credentialDataService;
  }

  public void check() {
    ArrayList<UUID> uuids = encryptionKeyCanaryMapper.getKnownCanaryUuids();

    Long countTotalSecrets = credentialDataService.count();
    Long countSecretsEncryptedWithKeyWeHave = credentialDataService.countEncryptedWithKeyUuidIn(uuids);
    if (countTotalSecrets > 0 && countSecretsEncryptedWithKeyWeHave == 0) {
      throw new RuntimeException(
          "The encryption keys provided cannot decrypt any of the " + countTotalSecrets
              + " value(s) in the database. "
              + "Please make sure you've provided the necessary encryption keys.");
    }
  }
}
