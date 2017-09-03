package com.disid.ad.integration.ldap;

/**
 * Manages local data for a {@link LdapService}. 
 *
 * @param <T> the type of local entity
 */
public interface LocalDataProvider<T>
{
  /**
   * Returns and new instance of the entity with the given LDAP common name
   * @param name the common name that relates the local entity with an LDAP entry
   * @return the new entity
   */
  T createByName( String name );

  /**
   * Returns and existing instance of the entity with the given LDAP common name.
   * @param name the common name that relates the local entity with an LDAP entry
   * @return the local entity or null
   */
  T findByName( String name );

  /**
   * Stores a new or updated entity with the values that come from a related LDAP entry.
   * @param value the entity to store in the local application repository
   */
  void saveFromLdap( T value );

  /**
   * Deletes all the local entities whose LDAP related common name is not included in the list.
   * This is usually used to remove local entities whose related LDAP entry no longer exists
   * @param names the list of LDAP related names of entities not to delete
   */
  void deleteByNameNotIn( Iterable<String> names );
}
