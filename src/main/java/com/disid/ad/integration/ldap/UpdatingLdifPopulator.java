package com.disid.ad.integration.ldap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.ldif.parser.LdifParser;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.ldap.test.LdapTestUtils;
import org.springframework.util.Assert;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapName;

public class UpdatingLdifPopulator implements InitializingBean
{

  private final Resource resource;
  private final ContextSource contextSource;
  private final boolean clean;

  public UpdatingLdifPopulator( Resource resource, ContextSource contextSource, boolean clean )
  {
    this.resource = resource;
    this.contextSource = contextSource;
    this.clean = clean;
  }

  @Override
  public void afterPropertiesSet() throws Exception
  {
    Assert.notNull( contextSource, "ContextSource must be specified" );
    Assert.notNull( resource, "Resource must be specified" );

    if ( clean )
    {
      LdapTestUtils.clearSubContexts( contextSource, LdapUtils.emptyLdapName() );
    }

    DirContext context = contextSource.getReadWriteContext();
    try
    {
      loadLdif( context, LdapUtils.emptyLdapName(), resource );
    }
    finally
    {
      try
      {
        context.close();
      }
      catch ( Exception e )
      {
        // This is not the exception we are interested in.
      }
    }
  }

  private void loadLdif( DirContext context, LdapName rootNode, Resource ldifFile )
  {
    try
    {
      LdapName baseDn = (LdapName) context.getEnvironment().get( DefaultDirObjectFactory.JNDI_ENV_BASE_PATH_KEY );

      LdifParser parser = new LdifParser( ldifFile );
      parser.open();
      while ( parser.hasMoreRecords() )
      {
        LdapAttributes record = parser.getRecord();

        LdapName dn = record.getName();

        if ( baseDn != null )
        {
          dn = LdapUtils.removeFirst( dn, baseDn );
        }

        if ( !rootNode.isEmpty() )
        {
          dn = LdapUtils.prepend( dn, rootNode );
        }

        if ( exists( context, dn ) )
        {
          context.rebind( dn, null, record );
        }
        else
        {
          context.bind( dn, null, record );
        }

      }
    }
    catch ( Exception e )
    {
      throw new UncategorizedLdapException( "Failed to populate LDIF", e );
    }

  }

  private boolean exists( DirContext context, LdapName dn )
  {
    try
    {
      context.lookup( dn );
    }
    catch ( NamingException ex )
    {
      return false;
    }
    return true;
  }

}
