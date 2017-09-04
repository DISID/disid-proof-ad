package com.disid.ad.config;

import com.disid.ad.config.ActiveDirectoryProperties.Context;
import com.disid.ad.integration.ad.ActiveDirectoryProfileServiceImpl;
import com.disid.ad.integration.ad.ActiveDirectoryService;
import com.disid.ad.integration.ad.ActiveDirectoryUserServiceImpl;
import com.disid.ad.integration.ad.ProfileDnBuilder;
import com.disid.ad.integration.ad.UpdatingLdifPopulator;
import com.disid.ad.integration.ad.UserDefaults;
import com.disid.ad.integration.ad.UserDnBuilder;
import com.disid.ad.model.Profile;
import com.disid.ad.model.User;

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
 * Configuration of ActiveDirectory services.
 */
@Configuration
public class ActiveDirectoryConfiguration
{

  @Configuration
  @Order( Ordered.HIGHEST_PRECEDENCE )
  @org.springframework.context.annotation.Profile( "dev" ) // This configuration is enabled when the application is running in a development environment
  protected static class LdapDevConfiguration
  {
    /**
     * ActiveDirectory configuration properties
     */
    private final ActiveDirectoryProperties ldapProperties;

    /**
     * Constructor. As it is the single constructor of the class, it is 
     * used automatically by Spring to autowire the parameters.
     * @param ldapProperties ActiveDirectory configuration properties
     */
    public LdapDevConfiguration( ActiveDirectoryProperties ldapProperties )
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
     * ActiveDirectory configuration properties
     */
    private final ActiveDirectoryProperties ldapProperties;
    private ConfigurableApplicationContext applicationContext;

    /**
     * Constructor. As it is the single constructor of the class, it is 
     * used automatically by Spring to autowire the parameters.
     * @param ldapProperties ActiveDirectory configuration properties
     */
    public LdapMainConfiguration( ActiveDirectoryProperties ldapProperties,
        ConfigurableApplicationContext applicationContext )
    {
      this.ldapProperties = ldapProperties;
      this.applicationContext = applicationContext;
    }

    /**
     * Creates the ActiveDirectory context source with the parameters to connect to the ActiveDirectory server.
     *
     * @return {@link DefaultSpringSecurityContextSource}
     */
    @Bean
    public DefaultSpringSecurityContextSource contextSource()
    {
      DefaultSpringSecurityContextSource contextSource = null;

      ActiveDirectoryProperties.Context context = ldapProperties.getContext();

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
     * @return the template to perform ActiveDirectory operations
     */
    @Bean
    public LdapTemplate ldapTemplate()
    {
      LdapTemplate ldap = new LdapTemplate( contextSource() );
      // For Active Directory (AD) users. See LdapTemplate doc.
      ldap.setIgnorePartialResultException( true );
      return ldap;
    }

    @Bean
    public ProfileDnBuilder profileDnBuilder()
    {
      return new ProfileDnBuilder( ldapProperties.getSync().getGroup().getSearchBase() );
    }

    @Bean
    public UserDnBuilder userDnBuilder()
    {
      //return new UserDnBuilder( ldapProperties.getSync().getUser().getSearchBase() );
      return new UserDnBuilder( UserDefaults.SEARCH_BASE );
    }

    /**
     * Returns the service to manage groups in the ActiveDirectory service.
     * @return the ActiveDirectory groups service
     */
    @Bean
    public ActiveDirectoryService<Profile> ldapProfileService()
    {
      ActiveDirectoryProperties.Sync.Group group = ldapProperties.getSync().getGroup();
      return new ActiveDirectoryProfileServiceImpl( ldapTemplate(), group.getObjectClassValues(), profileDnBuilder(),
          group.getSearchFilter(), userDnBuilder() );
    }

    /**
     * Returns the service to manage users in the ActiveDirectory service.
     * @return the ActiveDirectory users service
     */
    @Bean
    public ActiveDirectoryService<User> ldapUserService()
    {
      ActiveDirectoryProperties.Sync.User user = ldapProperties.getSync().getUser();
      return new ActiveDirectoryUserServiceImpl( ldapTemplate() );
    }

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
