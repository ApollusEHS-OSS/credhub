package org.cloudfoundry.credhub.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;
import org.cloudfoundry.credhub.auth.UserContext;
import org.cloudfoundry.credhub.auth.UserContextHolder;
import org.cloudfoundry.credhub.data.PermissionDataService;
import org.cloudfoundry.credhub.entity.PermissionData;
import org.cloudfoundry.credhub.request.PermissionOperation;

@Component
public class PermissionCheckingService {
  private PermissionDataService permissionDataService;
  private UserContextHolder userContextHolder;

  @Value("${security.authorization.acls.enabled}")
  private boolean enforcePermissions;

  @Autowired
  public PermissionCheckingService(PermissionDataService permissionDataService,
                                   UserContextHolder userContextHolder) {
    this.permissionDataService = permissionDataService;
    this.userContextHolder = userContextHolder;
  }

  public boolean hasPermission(String user, String credentialName, PermissionOperation permission) {
    if (enforcePermissions) {
      return permissionDataService.hasPermission(user, credentialName, permission);
    }
    return true;
  }

  public boolean hasPermission(String user, UUID guid, PermissionOperation permission) {
    if (enforcePermissions) {
      PermissionData permissionData = permissionDataService.getPermission(guid);
      if (permissionData == null) {
        return false;
      }
      return permissionDataService.hasPermission(user, permissionData.getPath(), permission);
    }
    return true;
  }

  public boolean hasPermissions(String user, String path, List<PermissionOperation> permissions) {
    for (PermissionOperation permission : permissions) {
      if (!permissionDataService.hasPermission(user, path, permission)) {
        return false;
      }
    }
    return true;
  }

  public boolean userAllowedToOperateOnActor(String actor) {
    if (enforcePermissions) {
      UserContext userContext = userContextHolder.getUserContext();
      return actor != null &&
        userContext.getActor() != null &&
        !StringUtils.equals(userContext.getActor(), actor);
    } else {
      return true;
    }
  }

  public boolean userAllowedToOperateOnActor(UUID guid) {
    if (enforcePermissions) {
      UserContext userContext = userContextHolder.getUserContext();
      String actor = permissionDataService.getPermission(guid).getActor();
      return actor != null &&
        userContext.getActor() != null &&
        !StringUtils.equals(userContext.getActor(), actor);
    } else {
      return true;
    }
  }

}
