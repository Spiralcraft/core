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

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.ReverseTranslator;
import spiralcraft.lang.spi.TranslatorChannel;
import spiralcraft.util.string.StringConverter;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

/**
 * <p>Provides access to spiralcraft.data metadata for primitive types.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class PrimitiveReflector<T>
  extends BeanReflector<T>
  implements TypedDataReflector<T>
{

  private final Type<T> type;
  
  private final StringConverter<T> converter
    =new StringConverter<T>()
  {

    @Override
    public T fromString(String val)
    { 
      try
      { return type.fromString(val);
      }
      catch (DataException x)
      { throw new IllegalArgumentException(val,x);
      }
    }
    
    @Override
    public String toString(T val)
    {
      return type.toString(val);
    }
     
  };
  
  public PrimitiveReflector(Type<T> type)
  { 
    super(type.getNativeClass());
    this.type=type;
    this.type.link();
  }
  
  @Override
  public Type<T> getType()
  { return type;
  }
  
  @Override
  public Reflector<?> disambiguate(Reflector<?> alternate)
  {
    if (alternate instanceof BeanReflector<?>)
    { 
      // Don't allow BeanReflectors to be overriden by Primitive reflectors
      return alternate;
    }
    else if (getTypeModel()==alternate.getTypeModel())
    { return this;
    }
    else
    { 
      // Defer to dependent type model
      return alternate.disambiguate(this);
    }    
  }
  
  @Override
  public StringConverter<T> getStringConverter()
  { 
    if (type.isStringEncodable())
    { return converter;
    }
    else
    { return null;
    }
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  /**
   * Create a constructor channel
   */
  public Channel<T> bindChannel(
    Focus<?> focus,
    Channel<?>[] arguments)
    throws BindException
  {
    if (type.isDataEncodable())
    { 
      TupleReflector dataReflector
        =(TupleReflector<Tuple>) DataReflector.<Tuple>getExternalizedInstance(type);
      
      Channel<DataComposite> tupleChannel
        =dataReflector.bindChannel(focus,arguments);
      
      try
      {
        return new TranslatorChannel<T,DataComposite>
          (tupleChannel
          ,new ReverseTranslator<T,DataComposite>
            (this,new ToDataTranslator<T>(type))
          ,arguments
          );
      }
      catch (DataException x)
      { 
        throw new BindException
          ("Error creating tuple constructor channel for "+getTypeURI(),x);
      }
    }
    return super.bindChannel(focus,arguments);
  }

}
