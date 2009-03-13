//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.data.util;


import spiralcraft.data.Aggregate;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.spi.EditableArrayListAggregate;


/**
 * <p>A DataConsumer which adds data to a Aggregate for later retrieval
 * </p>
 * 
 * @author mike
 *
 * @param <Ttype>
 */
public class DataAggregator<Ttype extends Tuple>
  implements DataConsumer<Ttype>
{

  private EditableArrayListAggregate<Ttype> list;
  
  @Override
  public void dataAvailable(Ttype tuple)
    throws DataException
  { list.add(tuple);  
  }

  @Override
  public void dataFinalize()
    throws DataException
  {    
  }

  @SuppressWarnings("unchecked") // Type cast
  @Override
  public void dataInitialize(
    FieldSet fieldSet)
    throws DataException
  { 
    Type type=fieldSet.getType();
    if (type==null)
    { 
      throw new DataException
        ("The supplied FieldSet must be associated with a type");
    }
    list=new EditableArrayListAggregate<Ttype>(Type.getAggregateType(type));
  }
  
  public Aggregate<Ttype> getAggregate()
  { return list;
  }
  
  public void clear()
  { list.clear();
  }

}
