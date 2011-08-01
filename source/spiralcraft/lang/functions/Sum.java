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

import java.math.BigDecimal;
import java.math.BigInteger;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.Accumulator;
import spiralcraft.lang.spi.ViewState;
import spiralcraft.util.lang.ClassUtil;

/**
 * Computes the arithmetic sum of a sequence
 * 
 * @author mike
 *
 * @param <Tresult>
 * @param <Tstate>
 * @param <Tsource>
 */
public class Sum<T extends Number>
  extends Accumulator<T,T>
{

  private Class<T> type;
  
  @SuppressWarnings("unchecked")
  @Override
  protected Context<?> newContext(
    Channel<T> source,
    Focus<?> focus)
    throws BindException
  {
    this.type=source.getContentType();
    if (type.isPrimitive())
    { type=(Class<T>) ClassUtil.boxedEquivalent(type);
    }
    
    if (type.equals(Integer.class))
    { 
      return new SumContext<Integer>(source,focus)
      {
        @Override
        protected Integer zero()
        { return Integer.valueOf(0);
        }

        @Override
        protected Integer add(Integer v1,Integer v2)
        { return v1+v2;
        }
      };
        
    }
    else if (type.equals(Float.class))
    { 
      return new SumContext<Float>(source,focus)
      {
        @Override
        protected Float zero()
        { return Float.valueOf(0);
        }

        @Override
        protected Float add(Float v1,Float v2)
        { return v1+v2;
        }
      };
        
    }
    else if (type.equals(Long.class))
    { 
      return new SumContext<Long>(source,focus)
      {
        @Override
        protected Long zero()
        { return Long.valueOf(0);
        }

        @Override
        protected Long add(Long v1,Long v2)
        { return v1+v2;
        }
      };
        
    }
    else if (type.equals(Double.class))
    { 
      return new SumContext<Double>(source,focus)
      {
        @Override
        protected Double zero()
        { return Double.valueOf(0);
        }

        @Override
        protected Double add(Double v1,Double v2)
        { return v1+v2;
        }
      };
        
    }
    else if (type.equals(Short.class))
    { 
      return new SumContext<Short>(source,focus)
      {
        @Override
        protected Short zero()
        { return Short.valueOf((short) 0);
        }
        
        @Override
        protected Short add(Short v1,Short v2)
        { return Integer.valueOf(v1+v2).shortValue();
        }
      };
        
    }
    else if (type.equals(Byte.class))
    { 
      return new SumContext<Byte>(source,focus)
      {
        @Override
        protected Byte zero()
        { return Byte.valueOf((byte) 0);
        }
        
        @Override
        protected Byte add(Byte v1,Byte v2)
        { return Integer.valueOf(v1+v2).byteValue();
        }
      };
        
    }
    else if (type.equals(BigInteger.class))
    { 
      return new SumContext<BigInteger>(source,focus)
      {
        @Override
        protected BigInteger zero()
        { return BigInteger.ZERO;
        }
        
        @Override
        protected BigInteger add(BigInteger v1,BigInteger v2)
        { return v1.add(v2);
        }
      };
        
    }
    else if (type.equals(BigDecimal.class))
    { 
      return new SumContext<BigDecimal>(source,focus)
      {
        @Override
        protected BigDecimal zero()
        { return BigDecimal.ZERO;
        }
        
        @Override
        protected BigDecimal add(BigDecimal v1,BigDecimal v2)
        { return v1.add(v2);
        }
      };
        
    }
    
    throw new BindException("Cannot Sum data of type "+type.getName());
  }
  

  
  abstract class SumContext<Tstate>
    extends Context<Tstate>
  {
    
    public SumContext
     (Channel<T> source
     ,Focus<?> focus
     )
      throws BindException
    { super(source,focus);
    }

    protected abstract Tstate add(Tstate v1,Tstate v2);
    
    protected abstract Tstate zero();
    
    @SuppressWarnings("unchecked")
    @Override
    protected void update(ViewState<Tstate> state)
    {
      Tstate val=(Tstate) source.get();

      if (state.data==null)
      { state.data=val;
      }
      else
      { 
        if (val!=null)
        { state.data=add(state.data,val);
        }
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T latest(
      ViewState<Tstate> state)
    { return (T) (state.data==null?zero():state.data);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean reset(
      ViewState<Tstate> state,
      T val)
    { 
      state.data=(Tstate) val;
      return true;
    }

  }
  
  
    
}
