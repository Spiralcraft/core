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
package spiralcraft.data.core;

import spiralcraft.lang.BindException;
import spiralcraft.lang.reflect.BeanReflector;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.core.FieldImpl;

/**
 * <p>A Field that holds a Bean 
 * </p>
 * 
 * @author mike
 *
 * @param <T> The Bean type
 */
public class BeanField<T>
  extends FieldImpl<T>
{
  
  public BeanField()
  { 
  }
  
  @Override
  public void setType(Type<T> type)
    throws DataException
  {
    super.setType(type);
    try
    { contentReflector=BeanReflector.getInstance(type.getNativeClass());
    }
    catch (BindException x)
    { 
      throw new DataException
        ("Could not find spiralcraft.lang.reflect.BeanReflector for Class "
        +type.getNativeClass().getName()
        ,x
        );
    }    
    
  }
  
}