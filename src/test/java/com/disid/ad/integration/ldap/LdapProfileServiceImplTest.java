package com.disid.ad.integration.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import com.disid.ad.AbstractBaseIT;
import com.disid.ad.model.Profile;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class LdapProfileServiceImplTest extends AbstractBaseIT
{
  @Autowired
  private LocalDataProvider<Profile> profileProvider;

  @Autowired
  private LdapService<Profile> ldapProfileService;

  @Test
  public void findAllShouldReturnAllProfiles()
  {
    List<Profile> profiles = ldapProfileService.findAll( profileProvider );

    assertThat( profiles ).isNotEmpty().extracting( "name" ).contains( "Administrators", "Police dispatchers",
        "Police call talkers" );
  }

  @Test
  public void createAddsProfileToLdapDeleteRemovesProfileFromLdap()
  {

    List<Profile> profilesInitial = ldapProfileService.findAll( profileProvider );

    String name = "Create and Delete group test";
    Profile profile = profileProvider.createByName( name );

    ldapProfileService.create( profile );

    List<Profile> profilesAfterCreate = ldapProfileService.findAll( profileProvider );

    assertThat( profilesAfterCreate ).size().isEqualTo( profilesInitial.size() + 1 );
    assertThat( profilesAfterCreate ).extracting( "name" ).contains( name );

    ldapProfileService.delete( profile );

    List<Profile> profilesAfterDelete = ldapProfileService.findAll( profileProvider );

    assertThat( profilesAfterDelete ).size().isEqualTo( profilesAfterCreate.size() - 1 );
    assertThat( profilesAfterDelete ).extracting( "name" ).doesNotContain( name );
  }

  @Test
  public void updateChangesProfileAttributeInLdap()
  {
    List<Profile> profiles = ldapProfileService.findAll( profileProvider );

    Profile profile = profiles.get( 0 );
    String oldName = profile.getName();
    String name = oldName + " UPDATED";
    profile.setName( name );

    ldapProfileService.update( oldName, profile );

    List<Profile> profilesAfter = ldapProfileService.findAll( profileProvider );
    assertThat( profilesAfter ).hasSameSizeAs( profiles ).extracting( "name" ).contains( name );
  }
}
