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
import spiralcraft.data.DataException;



import java.net.URI;

public abstract class Buffer
  implements DataComposite
{
  public static final URI FOCUS_URI
    =URI.create("class:/spiralcraft/data/session/Buffer");

/*  
  private  ArrayList<Buffer> children=new ArrayList<Buffer>();
  private WeakReference<Buffer> parentRef;
*/
/*  
  void addChild(Buffer buffer)
  { children.add(buffer);
  }
  
  
  void removeChild(Buffer buffer)
  { children.remove(buffer);
  }
    
  void setParent(Buffer buffer)
  { parentRef=new WeakReference<Buffer>(buffer);
  }
  
  Buffer getParent()
  { return parentRef.get();
  }
*/  
  /**
   * Provide a permanent id for newly created s before they are
   *   persisted beyond the data session.
   * 
   * @param id
   */
  abstract void setId(Identifier id)
    throws DataException;
  
  @Override
  public abstract BufferTuple asTuple();
  
  @Override
  public abstract BufferAggregate<?> asAggregate();
  
  public abstract void revert();

}
