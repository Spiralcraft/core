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
package spiralcraft.data.spi;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.Channel;

/**
 * <p>An Identifier composed from a candidate key of a Type
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class KeyIdentifier<T>
  extends KeyTuple
  implements Identifier
{
  
  private final Type<T> identifiedType;
  

  public static final <T> KeyIdentifier<T> read
    (Type<T> type,Channel<?>[] sources)
    throws DataException
  { 
    Object[] data=new Object[sources.length];
    int i=0;
    for (Channel<?> channel: sources)
    { data[i++]=channel.get();
    }
    return new KeyIdentifier<T>(type,type.getPrimaryKey(),data,true);
  }
  
  public KeyIdentifier
    (Type<T> identifiedType
    ,FieldSet fieldSet
    ,Object[] data
    ,boolean useReference
    )
    throws DataException
  { 
    super(fieldSet,data,useReference);
    this.identifiedType=identifiedType;
  }  
  
  public KeyIdentifier(Type<T> identifiedType,Tuple dataref) 
    throws DataException
  { 
    super(dataref);
    this.identifiedType=identifiedType;
  }

  @Override
  public Type<?> getIdentifiedType()
  { return identifiedType;
  }

  @Override
  public boolean isPublic()
  { return true;
  }
}
