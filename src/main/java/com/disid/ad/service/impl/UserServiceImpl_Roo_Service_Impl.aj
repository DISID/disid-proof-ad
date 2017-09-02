// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.disid.ad.service.impl;

import com.disid.ad.model.Profile;
import com.disid.ad.model.User;
import com.disid.ad.repository.UserRepository;
import com.disid.ad.service.impl.UserServiceImpl;
import io.springlets.data.domain.GlobalSearch;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

privileged aspect UserServiceImpl_Roo_Service_Impl {
    
    declare @type: UserServiceImpl: @Service;
    
    declare @type: UserServiceImpl: @Transactional(readOnly = true);
    
    /**
     * TODO Auto-generated attribute documentation
     * 
     */
    private UserRepository UserServiceImpl.userRepository;
    
    /**
     * TODO Auto-generated constructor documentation
     * 
     * @param userRepository
     */
    @Autowired
    public UserServiceImpl.new(UserRepository userRepository) {
        setUserRepository(userRepository);
    }

    /**
     * TODO Auto-generated method documentation
     * 
     * @return UserRepository
     */
    public UserRepository UserServiceImpl.getUserRepository() {
        return userRepository;
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param userRepository
     */
    public void UserServiceImpl.setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param user
     */
    @Transactional
    public void UserServiceImpl.delete(User user) {
        // Clear bidirectional many-to-many child relationship with Profile
        for (Profile item : user.getProfiles()) {
            item.getUsers().remove(user);
        }
        
        getUserRepository().delete(user);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param entities
     * @return List
     */
    @Transactional
    public List<User> UserServiceImpl.save(Iterable<User> entities) {
        return getUserRepository().save(entities);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param ids
     */
    @Transactional
    public void UserServiceImpl.delete(Iterable<Long> ids) {
        List<User> toDelete = getUserRepository().findAll(ids);
        getUserRepository().deleteInBatch(toDelete);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param entity
     * @return User
     */
    @Transactional
    public User UserServiceImpl.save(User entity) {
        return getUserRepository().save(entity);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param id
     * @return User
     */
    public User UserServiceImpl.findOne(Long id) {
        return getUserRepository().findOne(id);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param id
     * @return User
     */
    public User UserServiceImpl.findOneForUpdate(Long id) {
        return getUserRepository().findOneDetached(id);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param ids
     * @return List
     */
    public List<User> UserServiceImpl.findAll(Iterable<Long> ids) {
        return getUserRepository().findAll(ids);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @return List
     */
    public List<User> UserServiceImpl.findAll() {
        return getUserRepository().findAll();
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @return Long
     */
    public long UserServiceImpl.count() {
        return getUserRepository().count();
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param globalSearch
     * @param pageable
     * @return Page
     */
    public Page<User> UserServiceImpl.findAll(GlobalSearch globalSearch, Pageable pageable) {
        return getUserRepository().findAll(globalSearch, pageable);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param ids
     * @param globalSearch
     * @param pageable
     * @return Page
     */
    public Page<User> UserServiceImpl.findAllByIdsIn(List<Long> ids, GlobalSearch globalSearch, Pageable pageable) {
        return getUserRepository().findAllByIdsIn(ids, globalSearch, pageable);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param profiles
     * @param globalSearch
     * @param pageable
     * @return Page
     */
    public Page<User> UserServiceImpl.findByProfilesContains(Profile profiles, GlobalSearch globalSearch, Pageable pageable) {
        return getUserRepository().findByProfilesContains(profiles, globalSearch, pageable);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @param profiles
     * @return Long
     */
    public long UserServiceImpl.countByProfilesContains(Profile profiles) {
        return getUserRepository().countByProfilesContains(profiles);
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @return Class
     */
    public Class<User> UserServiceImpl.getEntityType() {
        return User.class;
    }
    
    /**
     * TODO Auto-generated method documentation
     * 
     * @return Class
     */
    public Class<Long> UserServiceImpl.getIdType() {
        return Long.class;
    }
    
}