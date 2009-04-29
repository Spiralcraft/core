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

import spiralcraft.data.access.Cursor;
import spiralcraft.data.lang.CursorBinding;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

/**
 * A Cursor that provides access to Tuples that are provided by the
 *   caller of the dataSetTuple()  method.   
 */
public class ManualCursor<T extends Tuple>
  implements Cursor<T>
{

  protected final FieldSet fieldSet;
  protected T tuple;
  protected Identifier relationId;
  
  public ManualCursor(FieldSet fieldSet)
  { this.fieldSet=fieldSet;
  }
  
  @Override
  public Type<?> getResultType()
  { return fieldSet.getType();
  }
  
  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  @Override
  public T getTuple()
  { return tuple;
  }
  
  public void setTuple(T tuple)
  { this.tuple=tuple;
  }
  
  @Override
  public Identifier getRelationId()
  { return relationId;
  }
  
  public void setRelationId(Identifier relationId)
  { this.relationId=relationId;
  }
  
  @Override
  public Channel<T> bind()
    throws BindException
  { return new CursorBinding<T,ManualCursor<T>>(this);
  }

  /**
   * Do nothing
   */
  @Override
  public void close()
  { }
}
