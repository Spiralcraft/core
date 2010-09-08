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

import spiralcraft.data.Identifier;
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
  
  @Override
  public Object get(String fieldName)
    throws DataException
  { return delegate.get(fieldName);
  }
  
  @Override
  public Identifier getId()
  { return delegate.getId();
  }
  
  @Override
  public Tuple widen(Type<?> type)
    throws DataException
  { return new ReadOnlyFascadeTuple(delegate.widen(type));
  }
  
  @Override
  public Tuple getBaseExtent()
  { return new ReadOnlyFascadeTuple(delegate.getBaseExtent());
  }
  
  @Override
  public Type<?> getType()
  { return delegate.getType();
  }
  
  @Override
  public boolean isTuple()
  { return true;
  }
  
  @Override
  public Tuple asTuple()
  { return this;
  }
  
  @Override
  public boolean isAggregate()
  { return false;
  }
  
  @Override
  public Aggregate<?> asAggregate()
  { throw new UnsupportedOperationException("Not an Aggregate");
  }
 
  @Override
  public FieldSet getFieldSet()
  { return delegate.getFieldSet();
  }
  
  @Override
  public Object get(int index)
    throws DataException
  { return delegate.get(index);
  }
  
  @Override
  public String toText(String indent)
    throws DataException
  { return delegate.toText(indent);
  }
  
  @Override
  public boolean isMutable()
  { return delegate.isMutable();
  }
  
  @Override
  public boolean isVolatile()
  { return delegate.isVolatile();
  }
  
  @Override
  public Tuple snapshot()
    throws DataException
  { return delegate.snapshot();
  }
  
  @Override
  public boolean equals(Object o)
  { return delegate.equals(o);
  }
  
  @Override
  public int hashCode()
  { return delegate.hashCode();
  }
  
  @Override
  public String dumpData()
    throws DataException
  { return delegate.dumpData();
  }
  
  @Override
  public Object getBehavior()
    throws DataException
  { return delegate.getBehavior();
  }
}