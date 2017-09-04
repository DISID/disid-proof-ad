package com.disid.ad.integration.ldap;

import com.disid.ad.config.LdapProperties.Context;
import com.disid.ad.model.User;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

/**
 * {@link LdapService} implementation to manage LDAP entries related to the {@link User}
 * entity.
 */
@Transactional
public class LdapUserServiceImpl implements LdapUserService
{
  private static final String USER_ACCOUNT_CONTROL_ATTRIBUTE = "userAccountControl";

  // see https://msdn.microsoft.com/en-us/library/aa772300(v=vs.85).aspx
  private static final int ADS_UF_SCRIPT = 1; // 0x1
  private static final int ADS_UF_ACCOUNTDISABLE = 2; // 0x2
  private static final int ADS_UF_HOMEDIR_REQUIRED = 8; // 0x8
  private static final int ADS_UF_LOCKOUT = 16; // 0x10
  private static final int ADS_UF_PASSWD_NOTREQD = 32; // 0x20
  private static final int ADS_UF_PASSWD_CANT_CHANGE = 64; // 0x40
  private static final int ADS_UF_ENCRYPTED_TEXT_PASSWORD_ALLOWED = 128; // 0x80
  private static final int ADS_UF_TEMP_DUPLICATE_ACCOUNT = 256; // 0x100
  private static final int ADS_UF_NORMAL_ACCOUNT = 512; // 0x200
  private static final int ADS_UF_INTERDOMAIN_TRUST_ACCOUNT = 2048; // 0x800
  private static final int ADS_UF_WORKSTATION_TRUST_ACCOUNT = 4096; // 0x1000
  private static final int ADS_UF_SERVER_TRUST_ACCOUNT = 8192; // 0x2000
  private static final int ADS_UF_DONT_EXPIRE_PASSWD = 65536; // 0x10000
  private static final int ADS_UF_MNS_LOGON_ACCOUNT = 131072; // 0x20000
  private static final int ADS_UF_SMARTCARD_REQUIRED = 262144; // 0x40000
  private static final int ADS_UF_TRUSTED_FOR_DELEGATION = 524288; // 0x80000
  private static final int ADS_UF_NOT_DELEGATED = 1048576; // 0x100000
  private static final int ADS_UF_USE_DES_KEY_ONLY = 2097152; // 0x200000
  private static final int ADS_UF_DONT_REQUIRE_PREAUTH = 4194304; // 0x400000
  private static final int ADS_UF_PASSWORD_EXPIRED = 8388608; // 0x800000
  private static final int ADS_UF_TRUSTED_TO_AUTHENTICATE_FOR_DELEGATION = 16777216; // 0x1000000


  private static final String ACCOUNT_CONTROL_PRE_PASSWORD = Integer
      .toString( ADS_UF_NORMAL_ACCOUNT + ADS_UF_PASSWD_NOTREQD + ADS_UF_PASSWORD_EXPIRED + ADS_UF_ACCOUNTDISABLE );
  private static final String ACCOUNT_CONTROL_POST_PASSWORD =
      Integer.toString( ADS_UF_NORMAL_ACCOUNT | ADS_UF_DONT_EXPIRE_PASSWD );

  private final LdapTemplate ldapTemplate;

  //  private final LdapShaPasswordEncoder encoder = new LdapShaPasswordEncoder();

  private final String nameAttribute = DEFAULT_NAME_ATTRIBUTE;
  private final String passwordAttribute;
  private final String loginAttribute;
  private final String[] objectClassValues;
  private final String searchBase;
  private final String searchFilter;

  /**
   * Creates a new service to manage users in the ActiveDirectory server
   * @param ldapTemplate to perform ActiveDirectory operations
   * @param mainObjectClass of the profiles in the ActiveDirectory server
   * @param idAttribute attribute which identifies uniquely a profile from its sibling entries
   * @param nameAttribute the attribute to use as the profile's name
   * @param searchBase 
   */
  public LdapUserServiceImpl( LdapTemplate ldapTemplate )
  {
    this( ldapTemplate, DEFAULT_LOGIN_ATTRIBUTE, DEFAULT_PASSWORD_ATTRIBUTE, DEFAULT_OBJECT_CLASSES,
        DEFAULT_SEARCH_BASE, DEFAULT_SEARCH_FILTER );
  }

  /**
   * Creates a new service to manage users in the ActiveDirectory server
   * @param ldapTemplate to perform LDAP operations
   * @param loginAttribute the user login attribute name
   * @param passwordAttribute the user password attribute name
   * @param objectClassValues list of object classes to create Profile entries 
   * @param searchBase the search base to apply when looking for profiles relative to the 
   * {@link Context#getBaseDn()}. This relative base will be used also to create new groups into.
   * @param searchFilter filter to apply when looking for profiles.
   */
  public LdapUserServiceImpl( LdapTemplate ldapTemplate, String loginAttribute, String passwordAttribute,
      String[] objectClassValues, String searchBase, String searchFilter )
  {
    this.ldapTemplate = ldapTemplate;
    this.passwordAttribute = passwordAttribute;
    this.loginAttribute = loginAttribute;
    this.objectClassValues = objectClassValues;
    this.searchBase = searchBase;
    this.searchFilter = searchFilter;
  }

  @Override
  public List<User> findAll( LocalDataProvider<User> provider )
  {
    return findAllWithMapper( new UserAttributesMapper( provider, nameAttribute ) );
  }

  @Override
  public List<String> synchronize( LocalDataProvider<User> provider )
  {
    List<String> names =
        findAllWithMapper( new UserSynchronizationAndLdapIdAttributesMapper( provider, nameAttribute ) );
    if ( names != null && !names.isEmpty() )
    {
      provider.deleteByNameNotIn( names );
    }
    return names;
  }

  @Override
  @Transactional
  public void create( User user )
  {
    LdapName dn = buildDn( user );
    DirContextAdapter context = new DirContextAdapter( dn );

    context.setAttributeValues( OBJECT_CLASS_ATTRIBUTE, objectClassValues );
    context.setAttributeValue( this.nameAttribute, user.getName() );
    //    context.setAttributeValue( "sn", user.getName() );
    //    context.setAttributeValue( "sAMAccountName", user.getLdapId() );
    //context.setAttributeValue( "objectCategory", "person" );
    //    context.setAttributeValue( "memberof", "CN=Domain Users,CN=Users,DC=sambaad,DC=local" );
    //    context.setAttributeValue( "sn", user.getName() );

    // Note that the user object must be created before the password
    // can be set. Therefore as the user is created with no
    // password, userAccountControl must be set to the following
    // otherwise the Win2K3 password filter will return error 53
    // unwilling to perform.
    context.setAttributeValue( USER_ACCOUNT_CONTROL_ATTRIBUTE, ACCOUNT_CONTROL_POST_PASSWORD );

    ldapTemplate.bind( context );
  }

  @Override
  public void update( String currentName, User user )
  {
    Assert.hasText( currentName, "Current name must not be empty" );

    if ( !currentName.equals( user.getName() ) )
    {
      LdapName dn = buildDn( currentName );
      LdapName newDn = buildDn( user );

      ldapTemplate.rename( dn, newDn );
    }

    // TODO: update normal properties
    //    DirContextOperations operations = ldapTemplate.lookupContext( dn );
    //
    //    operations.setAttributeValue( this.idAttribute, user.getLdapId() );
    //    operations.setAttributeValue( this.loginAttribute, user.getLogin() );
    //    operations.setAttributeValue( this.nameAttribute, user.getName() );
    //    //    operations.setAttributeValue( "sn", user.getName() );
    //
    //    ldapTemplate.modifyAttributes( operations );
  }

  @Override
  public void delete( User user )
  {
    LdapName dn = buildDn( user );
    ldapTemplate.unbind( dn );
  }

  @Override
  public void changePassword( User user, String password )
  {
    Assert.hasLength( password, "Password must not be empty" );

    LdapName dn = buildDn( user );

    // Set password is a ldap modify operation
    // and we'll update the userAccountControl
    // enabling the account and forcing the password to never expire
    byte[] newUnicodePassword = toActiveDirectoryPasswordFormat( password );

    ModificationItem[] mods = new ModificationItem[] {
        new ModificationItem( DirContext.REPLACE_ATTRIBUTE,
            new BasicAttribute( this.passwordAttribute, newUnicodePassword ) ),
        new ModificationItem( DirContext.REPLACE_ATTRIBUTE,
            new BasicAttribute( USER_ACCOUNT_CONTROL_ATTRIBUTE, ACCOUNT_CONTROL_POST_PASSWORD ) ) };
    ldapTemplate.modifyAttributes( dn, mods );
  }

  /**
   * {@link AttributesMapper} which returns the list of all available Profile names, while
   * performing a data synchronization from the LDAP groups to the local aplication profiles.
   * That means profiles that exist in LDAP but not in the app will be created, the ones that
   * already exist will be updated, and the ones that don't exist in LDAP will be deleted.
   */
  private class UserSynchronizationAndLdapIdAttributesMapper extends SynchronizingAttributeMapper<User>
  {

    public UserSynchronizationAndLdapIdAttributesMapper( LocalDataProvider<User> provider, String nameAttribute )
    {
      super( provider, nameAttribute );
    }

    @Override
    protected void mapAttributesToElement( Attributes attrs, User user ) throws NamingException
    {
      mapAttributes( attrs, user );
    }

  }

  /**
   * {@link AttributesMapper} that maps LDAP user data to new {@link User} instances.
   */
  private class UserAttributesMapper extends ProvidedAttributeMapper<User>
  {

    public UserAttributesMapper( LocalDataProvider<User> provider, String nameAttribute )
    {
      super( provider, nameAttribute );
    }

    @Override
    protected void mapAttributesToElement( Attributes attrs, User user ) throws NamingException
    {
      mapAttributes( attrs, user );
    }

  }

  private LdapName buildDn( User user )
  {
    return buildDn( user.getName() );
  }

  private LdapName buildDn( String name )
  {
    return baseDnBuilder().add( this.nameAttribute, name ).build();
  }

  private LdapNameBuilder baseDnBuilder()
  {
    return LdapNameBuilder.newInstance( searchBase );
  }

  private <T> List<T> findAllWithMapper( AttributesMapper<T> mapper )
  {
    return ldapTemplate.search( searchBase, searchFilter, mapper );
  }

  private void mapAttributes( Attributes attrs, User user ) throws NamingException
  {
    user.setName( (String) attrs.get( nameAttribute ).get() );
    user.setBlocked( false );
    user.setNewRegistration( true );
  }

  private byte[] toActiveDirectoryPasswordFormat( String password )
  {
    //Password must be both Unicode little endian with quotes
    String newQuotedPassword = "\"" + password + "\"";
    try
    {
      return newQuotedPassword.getBytes( "UTF-16LE" );
    }
    catch ( UnsupportedEncodingException ex )
    {
      throw new RuntimeException( "Unable to convert password " + password + " to UTF-16LE enconding", ex );
    }
  }

}
