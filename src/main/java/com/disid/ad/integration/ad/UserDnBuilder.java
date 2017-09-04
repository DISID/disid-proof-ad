package com.disid.ad.integration.ad;

import com.disid.ad.model.User;

import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.ldap.LdapName;

/**
 * Builder for {@link User} {@link LdapName}s.
 */
public class UserDnBuilder
{
  private final String nameAttribute;
  private final String searchBase;

  /**
   * Creates a new builder with the given User search base.
   * @param searchBase to find users in the AD server
   */
  public UserDnBuilder( String searchBase )
  {
    this( searchBase, UserDefaults.NAME_ATTRIBUTE );
  }

  /**
   * Creates a new builder with the given User search base and name attribute
   * @param searchBase to find users in the AD server
   * @param nameAttribute the name of the attribute with the user name
   */
  public UserDnBuilder( String searchBase, String nameAttribute )
  {
    this.searchBase = searchBase;
    this.nameAttribute = nameAttribute;
  }

  /**
   * Returns the {@link LdapName} for the given user
   * @param user to get the name for
   * @return the ldap name
   */
  public LdapName getName( User user )
  {
    return getName( user.getName() );
  }

  /**
   * Returns the {@link LdapName} for the given user name
   * @param name to get the ldap name for
   * @return the ldap name
   */
  public LdapName getName( String name )
  {
    return LdapNameBuilder.newInstance( searchBase ).add( nameAttribute, name ).build();
  }

  /**
   * Returns the search base to look for the users in the AD.
   * @return the search base value
   */
  public String getSearchBase()
  {
    return searchBase;
  }
}
