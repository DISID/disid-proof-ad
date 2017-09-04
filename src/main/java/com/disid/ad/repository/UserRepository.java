package com.disid.ad.repository;
import com.disid.ad.model.User;

import org.springframework.roo.addon.layers.repository.jpa.annotations.RooJpaRepository;

/**
 * = UserRepository
 *
 * TODO Auto-generated class documentation
 *
 */
@RooJpaRepository(entity = User.class)
public interface UserRepository {

  User findByName( String name );

  void deleteByNameNotIn( Iterable<String> names );
}
