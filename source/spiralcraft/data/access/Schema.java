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

import java.util.ArrayList;

import spiralcraft.data.Type;


/**
 * A collection of inter-related Entities. A Schema is implemented by a Store
 *   which provides access to the data.
 */
public class Schema
{
  private final ArrayList<Entity> entities=new ArrayList<Entity>();
  
  
  /**
   * <p>Specify the set of Types that represent first class Entities.</p>
   * 
   * <p>Creates an Entity with a default configuration for each specified
   *   Type.
   * </p>
   *  
   * @param types
   */
  public void setTypes(Type<?>[] types)
  { 
    for (Type<?> type : types)
    { 
      Entity table=new Entity();
      table.setType(type);
      entities.add(table);
    }
    
  }


  
  /**
   * 
   * @return the Entities associated with this Schema
   */
  public Entity[] getEntities()
  { return entities.toArray(new Entity[entities.size()]);
  }

  /**
   * 
   * @param entities The Entities associated with this Schema
   */
  public void setEntities(Entity[] entities)
  { 
    this.entities.clear();
    for (Entity entity:entities)
    { this.entities.add(entity);
    }
  }
}
