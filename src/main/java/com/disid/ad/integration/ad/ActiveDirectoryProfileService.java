package com.disid.ad.integration.ad;

import com.disid.ad.model.Profile;

/**
 * {@link ActiveDirectoryService} to manage {@link Profile} entities as ActiveDirectory groups.
 */
public interface ActiveDirectoryProfileService extends ActiveDirectoryService<Profile>
{
  public static final String DEFAULT_NAME_ATTRIBUTE = "cn";
  public static final String DEFAULT_SEARCH_BASE = "cn=Users";
  public static final String DEFAULT_SEARCH_FILTER = // 
      "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
  public static final String[] DEFAULT_OBJECT_CLASSES = new String[] { "top", "group" };
}
