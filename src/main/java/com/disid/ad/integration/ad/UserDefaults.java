package com.disid.ad.integration.ad;

/**
 * Default values for the configuration of the integration with users
 * in ActiveDirectory.
 */
public interface UserDefaults
{
  static final String NAME_ATTRIBUTE = "cn";
  static final String SEARCH_BASE = "cn=Users";
  static final String SEARCH_FILTER = "(&(objectClass=user)(!(isCriticalSystemObject=TRUE)))";
  static final String[] OBJECT_CLASSES = new String[] { "top", "person", "organizationalPerson", "user" };
  static final String LOGIN_ATTRIBUTE = "sAMAccountName";
  static final String PASSWORD_ATTRIBUTE = "userPassword";


  static final int ADS_UF_SCRIPT = 1; // 0x1
  static final int ADS_UF_ACCOUNTDISABLE = 2; // 0x2
  static final int ADS_UF_HOMEDIR_REQUIRED = 8; // 0x8
  static final int ADS_UF_LOCKOUT = 16; // 0x10
  static final int ADS_UF_PASSWD_NOTREQD = 32; // 0x20
  static final int ADS_UF_PASSWD_CANT_CHANGE = 64; // 0x40
  static final int ADS_UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED = 128; // 0x80
  static final int ADS_UF_TEMP_DUPLICATE_ACCOUNT = 256; // 0x100
  static final int ADS_UF_NORMAL_ACCOUNT = 512; // 0x200
  static final int ADS_UF_INTERDOMAIN_TRUST_ACCOUNT = 2048; // 0x800
  static final int ADS_UF_WORKSTATION_TRUST_ACCOUNT = 4096; // 0x1000
  static final int ADS_UF_SERVER_TRUST_ACCOUNT = 8192; // 0x2000
  static final int ADS_UF_DONT_EXPIRE_PASSWD = 65536; // 0x10000
  static final int ADS_UF_MNS_LOGON_ACCOUNT = 131072; // 0x20000
  static final int ADS_UF_SMARTCARD_REQUIRED = 262144; // 0x40000
  static final int ADS_UF_TRUSTED_FOR_DELEGATION = 524288; // 0x80000
  static final int ADS_UF_NOT_DELEGATED = 1048576; // 0x100000
  static final int ADS_UF_USE_DES_KEY_ONLY = 2097152; // 0x200000
  static final int ADS_UF_DONT_REQUIRE_PREAUTH = 4194304; // 0x400000
  static final int ADS_UF_PASSWORD_EXPIRED = 8388608; // 0x800000
  static final int ADS_UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION = 16777216; // 0x1000000

  static final String ACCOUNT_CONTROL_PRE_PASSWORD = Integer
      .toString( ADS_UF_NORMAL_ACCOUNT + ADS_UF_PASSWD_NOTREQD + ADS_UF_PASSWORD_EXPIRED + ADS_UF_ACCOUNTDISABLE );
  static final String ACCOUNT_CONTROL_POST_PASSWORD =
      Integer.toString( ADS_UF_NORMAL_ACCOUNT | ADS_UF_DONT_EXPIRE_PASSWD );
}
