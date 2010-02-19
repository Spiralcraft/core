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
package spiralcraft.lang.parser;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Coercion;

public class RelationalNode<T1 extends Comparable<T1>,T2 extends T1>
  extends LogicalNode<T1,T2>
{

  private final boolean _greaterThan;
  private final boolean _equals;

  public RelationalNode(boolean greaterThan,boolean equals,Node op1,Node op2)
  { 
    super(op1,op2);
    _greaterThan=greaterThan;
    _equals=equals;
  }

  @Override
  public Node copy(Object visitor)
  { 
    RelationalNode<T1,T2> copy
      =new RelationalNode<T1,T2>
      (_greaterThan,_equals,_op1.copy(visitor),_op2.copy(visitor));
    if (sameOperandNodes(copy))
    { return this;
    }
    else
    { return copy;
    }
  }
  
  @Override
  public String reconstruct()
  {
    if (_greaterThan)
    {
      if (_equals)
      { return reconstruct(">=");
      }
      else
      { return reconstruct(">");
      }
    }
    else
    {
      if (_equals)
      { return reconstruct("<=");
      }
      else
      { return reconstruct("<");
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected LogicalTranslator 
    newTranslator(final Reflector<T1> r1,final Reflector<T2> r2)
  { 

    
    return new LogicalTranslator()
    { 
      private final Coercion<T2,T1> coercion;
      
      {
        if (r1.getContentType()!=r2.getContentType())
        {
          if (Number.class.isAssignableFrom(r1.getContentType())
              && Number.class.isAssignableFrom(r2.getContentType()))
          {
            if (r1.getContentType().equals(Float.class))
            {
              coercion=(Coercion) 
                new Coercion<Number,Float>() 
              {
                public Float coerce(Number val)
                { return val.floatValue();
                }
              };
            }
            else
            { coercion=null;
            }
          }
          else
          { coercion=null;
          }
        } 
        else
        { coercion=null;
        }
      }
      
      public Boolean translateForGet(T1 val,Channel<?>[] mods)
      { 
        Comparable<T1> val1= val;
        T2 val2=((Channel<T2>) mods[0]).get();

        if (val1==null)
        { 
          if (val2==null && _equals)
          { return true;
          }
          else
          { return null;
          }
        }
        
        int result=val1.compareTo(coercion!=null?coercion.coerce(val2):val2);

        if (_greaterThan)
        {
          if (_equals)
          { return result>=0;
          }
          else
          { return result>0;
          }
        }
        else
        {
          if (_equals)
          { return result<=0;
          }
          else
          { return result<0;
          }
        }
      }
    };
  }
  
  
  public boolean isGreaterThan()
  { return _greaterThan;
  }
  
  public boolean isEqual()
  { return _equals;
  }
  
  
  @Override
  public String getSymbol()
  { 
    if (_greaterThan)
    { 
      if (_equals)
      { return ">=";
      }
      else
      { return ">";
      }
    }
    else
    {
      if (_equals)
      { return "<=";
      }
      else
      { return "<";
      }
    }
  }

}


