package com.disid.ad.integration.ldap;

import com.disid.ad.model.User;

/**
 * {@link LdapService} to manage {@link User} entities as ActiveDirectory users.
 */
public interface LdapUserService extends LdapService<User>
{
  public static final String DEFAULT_NAME_ATTRIBUTE = "cn";
  public static final String DEFAULT_SEARCH_BASE = "cn=Users";
  public static final String DEFAULT_SEARCH_FILTER = // 
      "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
  public static final String[] DEFAULT_OBJECT_CLASSES = new String[] { "top", "group" };
  public static final String DEFAULT_LOGIN_ATTRIBUTE = "cn";
  public static final String DEFAULT_PASSWORD_ATTRIBUTE = "userPassword";

  /**
   * Sets or updates the password of a user in the ActiveDirectory service.
   * @param user to set the password of
   * @param password to set
   */
  void changePassword( User user, String password );

}
