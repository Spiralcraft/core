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
package spiralcraft.lang.functions;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.util.string.StringConverter;

/**
 * <p>A reversible function which converts Strings to Objects based on a
 *   StringConverter
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public class FromString<T>
  implements ChannelFactory<T,String>

{

  private StringConverter<T> converter;
  private final Reflector<T> target;
  
  public FromString(Reflector<T> target)
  { this.target=target;
  }
  
  public FromString(Reflector<T> target,StringConverter<T> converter)
  { 
    this.target=target;
    this.converter=converter;
  }
  
  public FromString(Class<T> target)
  { this(BeanReflector.<T>getInstance(target));
  }
  
  @Override
  public Channel<T> bindChannel(
    Channel<String> source,
    Focus<?> focus,
    Expression<?>[] arguments)
    throws BindException
  { return new FromStringChannel(source);
  }
  
  public class FromStringChannel
    extends SourcedChannel<String,T>
  {

    private StringConverter<T> converter=FromString.this.converter;
    
    public FromStringChannel(Channel<String> source) 
       throws BindException
    { 
      super(target,source);
      if (converter==null)
      { converter=target.getStringConverter();
      }
      if (converter==null)
      { 
        throw new BindException
          ("No StringConverter for "+source.getReflector().getTypeURI());
      }
    }
    
    @Override
    protected T retrieve()
    { return converter.fromString(source.get());
    }

    @Override
    protected boolean store(
      T val)
      throws AccessException
    { return source.set(converter.toString(val));
    }
  }

  
}
