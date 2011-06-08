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
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.Channel;

/**
 * <p>An Identifier composed from a candidate key of a Type or Type relation
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
  

  /**
   * Construct a KeyIdentifier that represents the primary key of the
   *   specified type by reading data from the specified array of Channels
   *   
   * @param <T>
   * @param type
   * @param sources
   * @return
   * @throws DataException
   */
  public static final <T> KeyIdentifier<T> read
    (Type<T> type,Channel<?>[] sources)
    throws DataException
  { 
    Object[] data=new Object[sources.length];
    int i=0;
    for (Channel<?> channel: sources)
    { data[i++]=channel.get();
    }
    return read(type,data,true);
  }
  
  /**
   * Construct a KeyIdentifier that represents the primary key of the
   *   specified Type using the data in the specified array.
   *   
   * @param <T>
   * @param type
   * @param data
   * @param useReference true, if the array is immutable
   * @return
   * @throws DataException
   */
  @SuppressWarnings("unchecked")
  public static final <T> KeyIdentifier<T> read
    (Type<T> type,Object[] data,boolean useReference)
    throws DataException
  {
    Key<T> primaryKey=type.getPrimaryKey();
    return new KeyIdentifier<T>
      ((Type<T>) primaryKey.getSource().getType(),primaryKey,data,useReference);
  }
  
  /**
   * Construct a KeyIdentifier that represents a relation Key of the
   *   specified type.
   *   
   * @param <T>
   * @param type
   * @param sources
   * @return
   * @throws DataException
   */
  public KeyIdentifier(Type<T> identifiedType,Tuple dataref) 
    throws DataException
  { 
    super(dataref);
    this.identifiedType=identifiedType;
  }
  
  private KeyIdentifier
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
  


  @Override
  public Type<?> getIdentifiedType()
  { return identifiedType;
  }

  @Override
  public boolean isPublic()
  { return true;
  }
  
  @Override
  public String toString()
  { return identifiedType.getURI()+"#"+AbstractTuple.tupleToString(this);
  }

  @Override
  protected boolean isComparableTo(KeyTuple tuple)
  { 
    return 
        (tuple instanceof KeyIdentifier)
        ?((KeyIdentifier<?>) tuple).identifiedType.equals(identifiedType)
        :false;
  }
  
  @Override
  protected int computeHashCode(int hashCode)
  { 
    return hashCode ^ identifiedType.hashCode();
  }
}
