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
package spiralcraft.data.core;

import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

/**
 * Core implementation of a Field
 */
public class FieldImpl
  implements Field
{
  private boolean locked;
  private SchemeImpl scheme;
  private int index;
  private String name;
  private Type type;
  
  /**
   * Set the scheme
   */
  void setScheme(SchemeImpl scheme)
  { 
    assertUnlocked();
    this.scheme=scheme;
  }
  
  /**
   * Return the Scheme
   */
  public Scheme getScheme()
  { return scheme;
  }

  /**
   * Set the index
   */
  void setIndex(int index)
  { 
    assertUnlocked();
    this.index=index;
  }

  /**
   * Return the index
   */
  public int getIndex()
  { return index;
  }
  
  /**
   * Set the field name
   */
  public void setName(String name)
  { 
    assertUnlocked();
    this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  /**
   * Set the data Type
   */
  public void setType(Type type)
  { 
    assertUnlocked();
    this.type=type;
  }
  
  public Type getType()
  { return type;
  }
  
  public Object getValue(Tuple t)
  { 
    if (t.getScheme()!=scheme)
    { 
      throw new IllegalArgumentException
        ("Field '"+name+"' not in Tuple Scheme");
    }
    return t.get(index);
  }
  
  public void setValue(EditableTuple t,Object value)
    throws DataException
  { 
    if (t.getScheme()!=scheme)
    { 
      throw new IllegalArgumentException
        ("Field '"+name+"' not in Tuple Scheme");
    }
    t.set(index,value);
  }
  
  void lock()
  { locked=true;
  }
  
  public String toString()
  { return super.toString()+":"+name+"("+type.getUri()+")";
  }
  
  private final void assertUnlocked()
  { 
    if (locked)
    { throw new IllegalStateException("Field is in read-only state");
    }
  }
}