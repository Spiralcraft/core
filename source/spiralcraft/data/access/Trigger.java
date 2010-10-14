//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.data.access;

import spiralcraft.lang.Contextual;


/**
 * <p>Specifies a Task to be performed at a point in the data modification
 *   process.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class Trigger
  implements Contextual
{
  public static enum When
  { 
    BEFORE
    ,AFTER
  }
   
  private String name;
  private When when;
  private boolean debug;
  
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  public void setWhen(When when)
  { this.when=when;
  }
  
  public When getWhen()
  { return when;
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public boolean isDebug()
  { return debug;
  }
}
