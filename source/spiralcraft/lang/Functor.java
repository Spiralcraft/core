//
// Copyright (c) 1998,2008 Michael Toth
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

/**
 * <p>Implemented by components that integrate parameterized behavior into a 
 *   data stream, as a means to integrate and extend functionality.
 *   
 * </p>
 * 
 * <p>A single Functor will create Channel instances from
 *   the bindChannel method, It does not maintain a reference to anything
 *   retrieved from the Focus chain besides what is referenced by the
 *   returned channel.
 * </p>
 * 
 * @author mike
 *
 */
public interface Functor<Tchannel>
{
  /**
   * <p>Creates a new Channel given a Focus and a set of arguments
   * </p>
   * 
   * <p>To support named parameter syntax, the Functor implementation is 
   *   responsible for calling BindChannel.bindTarget(Focus<?> targetFocus) on 
   *   any BindingChannels in the arguments, supplying a Focus that contains 
   *   the binding target destinations.
   * </p>
   * 
   * @param focus
   * @return
   * @throws BindException
   */
  Channel<Tchannel> bindChannel
    (Focus<?> focus
    ,Channel<?>[] arguments
    )
    throws BindException;
}
