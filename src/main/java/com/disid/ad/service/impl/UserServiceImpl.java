package com.disid.ad.service.impl;

import com.disid.ad.integration.ad.LocalDataProvider;
import com.disid.ad.model.User;
import com.disid.ad.service.api.UserService;

import org.springframework.roo.addon.layers.service.annotations.RooServiceImpl;

/**
 * = UserServiceImpl
 *
 * TODO Auto-generated class documentation
 *
 */
@RooServiceImpl(service = UserService.class)
public class UserServiceImpl implements UserService, LocalDataProvider<User>
{

  @Override
  public User createByName( String name )
  {
    User user = new User();
    user.setName( name );
    return user;
  }

  @Override
  public User findByName( String name )
  {
    return getUserRepository().findByName( name );
  }

  @Override
  public void saveFromLdap( User value )
  {
    save( value );
  }

  @Override
  public void deleteByNameNotIn( Iterable<String> names )
  {
    getUserRepository().deleteByNameNotIn( names );
  }
}
