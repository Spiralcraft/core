//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.spi;

import java.lang.ref.WeakReference;

import spiralcraft.data.Identifier;
import spiralcraft.data.Type;

/**
 * Identifies by Java identity, using System.identityHashCode
 *   to provide a stable HashCode
 * 
 * @author mike
 *
 */
public class PojoIdentifier<T>
  implements Identifier
{
  
  private WeakReference<T> instance;
  private int hashCode;
  
  public boolean instanceIs(T other)
  { return other==instance.get();
  }

  public PojoIdentifier(T instance)
  { 
    this.instance=new WeakReference<T>(instance);
    hashCode=System.identityHashCode(instance);
  }
  
  public PojoIdentifier(PojoIdentifier<T> copy)
  { 
    this.instance=copy.instance;
    this.hashCode=copy.hashCode;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(
    Object identifier)
  {
    if (!(identifier instanceof PojoIdentifier))
    { return false;
    }
    else
    { return ((PojoIdentifier<T>) identifier).instanceIs(instance.get());
    }

  }

  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public Type<?> getIdentifiedType()
  {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public boolean isPublic()
  { return false;
  }

}
