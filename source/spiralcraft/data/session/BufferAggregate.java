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
package spiralcraft.data.session;

import java.util.Iterator;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.Type;
import spiralcraft.data.Identifier;

import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.ArrayListAggregate;

public class BufferAggregate<T>
  extends Buffer
  implements EditableAggregate<T>
{
  private final DataSession session;
  private Aggregate<T> original;
  private Type type;
  private Identifier id;
  
  private EditableArrayListAggregate<T> appendix;
  
  public BufferAggregate(DataSession session,Aggregate<T> original)
  { 
    this.session=session;
    this.original=original;
    this.type=original.getType();
  }
  
  public BufferAggregate(DataSession session,Type type)
  { 
    this.session=session;
    this.type=type;
  }
  
  public Identifier getId()
  { return id;
  }
  
  public void setId(Identifier id)
  { this.id=id;
  }
  
  @Override
  public BufferAggregate<T> asAggregate()
  { return this;
  }

  @Override
  public BufferTuple asTuple()
  { return null;
  }

  @Override
  public Type<?> getType()
  { return original.getType();
  }

  @Override
  public boolean isAggregate()
  { return true;
  }

  @Override
  public boolean isTuple()
  { return false;
  }

  @Override
  public String toText(
    String indent)
    throws DataException
  { return "Buffer:["+original.toText(indent+"  ")+"\r\n]";
  }

  @Override
  public void add(
    T val)
  {
    
    if (val==null)
    { throw new IllegalArgumentException
        ("Cannot add a null value to an Aggregate");
    }
    if (appendix==null)
    { appendix=new EditableArrayListAggregate<T>(getType());
    }
    appendix.add(val);
    // XXX Should check type, might have to dirty fields, etc. Might be
    //   a new Tuple which requires data, etc.
    
  }

  @Override
  public void addAll(
    Aggregate<T> values)
  {
    // TODO Auto-generated method stub
    for (T value : values)
    { add(value);
    }
  }

  @Override
  public T get(
    int index)
  {
    if (index >= original.size())
    { 
      index-=original.size();
      if (appendix!=null)
      { return appendix.get(index);
      }
      else throw new IndexOutOfBoundsException
        ("Index "+index+" exceeds size "+size());
    }
    else
    { return original.get(index);
    }
  }

  @Override
  public boolean isMutable()
  {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public int size()
  {
    return original.size()
      +(appendix!=null?appendix.size():0);
  }

  @Override
  public Aggregate<T> snapshot()
    throws DataException
  {
    // TODO Auto-generated method stub
    return new ArrayListAggregate<T>(this);
  }

  @Override
  public Iterator iterator()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
