//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.lang;

import spiralcraft.lang.spi.Binding;

import java.net.URI;

/**
 * <P>A Reflector is a "type broker" which exposes parts of an object model 
 *   by creating data pipes (Bindings) based on elements of Expression syntax
 *   as it applies to the underlying typing model.
 *   
 * <P>Given a data source and a Focus, a Reflector will resolve a name and a set of
 *   modifiers into another data source (Binding) bound to the first and to the
 *   Focus, in order to effect some transformation or computation.
 */
public interface Reflector<T>
{

  /**
   * Generate a new Binding which resolves the name and the given parameter 
   *   expressions against the source Binding and the supplied Focus.
   */
  public <X> Binding<X> resolve
    (Binding<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException;

  /**
   * Decorate the specified Binding with a decorator that implements the
   *   specified interface
   */
  public <D extends Decorator<T>> D decorate
    (Binding<? extends T> source,Class<D> decoratorInterface)
    throws BindException;
  
  /**
   * Return the Java class of the data object accessible through Channels 
   *   associated with this Reflector
   */
  public Class<T> getContentType();
  
  /**
   * @return The URI that identifies the specific type of the data objects
   *   described by this Reflector. The URI is defined by the type system
   *   that provides the Reflector implementation.
   */
  public URI getTypeURI();
  
  /**
   * @return Whether the data object described by this Reflector can be
   *   assigned to the type identified by the typeURI. If typeURI==getTypeURI()
   *   then this method must return true. Otherwise, the type compatibility
   *   is defined by the type system that provides the Reflector 
   *   implementation.
   */
  public boolean isAssignableTo(URI typeURI);
}
