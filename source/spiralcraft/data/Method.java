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
package spiralcraft.data;


import java.net.URI;

import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Signature;

/**
 * An action that can be taken on an data object
 * 
 * @author mike
 *
 */
public interface Method
{
  
  /**
   * A Method is a member of data Type.
   * 
   * @return
   */
  Type<?> getDataType();
  
  /**
   * This Method returns data of a specific Type
   * 
   * @return The Type, or null if this method does not return data.
   */
  Type<?> getReturnType();
  
  /**
   * The Method name
   * 
   * @return
   */
  String getName();
  
  /**
   * A description of what this method does
   * 
   * @return
   */
  String getDescription();
  
  /**
   * @return This method's URI, which is this method's name in the context of
   *   the Type that it belongs to. 
   */
  URI getURI();
  
  /**
   * The Types of the parameters accepted by this method.
   */
  Type<?>[] getParameterTypes();
  
  /**
   * Bind the method to a channel, which will invoke the operation and
   *   return the result when Channel.get() is called.
   * 
   * @param focus
   * @param source
   * @param params
   * @return
   * @throws BindException
   */
  Channel<?> bind(Channel<?> source,Channel<?>[] params)
    throws BindException;
  
  /**
   * 
   * @return The name, return type, and parameter types of this method
   * 
   * @throws BindException
   */
  Signature getSignature()
    throws BindException;
  
  /**
   * Static methods are published in the meta namespace for the
   *   type, e.g. [@myns:MyType].@myStaticMethod(p1,p2) 
   */
  boolean isStatic();
  
  /**
   * Generic methods are methods that are not resolvable without concrete
   *   Type information supplied via type parameters or other means.
   * 
   * @return
   */
  boolean isGeneric();
  
}
