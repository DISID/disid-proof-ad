package com.disid.ad.integration.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import com.disid.ad.AbstractBaseIT;
import com.disid.ad.model.User;
import com.disid.ad.service.api.UserService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class LdapUserServiceImplTest extends AbstractBaseIT
{
  private static final String NAME_PROPERTY = "name";
  private static final Object[] TEST_DATA_USER_NAMES =
      new Object[] { "Call take supervisor", "Ken Ryan", "Summer Nine" };

  @Autowired
  private LocalDataProvider<User> userProvider;

  @Autowired
  private UserService service;

  @Autowired
  private LdapService<User> ldapUserService;

  @Test
  public void findAllShouldReturnAllUsers()
  {
    List<User> profiles = ldapUserService.findAll( userProvider );

    assertThat( profiles ).isNotEmpty().extracting( NAME_PROPERTY ).contains( TEST_DATA_USER_NAMES );
  }

  @Test
  public void createAddsUserToLdapDeleteRemovesUserFromLdap()
  {

    List<User> usersInitial = ldapUserService.findAll( userProvider );

    String name = "Create and Delete user test";
    User profile = userProvider.createByName( name );

    ldapUserService.create( profile );

    List<User> usersAfterCreate = ldapUserService.findAll( userProvider );

    assertThat( usersAfterCreate ).size().isEqualTo( usersInitial.size() + 1 );
    assertThat( usersAfterCreate ).extracting( NAME_PROPERTY ).contains( name );

    ldapUserService.delete( profile );

    List<User> usersAfterDelete = ldapUserService.findAll( userProvider );

    assertThat( usersAfterDelete ).size().isEqualTo( usersAfterCreate.size() - 1 );
    assertThat( usersAfterDelete ).extracting( NAME_PROPERTY ).doesNotContain( name );
  }

  @Test
  public void updateChangesUserAttributeInLdap()
  {
    List<User> users = ldapUserService.findAll( userProvider );

    User user = users.get( 0 );
    String oldName = user.getName();
    String name = oldName + " UPDATED";
    user.setName( name );

    // Change the name
    ldapUserService.update( oldName, user );

    List<User> usersAfter = ldapUserService.findAll( userProvider );
    assertThat( usersAfter ).hasSameSizeAs( users ).extracting( NAME_PROPERTY ).contains( name );

    // Return to the original name
    user.setName( oldName );

    ldapUserService.update( name, user );

    usersAfter = ldapUserService.findAll( userProvider );
    assertThat( usersAfter ).hasSameSizeAs( users ).extracting( NAME_PROPERTY ).contains( oldName );
  }

  @Test
  public void synchronizeUpdatesAllUsers()
  {

    List<String> names = ldapUserService.synchronize( userProvider );

    List<User> dbUsers = service.findAll();

    assertThat( dbUsers ).extracting( NAME_PROPERTY ).containsExactlyElementsOf( names );

  }

}
