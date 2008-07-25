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
package spiralcraft.lang.spi;


import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;

// import spiralcraft.util.ArrayUtil;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


class MethodTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{

  private final Method _method;
  private final Reflector<Tprop> _reflector;
  
  public MethodTranslator(Method method)
    throws BindException
  { 
    _method=method;
    _reflector=BeanReflector.<Tprop>getInstance
      (method.getGenericReturnType());
  }

  public Method getMethod()
  { return _method;
  }

  @SuppressWarnings("unchecked") // Method is not generic
  public Tprop translateForGet(Tbean value,Channel<?>[] params)
  { 
//    System.out.println("MethodTranslator "+toString()+" translateForGet: ["+value+"]");
    
    if (params==null)
    { throw new IllegalArgumentException
        ("No parameters for "
        +_method.toString()
        );

    }
    
    if (params.length!=_method.getParameterTypes().length)
    { 
      throw new IllegalArgumentException
        ("Wrong number of parameters ("+params.length+") for "
        +_method.toString()
        );
    }
    if (value==null)
    { return null;
    }
    try
    { 
      Object[] paramValues=new Object[params.length];
      int i=0;
      for (Channel channel:params)
      { 
//        System.out.println("MethodTranslator "+toString()+" translateForGet: parameter["+i+"] "+optic);

        paramValues[i++]=channel.get();
      }

//      System.out.println
//        ("MethodTranslator invoke: ["+value+"]."
//        +_method.getName()
//        +"("+ArrayUtil.format(paramValues,",","")+")"
//        );
      
      return (Tprop) _method.invoke(value,paramValues);
      
    }
    catch (IllegalAccessException x)
    { 
      x.printStackTrace();
      return null;
    }
    catch (InvocationTargetException x)
    { 
      throw new RuntimeException("Error invoking method:",x.getTargetException());
    }
  }

  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Reflector<Tprop> getReflector()
  { return _reflector;
  }

  public String toString()
  { return super.toString()+":"+_method.toString();
  }
}

