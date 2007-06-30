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

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.access.Cursor;

/**
 * A spiralcraft.lang binding for Tuples, which uses the Tuple's Scheme
 *   as the type model for binding expressions.
 */
public class CursorBinding<T extends Tuple>
  extends TupleBinding<T>
{
  private Cursor<T> cursor;
  
  public CursorBinding(Cursor<T> cursor)
    throws BindException
  { 
    super(cursor.dataGetFieldSet(),false);
    this.cursor=cursor;
  }
  
  public CursorBinding(FieldSet fieldSet)
    throws BindException
  { super(fieldSet,false);
  }
  
  public void setCursor(Cursor<T> cursor)
  { 
    if (cursor.dataGetFieldSet()!=getFieldSet())
    { throw new IllegalArgumentException
        ("Cursor is incompatible with existing binding");
    }
    this.cursor=cursor;
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

}

