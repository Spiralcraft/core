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
package spiralcraft.registry;

/**
 * Provides access to hierarchically organized application
 *   services for a single Registrant, such as logging,
 *   monitoring and lightweight persistence (ie. preferences).
 *
 * A hierarchy of RegistryNodes provides a unified, externally
 *   referencable namespace which external services can use to
 *   associate service instances with individual components.
 *
 * For example, using the fully qualified component path, logging
 *   services can tailor logging preferences for individual components,
 *   monitoring services can be activated for individual components
 *   and configuration services can key configuration details to
 *   individual components.
 */
public interface RegistryNode
{
  /**
   * Return the name of this node relative to its parent node.
   * The name of the root node is ""
   */
  public String getName();

  /**
   * Return the absolute path of this node relative to the ClassLoader
   *   to which the Registry package is scoped.
   */
  public String getAbsolutePath();

  /**
   * Obtain the instance of the specified class 
   *   that has been registered with this RegistryNode or
   *   its ancestors. 
   */
  public <X> X findInstance(Class<X> instanceClass);

  /**
   * Register an instance as a local singleton, ie. one that will be visible
   *   from this RegistryNode and its descendants.
   */
  public void registerInstance(Class<?> instanceClass,Object instance);


  /** 
   * Create a child RegistryNode and register it with this node
   *   under the specified name. If the name is already taken,
   *   append an incrementing number to differentiate it, ie. 
   *   the name "foo", if already used, becomes "foo1", or "foo2", etc.
   */
  public RegistryNode createChild(String name); 

  /**
   * Create a child RegistryNode named with the Class name, and 
   *   register the specified instance as the local singleton for the
   *   specified class.
   */
  public RegistryNode createChild(Class<?> instanceClass,Object instance);

  /**
   * Return the child RegistryNode with the specified name
   */
  public RegistryNode getChild(String name);
}
