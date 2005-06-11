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

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Optic;
import spiralcraft.lang.BindException;

import java.lang.reflect.Field;

class BeanFieldLense
  implements Lense
{
  private final Field _field;
  private final Prism _prism;
  
  public BeanFieldLense(Field field)
    throws BindException
  { 
    _field=field;
    _prism=OpticFactory.getInstance().findPrism(_field.getType());
  }

  public Field getField()
  { return _field;
  }

  public Object translateForGet(Object value,Optic[] modifiers)
  { 
    try
    { return _field.get(value);
    }
    catch (IllegalAccessException x)
    { return null;
    }
  }

  public Object translateForSet(Object val,Optic[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Prism getPrism()
  { return _prism;
  }

}

