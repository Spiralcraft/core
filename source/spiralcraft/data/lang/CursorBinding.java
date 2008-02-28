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
package spiralcraft.data.lang;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.access.Cursor;
import spiralcraft.data.session.BufferChannel;

/**
 * A spiralcraft.lang binding for Tuples, which uses the Tuple's Scheme
 *   as the type model for binding expressions. The cursor is replaceable.
 */
public class CursorBinding<T extends Tuple,C extends Cursor<T>>
  extends TupleBinding<T>
{
  private C cursor;
  
  public CursorBinding(C cursor)
    throws BindException
  { 
    super(cursor.dataGetFieldSet(),false);
    this.cursor=cursor;
  }
  
  public CursorBinding(FieldSet fieldSet)
    throws BindException
  { super(fieldSet,false);
  }
  
  public void setCursor(C cursor)
  { 
    if (cursor.dataGetFieldSet()!=getFieldSet())
    { throw new IllegalArgumentException
        ("Cursor is incompatible with existing binding");
    }
    this.cursor=cursor;
  }
  
  public C getCursor()
  { return cursor;
  }

  protected T retrieve()
  { 
    try
    { return cursor.dataGetTuple();
    }
    catch (DataException x)
    { throw new RuntimeException("Caught exception getting value",x);
    }
  }
  
  protected boolean store(Tuple val)
  { throw new UnsupportedOperationException("Can't replace tuple");
  }

  /**
   * Convenience method to buffer 
   * 
   * @param focus
   * @return
   * @throws BindException
   * @throws DataException
   */
  public BufferChannel buffer(Focus<?> focus)
    throws BindException,DataException
  { 
    return new BufferChannel
      (Type.getBufferType(cursor.getResultType()),this,focus);
  }
}

