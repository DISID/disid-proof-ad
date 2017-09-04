package com.disid.ad.integration.ad;

import com.disid.ad.model.User;

/**
 * {@link ActiveDirectoryService} to manage {@link User} entities as ActiveDirectory users.
 */
public interface ActiveDirectoryUserService extends ActiveDirectoryService<User>
{
  /**
   * Sets or updates the password of a user in the ActiveDirectory service.
   * @param user to set the password of
   * @param password to set
   */
  void changePassword( User user, String password );

}
