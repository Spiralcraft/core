//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.builder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * <p>Tags a Java class as being managable by an AssemblyClass, even if
 *   there is no [classname].assy.xml file associated with it.
 * </p>
 * 
 * <p>This annotation allows builder clients to differentiate between 
 *   unmanaged POJOs (ie. data objects, primitives) and objects that are 
 *   candidates for additional management.
 * </p>
 *   
 * 
 * @author mike
 *
 */

@Retention(value=RetentionPolicy.RUNTIME)
public @interface Managable
{

}
