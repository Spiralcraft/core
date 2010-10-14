//
// Copyright (c) 2009 Michael Toth
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

import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.core.TypeImpl;

/**
 * An uninstantiable formal Type used to indicate that data of any Type
 *   is accepted.
 * 
 * @author mike
 *
 */
public class AnyType
  extends TypeImpl<Void>
{

  public static final URI TYPE_URI
    =URI.create("class:/spiralcraft/data/types/standard/Any");
  
  public static final Type<?> resolve()
  { 
    try
    { return Type.resolve(TYPE_URI);
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  } 
  
  public AnyType(TypeResolver resolver,URI uri)
  { super(resolver,uri);
  }
  
  @Override
  public boolean isAssignableFrom(Type<?> type)
  { return true;
  }
}
