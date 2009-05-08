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
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Type;
import spiralcraft.data.Field;
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
  
  public EditableArrayTuple(Type<?> type)
  { super(type.getFieldSet());
  }

  protected <X> void copyFieldFrom(Field<X> field,Tuple source)
    throws DataException
  { field.setValue(this,field.getValue(source));
  }
  
  public void set(int index,Object val)
  { 
    if (debug)
    { log.fine("Setting "+index+"="+val+" in Tuple "+this);
    }
    data[index]=val;
  }
  
  @Override
  public boolean isMutable()
  { return true;
  }
  
  @Override
  public EditableTuple widen(Type<?> type)
    throws DataException
  { return (EditableTuple) super.widen(type);
  }
  
  public void set(String fieldName,Object data)
    throws DataException
  {
    FieldSet fieldSet=this.fieldSet;
    if (fieldSet.getType()!=null)
    { fieldSet=fieldSet.getType().getFieldSet();
    }

    Field<Object> field=fieldSet.getFieldByName(fieldName);
    if (field==null)
    { throw new FieldNotFoundException(fieldSet,fieldName);
    }
    field.setValue(this,data);
  } 
  
  public void copyFrom(Tuple source)
    throws DataException
  { 
    Type<?> type=getType();
    if (getFieldSet()==source.getFieldSet()
        || (type!=null && type.hasArchetype(source.getType()))
       )
    { 
      for (Field<?> field: source.getFieldSet().fieldIterable())
      { copyFieldFrom(field,source);
      }
    }

    
    if (source.getBaseExtent()!=null)
    { ((EditableArrayTuple) baseExtent).copyFrom(source.getBaseExtent());
    }
  }

  @Override
  protected AbstractTuple createBaseExtent(
    FieldSet fieldSet)
  { return new EditableArrayTuple(fieldSet);
  }

  @Override
  protected AbstractTuple createBaseExtent(
    Tuple tuple)
    throws DataException
  { return new EditableArrayTuple(tuple);
  }
  
}