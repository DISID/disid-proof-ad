package com.disid.ad.integration.ldap;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import com.disid.ad.model.Profile;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.transaction.annotation.Transactional;

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
  private final LdapTemplate ldapTemplate;

  private final String mainObjectClass;
  private final String idAttribute;
  private final String nameAttribute;
  private final String[] objectClassValues;
  private final String searchBase;

  /**
   * Creates a new service to manage profiles in the LDAP server
   * @param ldapTemplate to perform LDAP operations
   * @param mainObjectClass of the profiles in the LDAP server
   * @param idAttribute attribute which identifies uniquely a profile from its sibling entries
   * @param nameAttribute the attribute to use as the profile's name
   * @param searchBase 
   */
  public LdapProfileServiceImpl( LdapTemplate ldapTemplate, String mainObjectClass, String idAttribute,
      String nameAttribute, String[] objectClassValues, String searchBase )
  {
    this.ldapTemplate = ldapTemplate;
    this.mainObjectClass = mainObjectClass;
    this.idAttribute = idAttribute;
    this.nameAttribute = nameAttribute;
    this.objectClassValues = objectClassValues;
    this.searchBase = searchBase;
  }

  @Override
  public List<Profile> findAll( LocalDataProvider<Profile> provider )
  {
    //    return ldapTemplate.search(
    //        query().base( searchBase ).where( OBJECT_CLASS_ATTRIBUTE ).is( mainObjectClass ).and( "isCriticalSystemObject" )
    //            .not().is( "TRUE" ),
    //        new ProfileAttributesMapper( provider ) );
    //    String filter = "&(objectClass=group)(!(isCriticalSystemObject=TRUE))";
    String filter = "(&(objectClass=group)(!(isCriticalSystemObject=TRUE)))";
    return ldapTemplate.search( searchBase, filter,
        new ProfileAttributesMapper( provider ) );
  }

  @Override
  public List<String> synchronize( LocalDataProvider<Profile> provider )
  {
    List<String> ldapIds = ldapTemplate.search( query().where( OBJECT_CLASS_ATTRIBUTE ).is( mainObjectClass ),
        new ProfileSynchronizationAndLdapIdAttributesMapper( provider ) );
    if ( ldapIds != null && !ldapIds.isEmpty() )
    {
      provider.deleteByLdapIdNotIn( ldapIds );
    }
    return ldapIds;
  }

  @Override
  public void create( Profile profile )
  {
    LdapName dn = buildDn( profile );
    DirContextAdapter context = new DirContextAdapter( dn );

    context.setAttributeValues( OBJECT_CLASS_ATTRIBUTE, objectClassValues );
    context.setAttributeValue( this.idAttribute, profile.getLdapId() );
    context.setAttributeValue( this.nameAttribute, profile.getName() );

    ldapTemplate.bind( context );
  }

  @Override
  public void update( Profile profile )
  {
    throw new UnsupportedOperationException( "Not implemented" );
  }

  @Override
  public void delete( Profile profile )
  {
    throw new UnsupportedOperationException( "Not implemented" );
  }

  private LdapName buildDn( Profile profile )
  {
    return baseDnBuilder().add( this.idAttribute, profile.getLdapId() ).build();
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
      String ldapId = getLdapId( attrs );

      // Find in the application database
      Profile profile = provider.findByLdapId( ldapId );
      if ( profile == null )
      {
        profile = provider.createByLdapId( ldapId );
      }

      mapAttributes( attrs, profile );

      // Store the changes in the local repository
      provider.saveFromLdap( profile );

      return ldapId;
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
      String ldapId = getLdapId( attrs );

      Profile profile = provider.createByLdapId( ldapId );

      mapAttributes( attrs, profile );

      return profile;
    }
  }

  private void mapAttributes( Attributes attrs, Profile profile ) throws NamingException
  {
    profile.setName( (String) attrs.get( nameAttribute ).get() );
  }

  private String getLdapId( Attributes attrs ) throws NamingException
  {
    String ldapId = (String) attrs.get( idAttribute ).get();
    return ldapId;
  }

}
