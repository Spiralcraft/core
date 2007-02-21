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

import spiralcraft.lang.WriteException;


import java.lang.reflect.Field;

public class BeanFieldBinding
  extends LenseBinding
{

  private final Field _field;

  public BeanFieldBinding
    (Binding source
    ,BeanFieldLense lense
    )
  {
    super(source,lense,null);
    _field=lense.getField();
  }



  public boolean isStatic()
  { return false;
  }

  public synchronized boolean set(Object val)
    throws WriteException
  {
    try
    { 
      _field.set(getSourceValue(),val);
      return true;
    }
    catch (IllegalAccessException x)
    { 
      throw new WriteException
        (x.toString()+" writing bean field '"+_field.getName()+"'",x);
    }
  }

  public String toString()
  { 
    return super.toString()
      +":"+super.toString()
      +":[field="+_field.getName()+" ("+_field.getType()+")]";
  }

}


