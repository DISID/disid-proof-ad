package com.disid.ad.model;

import org.springframework.roo.addon.javabean.annotations.RooEquals;
import org.springframework.roo.addon.javabean.annotations.RooJavaBean;
import org.springframework.roo.addon.javabean.annotations.RooToString;
import org.springframework.roo.addon.jpa.annotations.entity.JpaRelationType;
import org.springframework.roo.addon.jpa.annotations.entity.RooJpaEntity;
import org.springframework.roo.addon.jpa.annotations.entity.RooJpaRelation;

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
 * = Profile
 *
 * TODO Auto-generated class documentation
 *
 */
@RooJavaBean
@RooToString
@RooJpaEntity
@RooEquals( isJpaEntity = true )
public class Profile
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
  @ManyToMany( cascade = { javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST },
      fetch = FetchType.LAZY, mappedBy = "profiles" )
  @RooJpaRelation( type = JpaRelationType.AGGREGATION )
  private Set<User> users = new HashSet<User>();

  protected Profile()
  {
    // Default empty constructor needed by JPA.
  }

  public Profile( String name )
  {
    this.name = name;
  }
}
