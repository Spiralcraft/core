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

import java.lang.reflect.Field;

class BeanFieldTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{
  private final Field _field;
  private final Reflector<Tprop> _reflector;
  
  public BeanFieldTranslator(Field field)
  { 
    _field=field;
    _reflector=BeanReflector.<Tprop>getInstance(_field.getType());
  }

  public Field getField()
  { return _field;
  }

  @SuppressWarnings("unchecked") // Field is not generic
  public Tprop translateForGet(Tbean value,Channel<?>[] modifiers)
  { 
    try
    { return (Tprop) _field.get(value);
    }
    catch (IllegalAccessException x)
    { return null;
    }
    catch (NoClassDefFoundError x)
    { throw new Error("Error reading field "+_field,x);
    }
  }

  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  public Reflector<Tprop> getReflector()
  { return _reflector;
  }
  
  /**
   * Bean fields are mutable
   * 
   */
  public boolean isFunction()
  { 
    // TODO: When "final", field is not mutable
    return false;
  }
}

