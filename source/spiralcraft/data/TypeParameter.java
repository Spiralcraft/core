//
// Copyright (c) 2014 Michael Toth
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



/**
 * A property of a Type that can be referenced by type expressions and can
 *   be used to specialize an archetype in the context of a subtype.
 */
public class TypeParameter<T>
{  
  private String name;
  private Type<T> type;
  private T def;
  
  public String getName()
  { return name;
  }
  
  public void setName(String name)
  { this.name=name;
  }
  
  public Type<T> getType()
  { return type;
  }
  
  public void setType(Type<T> type)
  { this.type=type;
  }
  
  public void setDefault(T def)
  { this.def=def;
  }
  
  public T getDefault()
  { return def;
  }
}
