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
package spiralcraft.lang.reflect;


import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.Translator;
import spiralcraft.util.ArrayUtil;

// import spiralcraft.util.ArrayUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


class ConstructorTranslator<Tbean>
  implements Translator<Tbean,Void>
{

  private final Constructor<Tbean> _method;
  private final Reflector<Tbean> _reflector;
  
  public ConstructorTranslator(Reflector<Tbean> reflector,Constructor<Tbean> method)
  { 
    _method=method;
    _reflector=reflector;
  }

  public Constructor<Tbean> getMethod()
  { return _method;
  }

  @SuppressWarnings("unchecked") // Method is not generic
  public Tbean translateForGet(Void value,Channel<?>[] params)
  { 
//    System.out.println("MethodTranslator "+toString()+" translateForGet: ["+value+"]");
    
    if (params==null)
    { throw new IllegalArgumentException
        ("No parameters for "
        +_method.toString()
        );

    }
    
    Class[] parameterTypes=_method.getParameterTypes();
    
    if (params.length!=parameterTypes.length)
    { 
      throw new IllegalArgumentException
        ("Wrong number of parameters ("+params.length+") for "
        +_method.toString()
        );
    }

    
    Object[] paramValues=new Object[parameterTypes.length];
    try
    { 
      int i=0;
      for (Channel channel:params)
      { 
//        System.out.println("MethodTranslator "+toString()+" translateForGet: parameter["+i+"] "+optic);

        Object paramValue=channel.get();
        if (paramValue==null && parameterTypes[i].isPrimitive())
        { 
          throw new RuntimeException
            ("Can't assign null to primitive parameter at index "+i
            +": invoking method "+_method
            +" on "+value
            );
        }
        paramValues[i++]=paramValue;
        
      }

//      System.out.println
//        ("MethodTranslator invoke: ["+value+"]."
//        +_method.getName()
//        +"("+ArrayUtil.format(paramValues,",","")+")"
//        );
      
      return  _method.newInstance(paramValues);
      
      
    }
    catch (IllegalAccessException x)
    { 
      x.printStackTrace();
      return null;
    }
    catch (InvocationTargetException x)
    { 
      throw new RuntimeException
        ("Error invoking method "+_method.toString()
        +" on ["+value+"] with "
        +"["+ArrayUtil.format(paramValues,",","\"")+"]"
        ,x.getTargetException()
        );
    }
    catch (IllegalArgumentException x)
    {
      throw new RuntimeException
        ("Error invoking method "+_method
        +" on "+value+" with "
        +"["+ArrayUtil.format(paramValues,",","\"")+"]"
        ,x
        );
      
    }
    catch (InstantiationException x)
    {
      throw new RuntimeException
        ("Error running constructor "+_method
        +" with "
        +"["+ArrayUtil.format(paramValues,",","\"")+"]"
        ,x
        );
      
    }
  }

  public Void translateForSet(Tbean val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }
  
  /**
   * A constructor is the antithesis of a Function
   */
  public boolean isFunction()
  { return false;
  }    

  public Reflector<Tbean> getReflector()
  { return _reflector;
  }

  @Override
  public String toString()
  { return super.toString()+":"+_method.toString();
  }
}

