//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.data.types.standard;

import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.net.URI;

/**
 * <p>A Local String Identifier
 * </p>
 * 
 * <p>A universal synthetic key which identifies an object in a collection hierarchy.
 *   LSIDs are expanded to reference each layer in the
 *   collection hierarchy out of which it is exported. 
 * </p>
 * 
 * @author mike
 *
 */
public class LSIDType
  extends PrimitiveTypeImpl<String>
{
  public LSIDType(TypeResolver resolver,URI uri)
  { super(resolver,uri,String.class);
  }
  
  @Override
  public String fromString(String str)
  { return str;
  }
}