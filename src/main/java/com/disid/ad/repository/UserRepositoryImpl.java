package com.disid.ad.repository;

import io.springlets.data.jpa.repository.support.QueryDslRepositorySupportExt;
import org.springframework.roo.addon.layers.repository.jpa.annotations.RooJpaRepositoryCustomImpl;
import com.disid.ad.model.User;

/**
 * = UserRepositoryImpl
 *
 * TODO Auto-generated class documentation
 *
 */ 
@RooJpaRepositoryCustomImpl(repository = UserRepositoryCustom.class)
public class UserRepositoryImpl extends QueryDslRepositorySupportExt<User> {

    /**
     * TODO Auto-generated constructor documentation
     */
    UserRepositoryImpl() {
        super(User.class);
    }
}