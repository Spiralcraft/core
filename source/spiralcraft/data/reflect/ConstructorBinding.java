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
import java.lang.reflect.Constructor;

public class ConstructorBinding
  extends ParameterBinding
{
  private Constructor constructor;
  
  public ConstructorBinding
    (FieldSet source
    ,Class targetClass
    ,String ... fieldNames
    )
    throws DataException
  {
    super(source,fieldNames);
    this.constructor=ClassUtil.getConstructor(targetClass,signature);
    if (this.constructor==null)
    {
      throw new DataException
        ("Constructor matching ("
        +ArrayUtil.format(signature,",","")
        +") not found"
        );
    }
    
  }
  
  
  public Object newInstance(Tuple paramSource)
    throws DataException
  { 
    Object[] values=getValues(paramSource);
    Class[] paramTypes=constructor.getParameterTypes();

    int i=0;
    for (Class clazz: paramTypes)
    { 
      if (values[i]==null && clazz.isPrimitive())
      { values[i]=ClassUtil.primitiveDefault(clazz);
      }
      i++;
    }

    try
    { return constructor.newInstance(values);
    }
    catch (InstantiationException x)
    {
      throw new DataException
        ("Error invoking constructor for Tuple '"
        +paramSource+"':"+x.toString()
        ,x
        );
    }
    catch (InvocationTargetException x)
    {
      throw new DataException
        ("Error invoking constructor for Tuple '"
        +paramSource+"':"+x.toString()
        ,x
        );
    }
    catch (IllegalAccessException x)
    {
      throw new DataException
      ("Error invoking constructor for Tuple '"
      +paramSource+"':"+x.toString()
      ,x
      );
    }
  }
  
}
