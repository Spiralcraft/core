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
package spiralcraft.data.wrapper;

import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;

/** 
 * Base Class for Objects which use a Tuple for their instance data.
 * 
 * It is the responsibility of subclasses to map property accessors
 *   to field indexes.
 */
public abstract class TupleWrapper
{
  private final Scheme scheme;
  private Tuple tuple;
  private boolean editable;
    
  /**
   * Create a TupleWrapper for Tuples of the specific Scheme
   */
  protected TupleWrapper(Scheme scheme)
  { this.scheme=scheme;
  }
  
  /**
   * Associate a Tuple with the wrapper object
   */
  public synchronized void dataSetTuple(Tuple t)
  { 
    tuple=t;
    if (t!=null)
    {
      if (t.getScheme()!=scheme)
      { throw new IllegalArgumentException("Tuple does not match Scheme");
      }
      editable=t.isMutable();
    }
    else
    { editable=false;
    }
  }
  
  /**
   *@return tuple.get(index)
   */
  protected Object dataGet(int index)
  {
    assertTuple();
    return tuple.get(index);
  }
  
  /**
   * Calls tuple.set(index,value), if the Tuple is editable
   */
  protected void dataSet(int index,Object value)
    throws DataException
  {
    assertTuple();
    if (editable)
    { ((EditableTuple) tuple).set(index,value);
    }
    else
    { throw new IllegalStateException("Tuple is read-only");
    }
  }
  
  /**
   * Ensure that the tuple is set
   */
  private void assertTuple()
  {
    if (tuple==null)
    { throw new IllegalStateException("No Tuple set");
    }
  }
}