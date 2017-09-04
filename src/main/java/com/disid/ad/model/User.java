package com.disid.ad.model;

import org.springframework.roo.addon.javabean.annotations.RooEquals;
import org.springframework.roo.addon.javabean.annotations.RooJavaBean;
import org.springframework.roo.addon.javabean.annotations.RooToString;
import org.springframework.roo.addon.jpa.annotations.entity.RooJpaEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

/**
 * = User
 *
 * TODO Auto-generated class documentation
 *
 */
@RooJavaBean
@RooToString
@RooJpaEntity( table = "LOCAL_USER" )
@RooEquals( isJpaEntity = true )
public class User
{

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  @Id
  @GeneratedValue( strategy = GenerationType.AUTO )
  private Long id;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  @Version
  private Integer version;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  @NotNull
  @Column( unique = true )
  private String name;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  @NotNull
  @Column( unique = true )
  private String login;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  private Boolean blocked;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  private Boolean newRegistration;

  /**
   * TODO Auto-generated attribute documentation
   *
   */
  @ManyToMany( fetch = FetchType.LAZY )
  private Set<Profile> profiles = new HashSet<Profile>();

}
