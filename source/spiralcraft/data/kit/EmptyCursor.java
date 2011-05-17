//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.data.kit;

import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.lang.CursorBinding;



import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

/**
 * <p>A ScrollableCursor which contains nothing
 * </p>
 */
public class EmptyCursor<T extends Tuple>
  implements ScrollableCursor<T>
{
  protected final FieldSet fieldSet;
  protected Type<?> type;
  
  
  @Override
  public Type<?> getResultType()
  { return type;
  }
  
  public EmptyCursor(FieldSet fieldSet)
  { 
    this.type=fieldSet.getType();
    this.fieldSet=fieldSet;
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
  { return null;
  }

  @Override
  public Identifier getRelationId()
  { return null;
  }
  

  
  @Override
  public void moveAfterLast() 
    throws DataException
  { 
  }


  @Override
  public void moveBeforeFirst() throws DataException
  { 
  }


  @Override
  public boolean moveFirst() throws DataException
  { return false;
  }


  @Override
  public boolean moveLast() throws DataException
  { return false;
  }


  @Override
  public boolean next() throws DataException
  { return false;
  }


  @Override
  public boolean previous() throws DataException
  { return false;
  }
  
  @Override
  public Channel<T> bind()
    throws BindException
  { return new CursorBinding<T,EmptyCursor<T>>(this);
  }
  
  /**
   * Does nothing
   */
  @Override
  public void close()
  {
  }
}
