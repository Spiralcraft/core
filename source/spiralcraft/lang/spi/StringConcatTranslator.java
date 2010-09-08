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
package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.Translator;

public class StringConcatTranslator
  implements Translator<String,String>
{
  private final Reflector<String> _reflector;
  
  public StringConcatTranslator(Reflector<String> reflector)
  { _reflector=reflector;
  }
  
  @Override
  public Reflector<String> getReflector()
  { return _reflector;
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Upcast for expected modifiers
  public String translateForGet(String source,Channel[] modifiers)
  { 
    Object omodifier=((Channel<Object>)modifiers[0]).get();
    String modifier=omodifier!=null?omodifier.toString():null;
    if (modifier==null)
    { return source;
    }
    if (source==null)
    { return modifier;
    }
    return source.concat(modifier);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Upcast for expected modifiers
  public String translateForSet(String string,Channel[] modifiers)
  { 
    String concat=((Channel<String>) modifiers[0]).get();
    if (concat==null)
    { return string;
    }
    if (string.endsWith(concat))
    { return string.substring(0,string.indexOf(concat));
    }
    return null;
  }

  /**
   * Strings are immutable, and concatenating them is a Function
   */
  @Override
  public boolean isFunction()
  { return true;
  }

}
