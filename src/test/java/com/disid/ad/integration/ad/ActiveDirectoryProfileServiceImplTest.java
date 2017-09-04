package com.disid.ad.integration.ad;

import static org.assertj.core.api.Assertions.assertThat;

import com.disid.ad.AbstractBaseIT;
import com.disid.ad.model.Profile;
import com.disid.ad.model.User;
import com.disid.ad.service.api.ProfileService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

public class ActiveDirectoryProfileServiceImplTest extends AbstractBaseIT
{
  private static final String NAME_PROPERTY = "name";
  private static final Object[] TEST_DATA_PROFILE_NAMES =
      new Object[] { "Administrators", "Police dispatchers", "Police call talkers" };

  @Autowired
  private LocalDataProvider<Profile> profileProvider;

  @Autowired
  private LocalDataProvider<User> userProvider;

  @Autowired
  private ProfileService service;

  @Autowired
  private ActiveDirectoryProfileService ldapProfileService;

  @Autowired
  private ActiveDirectoryService<User> ldapUserService;

  @Test
  public void findAllShouldReturnAllProfiles()
  {
    List<Profile> profiles = ldapProfileService.findAll( profileProvider );

    assertThat( profiles ).isNotEmpty().extracting( NAME_PROPERTY ).contains( TEST_DATA_PROFILE_NAMES );
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
    assertThat( profilesAfterCreate ).extracting( NAME_PROPERTY ).contains( name );

    ldapProfileService.delete( profile );

    List<Profile> profilesAfterDelete = ldapProfileService.findAll( profileProvider );

    assertThat( profilesAfterDelete ).size().isEqualTo( profilesAfterCreate.size() - 1 );
    assertThat( profilesAfterDelete ).extracting( NAME_PROPERTY ).doesNotContain( name );
  }

  @Test
  public void updateChangesProfileAttributeInLdap()
  {
    List<Profile> profiles = ldapProfileService.findAll( profileProvider );

    Profile profile = profiles.get( 0 );
    String oldName = profile.getName();
    String name = oldName + " UPDATED";
    profile.setName( name );

    // Change the name
    ldapProfileService.update( oldName, profile );

    List<Profile> profilesAfter = ldapProfileService.findAll( profileProvider );
    assertThat( profilesAfter ).hasSameSizeAs( profiles ).extracting( NAME_PROPERTY ).contains( name );

    // Return to the original name
    profile.setName( oldName );

    ldapProfileService.update( name, profile );

    profilesAfter = ldapProfileService.findAll( profileProvider );
    assertThat( profilesAfter ).hasSameSizeAs( profiles ).extracting( NAME_PROPERTY ).contains( oldName );
  }

  @Test
  public void synchronizeUpdatesAllProfiles()
  {

    List<String> names = ldapProfileService.synchronize( profileProvider );

    List<Profile> dbProfiles = service.findAll();

    assertThat( dbProfiles ).extracting( NAME_PROPERTY ).containsExactlyElementsOf( names );

  }

  @Test
  public void addDeleteUsersUpdatesProfilesUsers()
  {
    List<Profile> profiles = ldapProfileService.findAll( profileProvider );
    Profile firstProfile = profiles.get( 0 );

    List<User> users = ldapUserService.findAll( userProvider );

    ldapProfileService.addUsers( firstProfile, users );

    List<String> userNames = ldapProfileService.getUserNames( firstProfile );
    
    assertThat( userNames ).hasSameSizeAs( users );
    
    ldapProfileService.removeUsers( firstProfile, Collections.singleton( users.get( 0 ) ) );
    
    List<String> userNamesAfterRemove = ldapProfileService.getUserNames( firstProfile );
    
    assertThat( userNamesAfterRemove ).size().isEqualTo( userNames.size() - 1 );
    
  }
}
