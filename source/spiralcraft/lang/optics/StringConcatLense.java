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

import spiralcraft.lang.Optic;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;

public class StringConcatLense
  implements Lense<String,String>
{
  private final Prism<String> _prism;
  
  public StringConcatLense(Prism<String> prism)
  { _prism=prism;
  }
  
  public Prism<String> getPrism()
  { return _prism;
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public String translateForGet(String source,Optic[] modifiers)
  { 
    String modifier=((Optic<String>)modifiers[0]).get();
    if (modifier==null)
    { return source;
    }
    if (source==null)
    { return modifier;
    }
    return source.concat(modifier);
  }

  @SuppressWarnings("unchecked") // Upcast for expected modifiers
  public String translateForSet(String string,Optic[] modifiers)
  { 
    String concat=((Optic<String>) modifiers[0]).get();
    if (concat==null)
    { return string;
    }
    if (string.endsWith(concat))
    { return string.substring(0,string.indexOf(concat));
    }
    return null;
  }


}
