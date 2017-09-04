package com.disid.ad.integration.ad;

import com.disid.ad.model.Profile;

/**
 * {@link ActiveDirectoryService} to manage {@link Profile} entities as ActiveDirectory groups.
 */
public interface ActiveDirectoryProfileService extends ActiveDirectoryService<Profile>
{
  static final String DEFAULT_NAME_ATTRIBUTE = "cn";
  static final String DEFAULT_SEARCH_BASE = "cn=Users";
  static final String DEFAULT_SEARCH_FILTER = // 
      "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
  static final String[] DEFAULT_OBJECT_CLASSES = new String[] { "top", "group" };


}
