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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;

public class NamespaceChannel<T>
  extends TranslatorChannel<T,Namespace>
{

  private final NamespaceAttribute<T> translator;
  
  public NamespaceChannel
    (Channel<Namespace> source
    ,NamespaceAttribute<T> translator
    )
  { 
    super(source,translator,null);
    this.translator=translator;
  }

  
  @SuppressWarnings("unchecked") // Source is heterogeneous
  @Override
  public boolean set(T val)
    throws AccessException
  { 
    if (_constant)
    { return false;
    }
    ((Channel<T>) source.get().getOptic(translator.getIndex())).set(val);
    return true;
  }
}


