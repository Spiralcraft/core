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

  protected boolean debug;
  
  
  /**
   * Requests that a Buffer be editable. A value of false will prevent the
   *   buffer from being Editable. A value of true will allow the buffer
   *   to be editable as other conditions allow. Defaults to true.
   * 
   * @return Whether this Buffer should be Editable 
   */
  public abstract void setEditable(boolean val);
  
  /**
   * @return Whether this Buffer can be edited 
   */
  public abstract boolean isEditable();
  
  public abstract DataComposite getOriginal();
  
  public abstract boolean isDirty();
  
  /**
   * Provide a permanent id for newly created buffers before they are
   *   persisted beyond the data session.
   * 
   * @param id
   */
  abstract void setId(Identifier id)
    throws DataException;
  
  @Override
  public abstract BufferTuple asTuple();
  
  @Override
  public abstract BufferAggregate<?,?> asAggregate();
  
  /**
   * Discard edited data
   */
  public abstract void revert();

  /**
   * Makes the buffer dirty even if nothing changed
   */
  public abstract void touch();
 
  /**
   * Commit the edits to the data store
   */
  public abstract void save()
    throws DataException;
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
}
