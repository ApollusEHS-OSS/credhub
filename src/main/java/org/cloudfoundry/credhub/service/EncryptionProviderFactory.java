package org.cloudfoundry.credhub.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.cloudfoundry.credhub.config.EncryptionKeyProvider;
import org.cloudfoundry.credhub.config.EncryptionKeysConfiguration;
import org.cloudfoundry.credhub.util.TimedRetry;

@Component
public class EncryptionProviderFactory {

  private EncryptionKeysConfiguration encryptionKeysConfiguration;
  private TimedRetry timedRetry;
  private PasswordKeyProxyFactory passwordKeyProxyFactory;
  private HashMap<String, EncryptionProvider> map;

  @Autowired
  public EncryptionProviderFactory(EncryptionKeysConfiguration keysConfiguration, TimedRetry timedRetry,
                                   PasswordKeyProxyFactory passwordKeyProxyFactory) {
    this.encryptionKeysConfiguration = keysConfiguration;
    this.timedRetry = timedRetry;
    this.passwordKeyProxyFactory = passwordKeyProxyFactory;
    map = new HashMap<>();
  }

  public EncryptionProvider getEncryptionService(EncryptionKeyProvider provider) throws Exception {
    EncryptionProvider encryptionService;

    if (map.containsKey(provider.getProviderName())) {
      return map.get(provider.getProviderName());
    } else {
      switch (provider.getProviderType()) {
        case HSM:
          encryptionService = new LunaEncryptionService(new LunaConnection(provider.getConfiguration()),
            encryptionKeysConfiguration.isKeyCreationEnabled(),
            timedRetry);
          break;
        case KMS_PLUGIN:
          encryptionService = new ExternalEncryptionProvider(provider.getConfiguration());
          break;
        default:
          encryptionService = new PasswordEncryptionService(passwordKeyProxyFactory);
      }
      map.put(provider.getProviderName(), encryptionService);
      return encryptionService;
    }
  }
}
