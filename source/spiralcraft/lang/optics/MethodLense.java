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
package spiralcraft.lang.optics;


import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

// import spiralcraft.util.ArrayUtil;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


class MethodLense<Tprop,Tbean>
  implements Lense<Tprop,Tbean>
{

  private final Method _method;
  private final Prism<Tprop> _prism;
  
  @SuppressWarnings("unchecked") // Method is not parameterized
  public MethodLense(Method method)
    throws BindException
  { 
    _method=method;
    _prism=OpticFactory.getInstance().<Tprop>findPrism((Class<Tprop>)method.getReturnType());
  }

  public Method getMethod()
  { return _method;
  }

  @SuppressWarnings("unchecked") // Method is not generic
  public Tprop translateForGet(Tbean value,Optic[] params)
  { 
//    System.out.println("MethodLense "+toString()+" translateForGet: ["+value+"]");
    
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
      for (Optic optic:params)
      { 
//        System.out.println("MethodLense "+toString()+" translateForGet: parameter["+i+"] "+optic);

        paramValues[i++]=optic.get();
      }

//      System.out.println
//        ("MethodLense invoke: ["+value+"]."
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

  public Tbean translateForSet(Tprop val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Prism<Tprop> getPrism()
  { return _prism;
  }

  public String toString()
  { return super.toString()+":"+_method.toString();
  }
}

