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

/**
 * <p>Base interface for functionality extensions applied to Channels</p>
 * 
 * <p>A Decorator is bound to a Channel and allows the binding of more 
 *   specific functionality (for example, Iteration) to the output of a
 *   channel. The Channel determines which implementation, if any, it
 *   should use to support a given Decorator interface.
 * </p> 
 * 
 * <p>A Decorator is requested from a Channel by an application component
 *   which uses the spiralcraft.lang package and expects a specific kind 
 *   of interface or other functionality to be supported by the object
 *   referenced by the Channel. 
 * </p>
 * 
 * <p>
 *   For example, an application component which provides a means for
 *   iterating through a container referenced by a Channel will request
 *   an IterationDecorator from the Channel.decorate() method. The
 *   Channel will determine, usually by querying its associated Reflector,
 *   which specific implementation of the IterationDecorator it will
 *   create to satisfy the contract, if any.   
 *   
 * </p>
 * 
 */ 
public interface Decorator<T>
{

  
}
