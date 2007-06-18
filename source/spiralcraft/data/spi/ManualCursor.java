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

import spiralcraft.data.transport.Cursor;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;

/**
 * A Cursor that provides access to Tuples that are provided by the
 *   caller of the dataSetTuple()  method.   
 */
public class ManualCursor<T extends Tuple>
  implements Cursor<T>
{

  protected final FieldSet fieldSet;
  protected T tuple;
  
  public ManualCursor(FieldSet fieldSet)
  { this.fieldSet=fieldSet;
  }
  
  public FieldSet dataGetFieldSet()
  { return fieldSet;
  }
  
  public T dataGetTuple()
  { return tuple;
  }
  
  public void dataSetTuple(T tuple)
  { this.tuple=tuple;
  }
}
