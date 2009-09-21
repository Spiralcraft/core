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
package spiralcraft.data.spi;

import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.CursorBinding;


import java.util.Iterator;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

/**
 * <p>A SerialCursor which returns the contents of an Iterator
 * </p>
 * 
 */
public class IteratorCursor<T extends Tuple>
  implements SerialCursor<T>
{
  protected final FieldSet fieldSet;
  protected Identifier relationId;
  protected Type<?> type;
  protected Iterator<T> iterator;
  protected T tuple;
  protected boolean eof;
  
  
  public Type<?> getResultType()
  { return type;
  }
  
  
  public IteratorCursor(FieldSet fieldSet,Iterator<T> iterator)
  { 
    this.type=fieldSet.getType();
    this.fieldSet=fieldSet;
    this.iterator=iterator;
    if (!iterator.hasNext())
    { eof=true;
    }
  }
  
  /**
   *@return The FieldSet common to all the Tuples that will be returned by this Cursor
   */
  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  
  /**
   *@return The Tuple currently positioned under the Cursor
   */
  @Override
  public T getTuple()
    throws DataException
  {
    if (tuple==null && !eof)
    { advance();
    }
    return tuple;
  }

  @Override
  public Identifier getRelationId()
  { return relationId;
  }
  
  public void setRelationId(Identifier relationId)
  { this.relationId=relationId;
  }
  
  protected void advance()
  {
    if (iterator.hasNext())
    { tuple=iterator.next();
    }
    else
    { 
      tuple=null;
      eof=true;
    }
  }
  


  @Override
  public boolean next() throws DataException
  { 
    if (!eof)
    { advance();
    }
    return !eof;
  }

  
  @Override
  public Channel<T> bind()
    throws BindException
  { return new CursorBinding<T,IteratorCursor<T>>(this);
  }
  
  /**
   * Does nothing
   */
  @Override
  public void close()
  {
  }
}
