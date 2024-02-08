package com.example.taskmanagementapp.security.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.acls.AclPermissionEvaluator;
import org.springframework.security.acls.domain.*;
import org.springframework.security.acls.jdbc.BasicLookupStrategy;
import org.springframework.security.acls.jdbc.JdbcMutableAclService;
import org.springframework.security.acls.model.AclCache;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;

import javax.sql.DataSource;

@EnableCaching
@Configuration
@EnableMethodSecurity
public class AclConfig {

    @Bean
    public JdbcMutableAclService jdbcMutableAclService(DataSource dataSource, CacheManager cacheManager) {

        AclAuthorizationStrategy aclAuthorizationStrategy = new AclAuthorizationStrategyImpl(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        ConsoleAuditLogger auditLogger = new ConsoleAuditLogger();

        AclCache aclCache = new SpringCacheBasedAclCache(
                cacheManager.getCache("security/acl"), new DefaultPermissionGrantingStrategy(
                auditLogger), aclAuthorizationStrategy
        );

        BasicLookupStrategy lookupStrategy = new BasicLookupStrategy(
                dataSource, aclCache, aclAuthorizationStrategy, auditLogger

        );
        lookupStrategy.setAclClassIdSupported(true);
        JdbcMutableAclService jdbcMutableAclService = new JdbcMutableAclService(
                dataSource, lookupStrategy, aclCache);
        jdbcMutableAclService.setClassIdentityQuery("SELECT @@IDENTITY");
        jdbcMutableAclService.setSidIdentityQuery("SELECT @@IDENTITY");
        jdbcMutableAclService.setAclClassIdSupported(true);
        return jdbcMutableAclService;

    }

    @Bean
    public AclPermissionEvaluator permissionEvaluator(MutableAclService aclService){
        return new AclPermissionEvaluator(aclService);
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(AclPermissionEvaluator permissionEvaluator,
                                                                                  ApplicationContext applicationContext){
        DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler =
                new DefaultMethodSecurityExpressionHandler();
        methodSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator);
        methodSecurityExpressionHandler.setApplicationContext(applicationContext);
        return methodSecurityExpressionHandler;
    }

    @Bean
    public DefaultHttpSecurityExpressionHandler defaultHttpSecurityExpressionHandler(AclPermissionEvaluator permissionEvaluator,
                                                                                ApplicationContext applicationContext){
        DefaultHttpSecurityExpressionHandler httpSecurityExpressionHandler = new DefaultHttpSecurityExpressionHandler();
        httpSecurityExpressionHandler.setPermissionEvaluator(permissionEvaluator);
        httpSecurityExpressionHandler.setApplicationContext(applicationContext);
        return httpSecurityExpressionHandler;
    }


}
