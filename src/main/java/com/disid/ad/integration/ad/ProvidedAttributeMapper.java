package com.disid.ad.integration.ad;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * Base {@link AttributesMapper} implementation to map LDAP attributes to an entity.
 *
 * @param <T> the entity type
 */
public abstract class ProvidedAttributeMapper<T> implements AttributesMapper<T>
{

  private final LocalDataProvider<T> provider;
  private final String nameAttribute;

  /**
   * Creates a new mapper
   * @param provider to generate new entity instances
   * @param nameAttribute the ldap attribute with the common name
   */
  public ProvidedAttributeMapper( LocalDataProvider<T> provider, String nameAttribute )
  {
    this.provider = provider;
    this.nameAttribute = nameAttribute;
  }

  public T mapFromAttributes( Attributes attrs ) throws NamingException
  {
    String name = getName( attrs );

    T element = getElement( name );

    mapAttributesToElement( attrs, element );

    return element;
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
    return provider.createByName( name );
  }

  private String getName( Attributes attrs ) throws NamingException
  {
    return (String) attrs.get( nameAttribute ).get();
  }
}
