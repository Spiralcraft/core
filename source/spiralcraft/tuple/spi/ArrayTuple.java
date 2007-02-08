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
package spiralcraft.tuple.spi;

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Buffer;
import spiralcraft.tuple.TupleId;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.BufferConflictException;

/**
 * A Tuple which stores its data in an Array
 */
public class ArrayTuple
  implements Tuple
{
  protected final Scheme scheme;
  protected boolean deleted;
  protected final Object[] data;
  private ArrayTuple _nextVersion;
  
  public ArrayTuple(Scheme scheme)
  { 
    this.scheme=scheme;
    data=new Object[scheme.getFields().size()];
  }
  
  /**
   * Create a new Tuple from a buffer commit
   */
  ArrayTuple(ArrayBuffer buffer)
  { 
    this(buffer.getScheme());
    for (Field field : scheme.getFields())
    { data[field.getIndex()]=buffer.get(field.getIndex());
    }
  }
  
  public Scheme getScheme()
  { return scheme;
  }
  
  public TupleId getId()
  { return null;
  }
  
  public Object get(int index)
  { return data[index];
  }
  
  public synchronized Buffer createBuffer()
  { 
    if (deleted)
    { throw new IllegalStateException("deleted");
    }
    return new ArrayBuffer(this);
  }
    
  public synchronized Tuple currentVersion()
  { 
    if (deleted)
    { return null;
    }
    
    if (_nextVersion!=null)
    { return _nextVersion.currentVersion();
    }
    return this;
  }
  
  public Tuple nextVersion()
  { return _nextVersion;
  }
  
  public boolean isDeletedVersion()
  { return deleted;
  }

  synchronized void spiDelete()
    throws BufferConflictException
  { 
    if (_nextVersion!=null)
    { throw new BufferConflictException(_nextVersion);
    }
    deleted=true;
  }

  synchronized ArrayTuple commitBuffer(ArrayBuffer buffer)
    throws BufferConflictException
  {
    if (_nextVersion!=null)
    { throw new BufferConflictException(_nextVersion);
    }
    if (deleted)
    { throw new BufferConflictException("deleted");
    }
    _nextVersion=new ArrayTuple(buffer);
    return _nextVersion;
  }
  
}

