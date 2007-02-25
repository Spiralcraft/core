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

import spiralcraft.data.EditableTuple;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;



/**
 * Base class for a simple in-memory editable Tuple.
 */
public class EditableArrayTuple
  extends ArrayTuple
  implements EditableTuple
{
  public EditableArrayTuple(FieldSet fieldSet)
  { super(fieldSet);
  }
  
  public EditableArrayTuple(Tuple original)
    throws DataException
  { super(original);
  }
  
  public void set(int index,Object val)
  { data[index]=val;
  }
  
  public boolean isMutable()
  { return true;
  }
  
  public EditableTuple widen(Type type)
    throws DataException
  { return (EditableTuple) super.widen(type);
  }
}