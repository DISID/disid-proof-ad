package com.disid.ad.service.impl;

import com.disid.ad.integration.ldap.LocalDataProvider;
import com.disid.ad.model.Profile;
import com.disid.ad.service.api.ProfileService;

import org.springframework.roo.addon.layers.service.annotations.RooServiceImpl;

/**
 * = ProfileServiceImpl
 *
 * TODO Auto-generated class documentation
 *
 */
@RooServiceImpl(service = ProfileService.class)
public class ProfileServiceImpl implements ProfileService, LocalDataProvider<Profile>
{

  @Override
  public Profile createByName( String name )
  {
    Profile profile = new Profile();
    profile.setName( name );
    return profile;
  }

  @Override
  public Profile findByName( String name )
  {
    return getProfileRepository().findByName( name );
  }

  @Override
  public void saveFromLdap( Profile value )
  {
    save( value );
  }

  @Override
  public void deleteByNameNotIn( Iterable<String> names )
  {
    getProfileRepository().deleteByNameNotIn( names );
  }
}
