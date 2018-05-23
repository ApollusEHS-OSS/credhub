package org.cloudfoundry.credhub.data;

import org.cloudfoundry.credhub.repository.AuthFailureAuditRecordRepository;
import org.cloudfoundry.credhub.repository.EventAuditRecordRepository;
import org.cloudfoundry.credhub.repository.RequestAuditRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuditCleanUpService {

  private AuthFailureAuditRecordRepository authFailureAuditRecordRepository;
  private EventAuditRecordRepository eventAuditRecordRepository;
  private RequestAuditRecordRepository requestAuditRecordRepository;

  @Autowired
  public AuditCleanUpService(AuthFailureAuditRecordRepository authFailureAuditRecordRepository,
                             EventAuditRecordRepository eventAuditRecordRepository,
                             RequestAuditRecordRepository requestAuditRecordRepository){
    this.authFailureAuditRecordRepository = authFailureAuditRecordRepository;
    this.eventAuditRecordRepository = eventAuditRecordRepository;
    this.requestAuditRecordRepository = requestAuditRecordRepository;
  }

  public void cleanUp(Integer daysRetained){
    requestAuditRecordRepository.deleteByNowBefore(cutOff(daysRetained));
    eventAuditRecordRepository.deleteByNowBefore(cutOff(daysRetained));
    authFailureAuditRecordRepository.deleteByNowBefore(cutOff(daysRetained));
  }

  private Instant cutOff(Integer daysRetained){
    Instant instant = Instant.now();
    instant = instant.minus(daysRetained, ChronoUnit.DAYS);

    return instant;
  }
}
