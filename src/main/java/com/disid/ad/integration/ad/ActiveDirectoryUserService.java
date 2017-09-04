package com.disid.ad.integration.ad;

import com.disid.ad.model.User;

/**
 * {@link ActiveDirectoryService} to manage {@link User} entities as ActiveDirectory users.
 */
public interface ActiveDirectoryUserService extends ActiveDirectoryService<User>
{
  public static final String DEFAULT_NAME_ATTRIBUTE = "cn";
  public static final String DEFAULT_SEARCH_BASE = "cn=Users";
  public static final String DEFAULT_SEARCH_FILTER = // 
      "(&(objectClass=user)(!(isCriticalSystemObject=TRUE)))";
  public static final String[] DEFAULT_OBJECT_CLASSES =
      new String[] { "top", "person", "organizationalPerson", "user" };
  public static final String DEFAULT_LOGIN_ATTRIBUTE = "sAMAccountName";
  public static final String DEFAULT_PASSWORD_ATTRIBUTE = "userPassword";


  public static final int ADS_UF_SCRIPT = 1; // 0x1
  public static final int ADS_UF_ACCOUNTDISABLE = 2; // 0x2
  public static final int ADS_UF_HOMEDIR_REQUIRED = 8; // 0x8
  public static final int ADS_UF_LOCKOUT = 16; // 0x10
  public static final int ADS_UF_PASSWD_NOTREQD = 32; // 0x20
  public static final int ADS_UF_PASSWD_CANT_CHANGE = 64; // 0x40
  public static final int ADS_UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED = 128; // 0x80
  public static final int ADS_UF_TEMP_DUPLICATE_ACCOUNT = 256; // 0x100
  public static final int ADS_UF_NORMAL_ACCOUNT = 512; // 0x200
  public static final int ADS_UF_INTERDOMAIN_TRUST_ACCOUNT = 2048; // 0x800
  public static final int ADS_UF_WORKSTATION_TRUST_ACCOUNT = 4096; // 0x1000
  public static final int ADS_UF_SERVER_TRUST_ACCOUNT = 8192; // 0x2000
  public static final int ADS_UF_DONT_EXPIRE_PASSWD = 65536; // 0x10000
  public static final int ADS_UF_MNS_LOGON_ACCOUNT = 131072; // 0x20000
  public static final int ADS_UF_SMARTCARD_REQUIRED = 262144; // 0x40000
  public static final int ADS_UF_TRUSTED_FOR_DELEGATION = 524288; // 0x80000
  public static final int ADS_UF_NOT_DELEGATED = 1048576; // 0x100000
  public static final int ADS_UF_USE_DES_KEY_ONLY = 2097152; // 0x200000
  public static final int ADS_UF_DONT_REQUIRE_PREAUTH = 4194304; // 0x400000
  public static final int ADS_UF_PASSWORD_EXPIRED = 8388608; // 0x800000
  public static final int ADS_UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION = 16777216; // 0x1000000


  public static final String ACCOUNT_CONTROL_PRE_PASSWORD = Integer
      .toString( ADS_UF_NORMAL_ACCOUNT + ADS_UF_PASSWD_NOTREQD + ADS_UF_PASSWORD_EXPIRED + ADS_UF_ACCOUNTDISABLE );
  public static final String ACCOUNT_CONTROL_POST_PASSWORD =
      Integer.toString( ADS_UF_NORMAL_ACCOUNT | ADS_UF_DONT_EXPIRE_PASSWD );

  /**
   * Sets or updates the password of a user in the ActiveDirectory service.
   * @param user to set the password of
   * @param password to set
   */
  void changePassword( User user, String password );

}
