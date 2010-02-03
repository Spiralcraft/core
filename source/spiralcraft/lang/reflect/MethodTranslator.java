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
import spiralcraft.util.lang.ClassUtil;

// import spiralcraft.util.ArrayUtil;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;


class MethodTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{

  private final Method _method;
  private final Reflector<Tprop> _reflector;
  private final boolean _staticMethod;
  
  public MethodTranslator(Method method)
  { 
    _method=method;
    _staticMethod=Modifier.isStatic(method.getModifiers());
    Type returnType=method.getGenericReturnType();
    Class<?> clazz=ClassUtil.getClass(returnType);
    if (clazz!=null)
    { _reflector=BeanReflector.<Tprop>getInstance(clazz);
    }
    else
    { _reflector=BeanReflector.getInstance(Object.class);
    }
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
    
    Class[] parameterTypes=_method.getParameterTypes();
    
    if (params.length!=parameterTypes.length)
    { 
      throw new IllegalArgumentException
        ("Wrong number of parameters ("+params.length+") for "
        +_method.toString()
        );
    }
    
    if (value==null && !_staticMethod)
    { return null;
    }
    
    Object[] paramValues=new Object[params.length];
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
      
      return (Tprop) _method.invoke(value,paramValues);
      
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
        +" on "+value+(value!=null?" ("+value.getClass()+")":"")
        +" with "
        +"["+ArrayUtil.format(paramValues,",","\"")+"]"
        ,x
        );
      
    }
  }

  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Reflector<Tprop> getReflector()
  { return _reflector;
  }

  @Override
  public String toString()
  { return super.toString()+":"+_method.toString();
  }
}

