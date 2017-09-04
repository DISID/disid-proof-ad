package com.disid.ad.config;

import com.disid.ad.config.LdapProperties.Context;
import com.disid.ad.integration.ldap.LdapProfileServiceImpl;
import com.disid.ad.integration.ldap.LdapService;
import com.disid.ad.integration.ldap.UpdatingLdifPopulator;
import com.disid.ad.model.Profile;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import javax.naming.NamingException;

/**
 * Configuration of LDAP services.
 */
@Configuration
public class LdapConfiguration
{

  @Configuration
  @Order( Ordered.HIGHEST_PRECEDENCE )
  @org.springframework.context.annotation.Profile( "dev" ) // This configuration is enabled when the application is running in a development environment
  protected static class LdapDevConfiguration
  {
    /**
     * LDAP configuration properties
     */
    private final LdapProperties ldapProperties;

    /**
     * Constructor. As it is the single constructor of the class, it is 
     * used automatically by Spring to autowire the parameters.
     * @param ldapProperties LDAP configuration properties
     */
    public LdapDevConfiguration( LdapProperties ldapProperties )
    {
      this.ldapProperties = ldapProperties;
    }

    private InMemoryDirectoryServer server;

    @Bean
    public InMemoryDirectoryServer directoryServer() throws LDAPException
    {
      Context context = ldapProperties.getContext();
      InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig( context.getBaseDn() );
      if ( hasCredentials( context ) )
      {
        config.addAdditionalBindCredentials( context.getUserDn(), context.getPassword() );
      }
      config.setSchema( null );
      InMemoryListenerConfig listenerConfig = InMemoryListenerConfig.createLDAPConfig( "LDAP", 8389 );
      config.setListenerConfigs( listenerConfig );
      this.server = new InMemoryDirectoryServer( config );
      this.server.startListening();
      return this.server;
    }

    @PreDestroy
    public void close()
    {
      if ( this.server != null )
      {
        this.server.shutDown( true );
      }
    }

    private boolean hasCredentials( Context context )
    {
      return StringUtils.hasText( context.getUserDn() ) && StringUtils.hasText( context.getPassword() );
    }

  }

  @Configuration
  protected static class LdapMainConfiguration
  {
    /**
     * LDAP configuration properties
     */
    private final LdapProperties ldapProperties;
    private ConfigurableApplicationContext applicationContext;

    /**
     * Constructor. As it is the single constructor of the class, it is 
     * used automatically by Spring to autowire the parameters.
     * @param ldapProperties LDAP configuration properties
     */
    public LdapMainConfiguration( LdapProperties ldapProperties, ConfigurableApplicationContext applicationContext )
    {
      this.ldapProperties = ldapProperties;
      this.applicationContext = applicationContext;
    }

    /**
     * Creates the LDAP context source with the parameters to connect to the LDAP server.
     *
     * @return {@link DefaultSpringSecurityContextSource}
     */
    @Bean
    public DefaultSpringSecurityContextSource contextSource()
    {
      DefaultSpringSecurityContextSource contextSource = null;

      LdapProperties.Context context = ldapProperties.getContext();

      if ( !StringUtils.isEmpty( context.getUrl() ) )
      {
        contextSource = new DefaultSpringSecurityContextSource( context.getUrl() );
        contextSource.setBase( context.getBaseDn() );
        contextSource.setUserDn( context.getUserDn() );
        contextSource.setPassword( context.getPassword() );
      }
      return contextSource;
    }

    /**
     * Creates a {@link LdapTemplate} for Active Directory (AD).
     *
     * @return the template to perform LDAP operations
     */
    @Bean
    public LdapTemplate ldapTemplate()
    {
      LdapTemplate ldap = new LdapTemplate( contextSource() );
      // For Active Directory (AD) users. See LdapTemplate doc.
      ldap.setIgnorePartialResultException( true );
      return ldap;
    }

    /**
     * Returns the service to manage groups in the LDAP service.
     * @return the LDAP groups service
     */
    @Bean
    public LdapService<Profile> ldapProfileService()
    {
      LdapProperties.Sync.Group group = ldapProperties.getSync().getGroup();
      return new LdapProfileServiceImpl( ldapTemplate(), group.getObjectClassValues(), group.getSearchBase(),
          group.getSearchFilter() );
    }

    //  /**
    //   * Returns the service to manage users in the LDAP service.
    //   * @return the LDAP users service
    //   */
    //  @Bean
    //  public LdapService<LocalUser> ldapUserService()
    //  {
    //    LdapProperties.Sync.User user = ldapProperties.getSync().getUser();
    //    return new LdapUserServiceImpl( ldapTemplate(), user.getObjectClass(), user.getIdAttribute(),
    //        user.getNameAttribute(), ldapProperties.getAuth().getPasswordAttribute(), user.getAccountAttribute(),
    //        user.getObjectClassValues(),
    //        user.getBaseRdn() );
    //  }

    @Bean
    public UpdatingLdifPopulator ldifPopulator() throws NamingException
    {
      UpdatingLdifPopulator populator = null;
      String ldif = ldapProperties.getContext().getLdif();
      if ( !StringUtils.isEmpty( ldif ) )
      {
        Resource resource = applicationContext.getResource( ldif );
        if ( resource.exists() )
        {
          populator = new UpdatingLdifPopulator( resource, contextSource(), false );
        }
      }
      return populator;
    }
  }

}
