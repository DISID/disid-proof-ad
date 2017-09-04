package com.disid.ad.integration.ad;

import com.disid.ad.config.ActiveDirectoryProperties.Context;
import com.disid.ad.model.Profile;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

/**
 * {@link ActiveDirectoryService} implementation to manage LDAP entries related to the {@link Profile}
 * entity.
 */
@Transactional
public class ActiveDirectoryProfileServiceImpl implements ActiveDirectoryProfileService
{

  private final LdapTemplate ldapTemplate;

  private final String nameAttribute = DEFAULT_NAME_ATTRIBUTE;
  private final String[] objectClassValues;
  private final String searchBase;
  private final String searchFilter;

  /**
   * Creates a new service to manage profiles in the ActiveDirectory server
   * @param ldapTemplate to perform ActiveDirectory operations
   */
  public ActiveDirectoryProfileServiceImpl( LdapTemplate ldapTemplate )
  {
    this( ldapTemplate, DEFAULT_OBJECT_CLASSES, DEFAULT_SEARCH_BASE, DEFAULT_SEARCH_FILTER );
  }

  /**
   * Creates a new service to manage profiles in the ActiveDirectory server
   * @param ldapTemplate to perform ActiveDirectory operations
   * @param objectClassValues list of object classes to create Profile entries 
   * @param searchBase the search base to apply when looking for profiles relative to the 
   * {@link Context#getBaseDn()}. This relative base will be used also to create new groups into.
   * @param searchFilter filter to apply when looking for profiles.
   */
  public ActiveDirectoryProfileServiceImpl( LdapTemplate ldapTemplate, String[] objectClassValues, String searchBase,
      String searchFilter )
  {
    this.ldapTemplate = ldapTemplate;
    this.objectClassValues = objectClassValues;
    this.searchBase = searchBase;
    this.searchFilter = searchFilter;
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
    LdapName dn = buildDn( profile );
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
      LdapName dn = buildDn( currentName );
      LdapName newDn = buildDn( profile );

      ldapTemplate.rename( dn, newDn );
    }
  }

  @Override
  public void delete( Profile profile )
  {
    LdapName dn = buildDn( profile );
    ldapTemplate.unbind( dn );
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

  private LdapName buildDn( Profile profile )
  {
    return buildDn( profile.getName() );
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

  private void mapAttributes( Attributes attrs, Profile profile ) throws NamingException
  {
    profile.setName( (String) attrs.get( nameAttribute ).get() );
  }

}
