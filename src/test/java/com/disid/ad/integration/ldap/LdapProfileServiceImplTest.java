package com.disid.ad.integration.ldap;

import com.disid.ad.AbstractBaseIT;
import com.disid.ad.model.Profile;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;

import java.util.List;

public class LdapProfileServiceImplTest extends AbstractBaseIT
{
  @Autowired
  private LdapTemplate template;

  private LdapService<Profile> ldapProfileService;

  @Before
  public void setup()
  {
    ldapProfileService =
        new LdapProfileServiceImpl( template, "group", "cn", "name", new String[] { "top", "group" }, "cn=Users" );
  }

  @Test
  public void findAllShouldReturnAllProfiles()
  {
    List<Profile> profiles = ldapProfileService.findAll( new LocalDataProvider<Profile>()
    {

      @Override
      public Profile createByLdapId( String ldapId )
      {
        Profile profile = new Profile();
        profile.setLdapId( ldapId );
        return profile;
      }

      @Override
      public Profile findByLdapId( String ldapId )
      {
        return null;
      }

      @Override
      public void deleteByLdapIdNotIn( Iterable<String> ldapIds )
      {}

      @Override
      public void saveFromLdap( Profile value )
      {}
    } );

    int size = profiles.size();
    System.out.println( size );
  }

}
