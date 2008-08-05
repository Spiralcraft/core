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
package spiralcraft.data.spi;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Identifier;
import spiralcraft.data.Projection;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;

import java.util.Iterator;

/**
 * Holds a aggregation of objects of a common type.
 */
public abstract class AbstractAggregate<T>
  implements Aggregate<T>
{
  
  private final Type<?> type;
  private Identifier id;
  
  /**
   * <p>Create a new AbstractAggregate of the specified Type. The type
   *   must be an Aggregate type (eg. Foo.list)
   * </p>
   * 
   * @param type
   */
  protected AbstractAggregate(Type<?> type)
  { 
    if (type==null)
    { throw new IllegalArgumentException("Aggregate Type cannot is null");
    }
    this.type=type;
  }

  public Identifier getId()
  { return id;
  }
  
  public void setId(Identifier id)
  { this.id=id;
  }
  
  public boolean isAggregate()
  { return true;
  }
  
  public Aggregate<?> asAggregate()
  { return this;
  }
  
  public boolean isTuple()
  { return false;
  }

  public Tuple asTuple()
  { throw new UnsupportedOperationException("An aggregate is not a Tuple");
  }
  
  
  public Type<?> getType()
  { return type;
  }
  
  public abstract Iterator<T> iterator();
  
  public abstract int size();
    
  @Override
  public String toString()
  {
    StringBuilder builder=new StringBuilder();
    builder.append(getClass().getName()+"{");
    boolean first=true;
    for (Object o:this)
    {
      if (!first)
      { builder.append(",");
      }
      else
      { first=false;
      }
      builder.append(o.toString());
      
    }
    builder.append("}");
    return builder.toString();

  }
  
  public String toText(String indent)
    throws DataException
  {
    StringBuilder builder=new StringBuilder();
    builder.append("\r\n").append(indent);
    builder.append("{");
    boolean first=true;
    for (Object o:this)
    {
      if (!first)
      { 
        builder.append("\r\n").append(indent);
        builder.append(",");
      }
      else
      { first=false;
      }
      if (o instanceof DataComposite)
      { builder.append(((DataComposite) o).toText(indent+"  "));
      }
      else
      { builder.append(o.toString());
      }
      
    }
    builder.append("\r\n").append(indent);
    builder.append("}");
    return builder.toString();
  }

  public abstract T get(int index);

  public boolean isMutable()
  { return false;
  }

  public abstract Aggregate<T> snapshot()
    throws DataException;
  
  
  public Index<T> getIndex(Projection projection,boolean create)
    throws DataException
  { return null;
  }

}