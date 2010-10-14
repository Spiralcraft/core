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

import spiralcraft.data.Type;

/**
 * <p>Represents a persistent Type within a Schema. A Store will map the
 *   Entity definitions in a Schema to storage and retrieval pathways,
 *   eg. tables.
 * </p>
 * 
 * @author mike
 *
 */
public class Entity
{
  private Type<?> type;
  private String name;
  private boolean debug;
  private DeltaTrigger[] deltaTriggers;
  
  public Entity()
  {
  }
  
  public Entity(Type<?> type)
  { this.type=type;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  public void setName(String storeName)
  { this.name=storeName;
  }
  
  public String getName()
  { 
    if (name!=null)
    { return name;
    }
    else
    { return type.getPackageURI().relativize(type.getURI()).getPath();
    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public boolean isDebug()
  { return debug;
  }
  
  /**
   * <p>The Triggers associated with this Entity
   * </p>
   * 
   * @param triggers
   */
  public void setDeltaTriggers(DeltaTrigger[] deltaTriggers)
  { this.deltaTriggers=deltaTriggers;
  }
  
  /**
   * 
   * @return the Triggers associated with this Entity
   */
  public DeltaTrigger[] getDeltaTriggers()
  { return deltaTriggers;
  }
}
