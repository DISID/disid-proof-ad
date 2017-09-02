// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.disid.ad.repository;

import com.disid.ad.model.Profile;
import com.disid.ad.model.User;
import com.disid.ad.repository.UserRepository;
import com.disid.ad.repository.UserRepositoryCustom;
import io.springlets.data.jpa.repository.DetachableJpaRepository;
import org.springframework.transaction.annotation.Transactional;

privileged aspect UserRepository_Roo_Jpa_Repository {
    
    declare parents: UserRepository extends DetachableJpaRepository<User, Long>;
    
    declare parents: UserRepository extends UserRepositoryCustom;
    
    declare @type: UserRepository: @Transactional(readOnly = true);
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param profiles
     * @return Long
     */
    public abstract long UserRepository.countByProfilesContains(Profile profiles);
    
}