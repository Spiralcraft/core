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
package spiralcraft.data.lang;

import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;

import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.Translator;

public class FieldTranslator
  implements Translator<Object,Object>
{
  private final Field field;
  private final Reflector<Object> reflector;
  
  public FieldTranslator(Field field)
    throws BindException
  { 
    this.field=field;
    reflector=DataReflector.getInstance(field.getType());
  }

  public Field getField()
  { return field;
  }
  
  public Object translateForGet(Object value,Channel<?>[] modifiers)
  { 
    if (value==null)
    { 
      // Null input results in null output
      return null;
    }
    
    try
    { return field.getValue((Tuple) value);
    }
    catch (DataException x)
    { throw new IllegalArgumentException("Field is not valid for Tuple",x);
    }
  }

  public Object translateForSet(Object val,Channel<?>[] modifiers)
  { 
    // We can't turn the value into a Tuple, lacking access to
    //   any kind of lookup context
    throw new UnsupportedOperationException();
  }
    
  public Reflector<Object> getReflector()
  { return reflector;
  }
}
