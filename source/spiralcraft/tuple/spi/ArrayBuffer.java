//
// Copyright (c) 1998,2006 Michael Toth
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

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Buffer;
import spiralcraft.tuple.BufferConflictException;

public class ArrayBuffer
  extends ArrayTuple
  implements Buffer
{
  private ArrayTuple original;
  private boolean buffering=true;
  
  public ArrayBuffer(Scheme scheme)
  { super(scheme);
  }
  
  public ArrayBuffer(ArrayTuple original)
  { 
    super(original.getScheme());
    this.original=original;
  }
  
  public synchronized void delete()
    throws BufferConflictException
  { 
    assertBuffer();
    deleted=true;
    commitBuffer();
  }
  
  public synchronized Tuple original()
  { return original;
  }
  
  public boolean isVolatile()
  { return buffering;
  }
  
  public synchronized Buffer createBuffer()
    throws IllegalStateException
  { 
    if (deleted)
    { throw new IllegalStateException("deleted");
    }
    
    if (!buffering)
    { buffering=true;
    }
    return this;
  }

  public synchronized void set(int index,Object value)
  { 
    assertBuffer();
    data[index]=value;
  }
  
  public synchronized Tuple commitBuffer()
    throws BufferConflictException
  { 
    assertBuffer();
    if (original!=null)
    { 
      if (!deleted)
      { original=original.commitBuffer(this);
      }
      else
      { spiDelete();
      }
    }
    buffering=false;
    return original;
  }

  private void assertBuffer()
  { 
    if (!buffering)
    { throw new IllegalStateException("Buffer already committed");
    }
  }
  
}