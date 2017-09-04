package com.disid.ad.integration.ad;

import com.disid.ad.config.ActiveDirectoryProperties.Context;
import com.disid.ad.model.User;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
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
 * {@link ActiveDirectoryService} implementation to manage LDAP entries related to the {@link User}
 * entity.
 */
@Transactional
public class ActiveDirectoryUserServiceImpl implements ActiveDirectoryUserService
{
  private static final String USER_ACCOUNT_CONTROL_ATTRIBUTE = "userAccountControl";

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
  public ActiveDirectoryUserServiceImpl( LdapTemplate ldapTemplate )
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
  public ActiveDirectoryUserServiceImpl( LdapTemplate ldapTemplate, String loginAttribute, String passwordAttribute,
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
    context.setAttributeValue( this.loginAttribute, user.getLogin() );

    // Note that the user object must be created before the password
    // can be set. Therefore as the user is created with no
    // password, userAccountControl must be set to the following
    // otherwise the Win2K3 password filter will return error 53
    // unwilling to perform.
    context.setAttributeValue( USER_ACCOUNT_CONTROL_ATTRIBUTE, ACCOUNT_CONTROL_PRE_PASSWORD );

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

    LdapName dn = buildDn( user );
    DirContextOperations operations = ldapTemplate.lookupContext( dn );

    operations.setAttributeValue( this.loginAttribute, user.getLogin() );

    ldapTemplate.modifyAttributes( operations );
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
    user.setLogin( (String) attrs.get( loginAttribute ).get() );
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
