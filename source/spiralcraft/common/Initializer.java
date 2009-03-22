//
// Copyright (c)2009 Michael Toth
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
package spiralcraft.common;

/**
 * <p>An object that is instantiated via the java.util.ServiceLoader
 *   subsystem during the initialization phase of a context that runs
 *   in a new Classloader. 
 * </p>
 * 
 * <p>The Initializer objects supplied by modules and other kinds of jars
 *   are used to load and register services and singletons that may be 
 *   requested dynamically. 
 * </p>
 * 
 * <p>The Initializer should do all of its work on instantiation. Typically,
 *   this involves referencing specific classes associated with a particular
 *   subsystem.
 * </p>
 * @author mike
 *
 */
public interface Initializer
{

}
