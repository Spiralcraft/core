//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.persist;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.registry.Registrant;

import java.net.URI;

/**
 * A reference to a persistent object- one with a lifetime
 *   longer than than that its in-vm references. Using the Registry, internal
 *   functionality can obtain a reference to this interface to request
 *   a 'save' or 'save as'.
 *
 *XXX This interface is incomplete
 */
public interface PersistentReference<T>
  extends Registrant
{
  /**
   * Save the state of this reference to the backing store
   */
  public void save()
    throws PersistenceException;
  
  /**
   * Change the URI for the backing store
   */
  public void setResourceUri(URI resourceUri)
    throws PersistenceException;
  
  
  /**
   * Return the Object being referred to
   */
  public T get();
  
  public void set(T object);
  
  /**
   * Bind this reference into the local hierarchical context
   * 
   * @param parent
   * @return
   */
  public Channel<T> bind(Focus<?> parent)
    throws BindException;
}
