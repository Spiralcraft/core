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

import spiralcraft.data.Type;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;

/**
 * A read-only fascade for a Tuple, designed to enforce the hiding of
 *   the Tuple implementation.
 */
public class ReadOnlyFascadeTuple
  implements Tuple
{
 
  private final Tuple delegate;
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public ReadOnlyFascadeTuple(Tuple delegate)
  { this.delegate=delegate;
  }
  
  public Tuple widen(Type type)
    throws DataException
  { return new ReadOnlyFascadeTuple(delegate.widen(type));
  }
  
  public Type<?> getType()
  { return delegate.getType();
  }
  
  public boolean isTuple()
  { return true;
  }
  
  public Tuple asTuple()
  { return this;
  }
  
  public boolean isAggregate()
  { return false;
  }
  
  public Aggregate asAggregate()
  { throw new UnsupportedOperationException("Not an Aggregate");
  }
 
  public FieldSet getFieldSet()
  { return delegate.getFieldSet();
  }
  
  public Object get(int index)
  { return delegate.get(index);
  }
  
  public String toText(String indent)
    throws DataException
  { return delegate.toText(indent);
  }
  
  public boolean isMutable()
  { return delegate.isMutable();
  }
  
  public boolean equals(Object o)
  { return delegate.equals(o);
  }
  
  public int hashCode()
  { return delegate.hashCode();
  }
}