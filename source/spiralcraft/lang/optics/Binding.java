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
package spiralcraft.lang.optics;

import java.beans.PropertyChangeSupport;

import spiralcraft.lang.Optic;

/**
 * A unit of transformationStandard internal basis for implementation of the Optic interface, which
 *   requires a local cache to eliminate redundant object creation.
 *
 * To summarize, a Binding provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container.
 */
public interface Binding
  extends Optic
{
  /**
   * Return the bound object
   */
  public Object get();

  /**
   * Update the bound object.
   *@return Whether the modification was successful or not.
   */
  public boolean set(Object value);

  /**
   * The Prism associated with the bound Object, against which
   *   
   *   
   */
  public Prism getPrism();

  /** 
   * Indicates whether the bound object is guaranteed to
   *   remain unchanged.
   */
  boolean isStatic();

  /**
   * Provide a reference to the PropertyChangeSupport object
   *   which fires a PropertyChangeEvent when the bound object changes.
   *   Returns null if the binding does not support
   *   property change notification, or is guaranteed to never change..
   */
  PropertyChangeSupport propertyChangeSupport();

  /**
   * Return the cache which hold bindings which derive their value from
   *   this binding.
   */
  WeakBindingCache getCache();
  
}
