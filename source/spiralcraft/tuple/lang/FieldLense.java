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
package spiralcraft.tuple.lang;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;

public class FieldLense
  implements Lense
{
  private final Field _field;
  private final Prism _prism;
  
  public FieldLense(Field field)
    throws BindException
  { 
    _field=field;

    if (_field.getType().getScheme()!=null)
    { 
      // Re-use the SchemePrisms- weak map?
      _prism
        =new SchemePrism(_field.getType().getScheme());
    }
    else
    {
      _prism=
        OpticFactory.getInstance().findPrism
          (_field.getType().getJavaClass());
    }
  }

  public Field getField()
  { return _field;
  }
  
  public Object translateForGet(Object value,Optic[] modifiers)
  { return ((Tuple) value).get(_field.getIndex());
  }

  public Object translateForSet(Object val,Optic[] modifiers)
  { 
    // We can't turn the value into a Tuple
    throw new UnsupportedOperationException();
  }
    
  public Prism getPrism()
  { return _prism;
  }
}
