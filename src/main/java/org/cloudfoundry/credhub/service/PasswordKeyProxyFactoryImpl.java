package org.cloudfoundry.credhub.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.cloudfoundry.credhub.config.EncryptionKeyMetadata;

import static org.cloudfoundry.credhub.constants.EncryptionConstants.ITERATIONS;

@Component
@Profile("!unit-test")
public class PasswordKeyProxyFactoryImpl implements PasswordKeyProxyFactory {
  public KeyProxy createPasswordKeyProxy(EncryptionKeyMetadata encryptionKeyMetadata, InternalEncryptionService encryptionService) {
    return new PasswordBasedKeyProxy(encryptionKeyMetadata.getEncryptionPassword(), ITERATIONS, encryptionService);
  }
}
