package com.disid.ad.repository;

import io.springlets.data.jpa.repository.support.QueryDslRepositorySupportExt;
import org.springframework.roo.addon.layers.repository.jpa.annotations.RooJpaRepositoryCustomImpl;
import com.disid.ad.model.Profile;

/**
 * = ProfileRepositoryImpl
 *
 * TODO Auto-generated class documentation
 *
 */ 
@RooJpaRepositoryCustomImpl(repository = ProfileRepositoryCustom.class)
public class ProfileRepositoryImpl extends QueryDslRepositorySupportExt<Profile> {

    /**
     * TODO Auto-generated constructor documentation
     */
    ProfileRepositoryImpl() {
        super(Profile.class);
    }
}