//
// Copyright (c) 2014 Michael Toth
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
import spiralcraft.lang.spi.Translator;
import spiralcraft.util.ArrayUtil;




class MethodKey
{
  private final Object instanceSig[];

  public MethodKey(Translator<?,?> translator,Channel<?>[] params)
  {
    instanceSig=new Object[params.length+1];
    instanceSig[0]=translator;
    for (int i=1;i<instanceSig.length;i++)
    { instanceSig[i]=params[i-1].getReflector();
    }
  }
  
  @Override
  public boolean equals(Object o)
  {
    if (o instanceof Object[])
    { return ArrayUtil.arrayEquals(instanceSig,(Object[]) o);
    }
    else
    { return false;
    }
  }
  
  @Override
  public int hashCode()
  { return ArrayUtil.arrayHashCode(instanceSig);
  }

}