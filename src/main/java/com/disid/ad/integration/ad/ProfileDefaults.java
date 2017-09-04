package com.disid.ad.integration.ad;

/**
 * Default values for the configuration of the integration with Profile (groups)
 * in ActiveDirectory.
 */
public interface ProfileDefaults
{
  static final String NAME_ATTRIBUTE = "cn";
  static final String SEARCH_BASE = "cn=Users";
  static final String SEARCH_FILTER = // 
      "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
  static final String[] OBJECT_CLASSES = new String[] { "top", "group" };
}
