package com.disid.ad.repository;
import com.disid.ad.model.Profile;

import org.springframework.roo.addon.layers.repository.jpa.annotations.RooJpaRepository;

/**
 * = ProfileRepository
 *
 * TODO Auto-generated class documentation
 *
 */
@RooJpaRepository(entity = Profile.class)
public interface ProfileRepository {

  void deleteByNameNotIn( Iterable<String> names );

  Profile findByName( String name );
}
