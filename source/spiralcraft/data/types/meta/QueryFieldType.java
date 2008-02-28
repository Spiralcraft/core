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
package spiralcraft.data.types.meta;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.core.QueryField;

import spiralcraft.data.reflect.ReflectionType;

import java.net.URI;

/**
 * A Type implementation that represents a Field
 */
public class QueryFieldType
  extends ReflectionType<QueryField>
{
  public QueryFieldType(TypeResolver resolver,URI uri)
  { super(resolver,uri,QueryField.class,QueryField.class);
  }
  

}