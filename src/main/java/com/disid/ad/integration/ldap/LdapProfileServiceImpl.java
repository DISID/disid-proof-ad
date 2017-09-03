package com.disid.ad.integration.ldap;

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
 * {@link LdapService} implementation to manage LDAP entries related to the {@link Profile}
 * entity.
 */
@Transactional
public class LdapProfileServiceImpl implements LdapService<Profile>
{
  public static final String DEFAULT_NAME_ATTRIBUTE = "cn";
  public static final String DEFAULT_SEARCH_BASE = "cn=Users";
  public static final String DEFAULT_SEARCH_FILTER = "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
  public static final String[] DEFAULT_OBJECT_CLASSES = new String[] { "top", "group" };

  private final LdapTemplate ldapTemplate;

  private final String nameAttribute = DEFAULT_NAME_ATTRIBUTE;
  private final String[] objectClassValues;
  private final String searchBase;
  private final String searchFilter;

  /**
   * Creates a new service to manage profiles in the LDAP server
   * @param ldapTemplate to perform LDAP operations
   * @param mainObjectClass of the profiles in the LDAP server
   * @param idAttribute attribute which identifies uniquely a profile from its sibling entries
   * @param nameAttribute the attribute to use as the profile's name
   * @param searchBase 
   */
  public LdapProfileServiceImpl( LdapTemplate ldapTemplate )
  {
    this( ldapTemplate, DEFAULT_OBJECT_CLASSES, DEFAULT_SEARCH_BASE, DEFAULT_SEARCH_FILTER );
  }

  /**
   * Creates a new service to manage profiles in the LDAP server
   * @param ldapTemplate to perform LDAP operations
   * @param mainObjectClass of the profiles in the LDAP server
   * @param idAttribute attribute which identifies uniquely a profile from its sibling entries
   * @param nameAttribute the attribute to use as the profile's name
   * @param searchBase 
   */
  public LdapProfileServiceImpl( LdapTemplate ldapTemplate, String[] objectClassValues, String searchBase,
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
    return findAllWithMapper( new ProfileAttributesMapper( provider ) );
  }

  @Override
  public List<String> synchronize( LocalDataProvider<Profile> provider )
  {
    List<String> ldapIds = findAllWithMapper( new ProfileSynchronizationAndLdapIdAttributesMapper( provider ) );
    if ( ldapIds != null && !ldapIds.isEmpty() )
    {
      provider.deleteByNameNotIn( ldapIds );
    }
    return ldapIds;
  }

  private <T> List<T> findAllWithMapper( AttributesMapper<T> mapper )
  {
    return ldapTemplate.search( searchBase, searchFilter, mapper );
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

  private class ProfileSynchronizationAndLdapIdAttributesMapper implements AttributesMapper<String>
  {

    private final LocalDataProvider<Profile> provider;

    public ProfileSynchronizationAndLdapIdAttributesMapper( LocalDataProvider<Profile> provider )
    {
      this.provider = provider;
    }

    public String mapFromAttributes( Attributes attrs ) throws NamingException
    {
      String name = getName( attrs );

      // Find in the application database
      Profile profile = provider.findByName( name );
      if ( profile == null )
      {
        profile = provider.createByName( name );
      }

      mapAttributes( attrs, profile );

      // Store the changes in the local repository
      provider.saveFromLdap( profile );

      return name;
    }
  }

  private class ProfileAttributesMapper implements AttributesMapper<Profile>
  {

    private final LocalDataProvider<Profile> provider;

    public ProfileAttributesMapper( LocalDataProvider<Profile> provider )
    {
      this.provider = provider;
    }

    public Profile mapFromAttributes( Attributes attrs ) throws NamingException
    {
      String name = getName( attrs );

      Profile profile = provider.createByName( name );

      mapAttributes( attrs, profile );

      return profile;
    }
  }

  private void mapAttributes( Attributes attrs, Profile profile ) throws NamingException
  {
    profile.setName( (String) attrs.get( nameAttribute ).get() );
  }

  private String getName( Attributes attrs ) throws NamingException
  {
    return (String) attrs.get( nameAttribute ).get();
  }

}
