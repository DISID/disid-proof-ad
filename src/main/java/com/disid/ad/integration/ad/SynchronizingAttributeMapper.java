package com.disid.ad.integration.ad;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * Base {@link AttributesMapper} implementation to get the name of an element, as
 * well as performing the synchronization of data between the LDAP entries and the
 * values provided by the LocalDataProvider.
 *
 * @param <T> the entity type
 */
public abstract class SynchronizingAttributeMapper<T> implements AttributesMapper<String>
{

  private final LocalDataProvider<T> provider;
  private final String nameAttribute;

  /**
   * Creates a new mapper
   * @param provider to generate new entity instances
   * @param nameAttribute the ldap attribute with the common name
   */
  public SynchronizingAttributeMapper( LocalDataProvider<T> provider, String nameAttribute )
  {
    this.provider = provider;
    this.nameAttribute = nameAttribute;
  }

  public String mapFromAttributes( Attributes attrs ) throws NamingException
  {
    String name = getName( attrs );

    T element = getElement( name );

    mapAttributesToElement( attrs, element );

    // Store the changes in the local repository
    provider.saveFromLdap( element );

    return name;
  }

  /**
   * Returns the current provider
   * @return the provider of entity instances
   */
  public LocalDataProvider<T> getProvider()
  {
    return provider;
  }

  /**
   * Maps the LDAP attributes to the given element instance
   * @param attrs the LDAP attributes
   * @param element to map the attributes to
   * @throws NamingException if there is an error getting the attributes values
   */
  protected abstract void mapAttributesToElement( Attributes attrs, T element ) throws NamingException;

  private T getElement( String name )
  {
    // Find in the application database
    T element = provider.findByName( name );
    if ( element == null )
    {
      element = provider.createByName( name );
    }

    return element;
  }

  private String getName( Attributes attrs ) throws NamingException
  {
    return (String) attrs.get( nameAttribute ).get();
  }

}
