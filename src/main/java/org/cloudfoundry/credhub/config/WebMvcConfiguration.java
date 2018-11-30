package org.cloudfoundry.credhub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.cloudfoundry.credhub.interceptor.AuditInterceptor;
import org.cloudfoundry.credhub.interceptor.ManagementInterceptor;
import org.cloudfoundry.credhub.interceptor.UserContextInterceptor;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
  private final AuditInterceptor auditInterceptor;
  private final UserContextInterceptor userContextInterceptor;
  private ManagementInterceptor managementInterceptor;

  @Autowired
  public WebMvcConfiguration(
    AuditInterceptor auditInterceptor,
    UserContextInterceptor userContextInterceptor,
    ManagementInterceptor managementInterceptor) {
    this.userContextInterceptor = userContextInterceptor;
    this.auditInterceptor = auditInterceptor;
    this.managementInterceptor = managementInterceptor;
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorPathExtension(false);
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setUseSuffixPatternMatch(false);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(auditInterceptor).excludePathPatterns("/info", "/health", "/**/key-usage", "/version");
    registry.addInterceptor(managementInterceptor);
    registry.addInterceptor(userContextInterceptor).excludePathPatterns("/info", "/health", "/**/key-usage", "/management");
  }
}
