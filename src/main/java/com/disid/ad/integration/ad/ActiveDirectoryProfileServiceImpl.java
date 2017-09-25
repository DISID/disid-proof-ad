package com.disid.ad.integration.ad;

import com.disid.ad.config.ActiveDirectoryProperties.Context;
import com.disid.ad.model.Profile;
import com.disid.ad.model.User;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;

/**
 * {@link ActiveDirectoryService} implementation to manage LDAP entries related to the {@link Profile}
 * entity.
 */
@Transactional
public class ActiveDirectoryProfileServiceImpl implements ActiveDirectoryProfileService
{

  private static final String MEMBER = "member";

  private final LdapTemplate ldapTemplate;

  private final String nameAttribute = ProfileActiveDirectoryDefaults.NAME_ATTRIBUTE;
  private final String[] objectClassValues;
  private final String searchFilter;
  private final ProfileDnBuilder dnBuilder;
  private final UserDnBuilder userDnBuilder;

  /**
   * Creates a new service to manage profiles in the ActiveDirectory server
   * @param ldapTemplate to perform ActiveDirectory operations
   */
  public ActiveDirectoryProfileServiceImpl( LdapTemplate ldapTemplate )
  {
    this( ldapTemplate, ProfileActiveDirectoryDefaults.OBJECT_CLASSES, new ProfileDnBuilder( ProfileActiveDirectoryDefaults.SEARCH_BASE ),
        ProfileActiveDirectoryDefaults.SEARCH_FILTER, new UserDnBuilder( UserDefaults.SEARCH_BASE ) );
  }

  /**
   * Creates a new service to manage profiles in the ActiveDirectory server
   * @param ldapTemplate to perform ActiveDirectory operations
   * @param objectClassValues list of object classes to create Profile entries 
   * @param searchBase the search base to apply when looking for profiles relative to the 
   * {@link Context#getBaseDn()}. This relative base will be used also to create new groups into.
   * @param searchFilter filter to apply when looking for profiles.
   */
  public ActiveDirectoryProfileServiceImpl( LdapTemplate ldapTemplate, String[] objectClassValues,
      ProfileDnBuilder dnBuilder, String searchFilter, UserDnBuilder userDnBuilder )
  {
    this.ldapTemplate = ldapTemplate;
    this.objectClassValues = objectClassValues;
    this.searchFilter = searchFilter;
    this.dnBuilder = dnBuilder;
    this.userDnBuilder = userDnBuilder;
  }

  @Override
  public List<Profile> findAll( LocalDataProvider<Profile> provider )
  {
    return findAllWithMapper( new ProfileAttributesMapper( provider, this.nameAttribute ) );
  }

  @Override
  public List<String> synchronize( LocalDataProvider<Profile> provider )
  {
    List<String> names =
        findAllWithMapper( new ProfileSynchronizationAndLdapIdAttributesMapper( provider, nameAttribute ) );
    if ( names != null && !names.isEmpty() )
    {
      provider.deleteByNameNotIn( names );
    }
    return names;
  }

  @Override
  public void create( Profile profile )
  {
    LdapName dn = dnBuilder.getName( profile );
    DirContextAdapter context = new DirContextAdapter( dn );

    context.setAttributeValues( OBJECT_CLASS_ATTRIBUTE, objectClassValues );
    context.setAttributeValue( this.nameAttribute, profile.getName() );

    ldapTemplate.bind( context );
  }

  @Override
  public void update( String currentName, Profile profile )
  {
    Assert.hasText( currentName, "Current name must not be empty" );

    if ( !currentName.equals( profile.getName() ) )
    {
      LdapName dn = dnBuilder.getName( currentName );
      LdapName newDn = dnBuilder.getName( profile );

      ldapTemplate.rename( dn, newDn );
    }
  }

  @Override
  public void delete( Profile profile )
  {
    LdapName dn = dnBuilder.getName( profile );
    ldapTemplate.unbind( dn );
  }

  @Override
  public void addUsers( Profile profile, Iterable<User> users )
  {
    Name dn = dnBuilder.getName( profile );

    for ( User user : users )
    {
      Name userDn = userDnBuilder.getName( user );
      Name baseLdapName = ( (BaseLdapPathContextSource) ldapTemplate.getContextSource() ).getBaseLdapName();
      try
      {
        userDn = baseLdapName.addAll( userDn );
      }
      catch ( InvalidNameException e )
      {
        throw LdapUtils.convertLdapException( e );
      }
      DirContextOperations operations = ldapTemplate.lookupContext( dn );
      operations.addAttributeValue( MEMBER, userDn.toString() );

      ldapTemplate.modifyAttributes( operations );
    }
  }

  @Override
  public void removeUsers( Profile profile, Iterable<User> users )
  {
    Name groupDn = dnBuilder.getName( profile );

    for ( User user : users )
    {
      Name userDn = userDnBuilder.getName( user );
      Name baseLdapName = ( (BaseLdapPathContextSource) ldapTemplate.getContextSource() ).getBaseLdapName();
      try
      {
        userDn = baseLdapName.addAll( userDn );
      }
      catch ( InvalidNameException e )
      {
        throw LdapUtils.convertLdapException( e );
      }

      ldapTemplate.modifyAttributes( groupDn, new ModificationItem[] {
          new ModificationItem( DirContext.REMOVE_ATTRIBUTE, new BasicAttribute( MEMBER, userDn.toString() ) ) } );
    }
  }

  @Override
  public List<String> getUserNames( Profile profile )
  {
    return ldapTemplate.lookup( dnBuilder.getName( profile ), new AttributesMapper<List<String>>()
    {

      @Override
      public List<String> mapFromAttributes( Attributes attributes ) throws NamingException
      {

        List<String> userNames = new ArrayList<>();
        //        LdapUtils.collectAttributeValues( attributes, "member", userNames, String.class );

        Enumeration<?> values = attributes.get( MEMBER ).getAll();

        while ( values.hasMoreElements() )
        {
          Object value = values.nextElement();
          userNames.add( value.toString() );
        }

        return userNames;
      }

    } );
  }

  /**
   * {@link AttributesMapper} which returns the list of all available Profile names, while
   * performing a data synchronization from the LDAP groups to the local aplication profiles.
   * That means profiles that exist in LDAP but not in the app will be created, the ones that
   * already exist will be updated, and the ones that don't exist in LDAP will be deleted.
   */
  private class ProfileSynchronizationAndLdapIdAttributesMapper extends SynchronizingAttributeMapper<Profile>
  {

    public ProfileSynchronizationAndLdapIdAttributesMapper( LocalDataProvider<Profile> provider, String attributeName )
    {
      super( provider, attributeName );
    }

    @Override
    protected void mapAttributesToElement( Attributes attrs, Profile element ) throws NamingException
    {
      mapAttributes( attrs, element );
    }

  }

  /**
   * {@link AttributesMapper} that maps LDAP group data to new {@link Profile} instances.
   */
  private class ProfileAttributesMapper extends ProvidedAttributeMapper<Profile>
  {

    public ProfileAttributesMapper( LocalDataProvider<Profile> provider, String attributeName )
    {
      super( provider, attributeName );
    }

    @Override
    protected void mapAttributesToElement( Attributes attrs, Profile element ) throws NamingException
    {
      mapAttributes( attrs, element );
    }
  }

  private <T> List<T> findAllWithMapper( AttributesMapper<T> mapper )
  {
    return ldapTemplate.search( dnBuilder.getSearchBase(), searchFilter, mapper );
  }

  private void mapAttributes( Attributes attrs, Profile profile ) throws NamingException
  {
    profile.setName( (String) attrs.get( nameAttribute ).get() );
  }

}
