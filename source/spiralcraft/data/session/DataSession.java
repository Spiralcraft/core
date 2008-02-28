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
package spiralcraft.data.session;


import spiralcraft.data.DataComposite;
import spiralcraft.data.Identifier;
import spiralcraft.data.Type;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.PojoIdentifier;

import java.util.HashMap;

import java.net.URI;

/**
 * Represents the state of a data modification session that holds a
 *   transactional unit of work. 
 *
 * @author mike
 */
public class DataSession
{
  public static final URI FOCUS_URI
    =URI.create("class:/spiralcraft/data/session/DataSession");
  
  private HashMap<Identifier,Buffer> buffers;  
  private DataComposite data;
  private Type<DataComposite> type;
  
  public void setType(Type<DataComposite> type)
  { this.type=type;
  }

  public Type<DataComposite> getType()
  { return type;
  }
  
  public DataComposite getData()
  { 
    if (data==null)
    { data=new EditableArrayTuple(type.getScheme());
    }
    return data;
  }
  
  /**
   * <p>Obtain a Buffer for the specified DataComposite. If an appropriate
   *   buffer is not found, create one and cache it in the session.
   * </p>
   * 
   * @param composite
   * @return
   */
  @SuppressWarnings("unchecked")
  public synchronized Buffer buffer(DataComposite composite)
  {
    if (buffers==null)
    { buffers=new HashMap<Identifier,Buffer>();
    }
    
    Identifier id=composite.getId();
    if (id==null)
    { id=new PojoIdentifier(composite);
    }
    Buffer buffer=buffers.get(id);
    if (buffer==null)
    { 
      if (composite.isTuple())
      { buffer=new BufferTuple(this,composite.asTuple());
      }
      else if (composite.isAggregate())
      { buffer=new BufferAggregate(this,composite.asAggregate());
      }
      else
      { 
        // Consider a Reference type
        throw new IllegalArgumentException("DataComposite not recognized");
      }
      buffers.put(composite.getId(), buffer);
    }
    return buffer;
    
    
  }
  
  /**
   * Remove the specified buffer from being cached, once the buffer has
   *   reverted, or it is known that it will not be used.
   *   
   * @param buffer
   * @param composite
   */
  synchronized void release(Buffer buffer,Identifier id)
  {
    if (buffers.get(id)==buffer)
    { buffers.remove(id);
    }
  }
  
}  
  
  
  
  
  
  

