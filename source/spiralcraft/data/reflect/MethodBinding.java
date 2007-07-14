//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.reflect;

import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.DataException;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.lang.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodBinding
  extends ParameterBinding
{
  private Method method;
  
  public MethodBinding
    (FieldSet source
    ,Class<?> targetClass
    ,String methodName
    ,String ... fieldNames
    )
    throws DataException
  {
    super(source,fieldNames);
    this.method=ClassUtil.getMethod(targetClass,methodName,signature);
    if (this.method==null)
    {
      throw new DataException
        ("Method matching "+methodName+"("
        +ArrayUtil.format(signature,",","")
        +") not found"
        );
    }
    
  }
  
  
  public Object invoke(Object target,Tuple paramSource)
    throws DataException
  { 
    Object[] values=getValues(paramSource);
    Class<?>[] paramTypes=method.getParameterTypes();

    int i=0;
    for (Class<?> clazz: paramTypes)
    { 
      if (values[i]==null && clazz.isPrimitive())
      { values[i]=ClassUtil.primitiveDefault(clazz);
      }
      i++;
    }
    
    try
    { return method.invoke(target, values);
    }
    catch (InvocationTargetException x)
    {
      throw new DataException
        ("Error invoking "+method+" for Tuple '"
        +paramSource+"':"+x.toString()
        ,x
        );
    }
    catch (IllegalAccessException x)
    {
      throw new DataException
      ("Error invoking "+method+" for Tuple '"
      +paramSource+"':"+x.toString()
      ,x
      );
    }
    catch (IllegalArgumentException x)
    {
      throw new DataException
        ("Error invoking "+method+" on ["+target+"] for Tuple '"
        +paramSource+"':"+x.toString()
        ,x
        );
    }
  }
  
}
