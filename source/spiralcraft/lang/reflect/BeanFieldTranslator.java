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
import java.lang.reflect.Modifier;

class BeanFieldTranslator<Tprop,Tbean>
  implements Translator<Tprop,Tbean>
{
  private final Field _field;
  private final Reflector<Tprop> _reflector;
  private final boolean _staticField;
  
  public BeanFieldTranslator(Field field)
  { 
    _field=field;
    _reflector=BeanReflector.<Tprop>getInstance(_field.getType());
    _staticField=Modifier.isStatic(_field.getModifiers());
  }

  public Field getField()
  { return _field;
  }

  @Override
  @SuppressWarnings("unchecked") // Field is not generic
  public Tprop translateForGet(Tbean value,Channel<?>[] modifiers)
  { 
    if (value==null && !_staticField)
    { return null;
    }
    
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

  @Override
  public Tbean translateForSet(Tprop val,Channel<?>[] modifiers)
  { throw new UnsupportedOperationException();
  }

  @Override
  public Reflector<Tprop> getReflector()
  { return _reflector;
  }
  
  /**
   * Bean fields are mutable
   * 
   */
  @Override
  public boolean isFunction()
  { 
    // TODO: When "final", field is not mutable
    return false;
  }
}

