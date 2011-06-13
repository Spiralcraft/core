//
// Copyright (c) 2010 Michael Toth
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



import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;
import spiralcraft.util.string.StringConverter;

/**
 * Concatenates the members of a set as strings
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class StringCat<T>
  extends Accumulator<String,T>
{


  private StringConverter<T> converter;
  
  public StringCat()
  {
  }
  
  public StringCat(StringConverter<T> converter)
  { this.converter=converter;
  }
  
  
  @Override
  protected Context<String> newContext(
    Channel<T> source,
    Focus<?> focus)
    throws BindException
  { return new StringCatContext(source,focus);
  }
  

  
  class StringCatContext
    extends Context<String>
  {
    
    private StringConverter<T> converter=StringCat.this.converter;

    public StringCatContext
     (Channel<T> source
     ,Focus<?> focus
     )
      throws BindException
    { 
      super(source,focus);
      
      if (converter==null)
      { converter=source.getReflector().getStringConverter();
      }     
    }

    
    @Override
    protected void update(ViewState<String> state)
    {
      T val=source.get();

      if (val!=null)
      {
        if (state.data==null)
        { state.data="";
        }
        
        if (converter!=null)
        { state.data=state.data+converter.toString(val);
        }
        else
        { state.data=state.data+val.toString();
        }
      }
    }

    @Override
    protected String latest(
      ViewState<String> state)
    { return state.data;
    }

    @Override
    protected boolean reset(ViewState<String> state,String val)
    { 
      state.data= val;
      return true;
    }

  }
  
  
    
}
