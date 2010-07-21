//
// Copyright (c) 1998,2005 Michael Toth
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

import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;

import spiralcraft.data.spi.ManualCursor;

import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.BindException;

/**
 * <P>A Focus with a subject that references a Tuple of a specific type.
 * 
 * <P>Useful for evaluating a set of Expressions on a single caller-provided
 *   Tuple at a time. 
 */
public class TupleFocus<T extends Tuple>
    extends SimpleFocus<T>
{

  private final ManualCursor<T> cursor;
  
  public static final <T extends Tuple> TupleFocus<T> create
    (Focus<?> parentFocus,FieldSet fieldSet)
    throws DataException
  {
    ManualCursor<T> cursor=new ManualCursor<T>(fieldSet);
    CursorBinding<T,ManualCursor<T>> cursorBinding;
    
    try
    { cursorBinding=new CursorBinding<T,ManualCursor<T>>(cursor);
    }
    catch (BindException x)
    { 
      throw new DataException
        ("Error creating binding for FieldSet '"+fieldSet+"':"+x,x);
    }
    return new TupleFocus<T>(parentFocus,cursor,cursorBinding);
  }
  
  public TupleFocus
    (Focus<?> parentFocus
    ,ManualCursor<T> cursor
    ,CursorBinding<T,ManualCursor<T>> cursorBinding
    )
    throws DataException
  { 
    super(parentFocus,cursorBinding);
    this.cursor=cursor;
  }
  
  
  public void setTuple(T tuple)
  { cursor.setTuple(tuple);
  }
  
}
