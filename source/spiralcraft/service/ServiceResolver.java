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
package spiralcraft.service;

/**
 * Provides a means for components and other Services to find Services
 *   that provide required interfaces. 
 *
 * Services are uniquely identified by the interfaces they
 *   provide in conjunction with an optional Key, for cases where
 *   multiple services which provide the same interface coexist within the 
 *   scope of the same ServiceResolver.
 *
 * The ServiceResolver interface provides the primary means for application
 *   containers to make Services available to application components in
 *   a manner which isolates the application specific organization of
 *   Services from generic component assemblies, which normally expect
 *   to obtain an interface of a specific type.
 */
public interface ServiceResolver
{

  /**
   * Find all Services that implement the specified interface
   *
   *@throws AmbiguousServiceException if multiple services in the same scope with
   *  the same key provide the same interface
   */
  public Service[] findServices(Class serviceInterface)
    throws AmbiguousServiceException;

  /**
   * Find the service which implements the specified interface
   *   and has the specified selector.
   *
   *@throws AmbiguousServiceException if multiple services in the same scope with
   *  the same selector provide the same interface
   */
  public Service findService(Class serviceInterface,Object selector)
    throws AmbiguousServiceException;

}
