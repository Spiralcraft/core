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
import java.util.LinkedHashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.common.attributes.AbstractAttributeContext;
import spiralcraft.data.Type;


/**
 * A collection of inter-related Entities. A Schema is implemented by a Store
 *   which provides access to the data.
 */
public class Schema
  extends AbstractAttributeContext<Schema>
{
  private final LinkedHashMap<Type<?>,Entity> entities
    =new LinkedHashMap<Type<?>,Entity>();
  
  private Entity[] combinedEntities;
  private String name;
  
  public void setName(String name)
  { this.name=name;
  }
  
  public String getName()
  {
    if (name!=null)
    { return name;
    }
    else if (base!=null)
    { return base.getName();
    }
    else
    { return null;
    }
  }
  
  
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
      entities.put(type,table);
    }
    
  }


  
  /**
   * 
   * @return the Entities associated with this Schema
   */
  public Entity[] getEntities()
  { 
    return combinedEntities;
    
  }

  /**
   * 
   * @param entities The Entities associated with this Schema
   */
  public void setEntities(Entity[] entities)
  { 
    this.entities.clear();
    for (Entity entity:entities)
    { this.entities.put(entity.getType(),entity);
    }
  }
  
  public Entity getEntity(Type<?> type)
  { 
    Entity ret=entities.get(type);
    if (ret==null && base!=null)
    { ret=base.getEntity(type);
    }
    return ret;
  }
  
  

  
  @Override
  public void resolve()
    throws ContextualException
  { 
    ArrayList<Entity> allEntities
      =new ArrayList<Entity>();
    if (getBase()!=null)
    {
      resolveBase();
      for (Entity entity: base.getEntities())
      { 
        Entity extension=entities.get(entity.getType());
        if (extension!=null)
        { 
          extension.setExtends(entity);
          allEntities.add(extension);
        }
        else
        { 
          // Make sure inherited entities can resolve base type entities
          //   in this Schema.
          entity.setSchema(this);
          allEntities.add(entity);
        }
      }
    }

    for (Entity entity:entities.values())
    { 
      if (entity.getBase()==null)
      { 
        // Add remaining entities at the end
        allEntities.add(entity);
      }
      entity.setSchema(this);
      entity.resolve();
    }
    this.combinedEntities=allEntities.toArray(new Entity[allEntities.size()]);
    super.resolve();

  }
  
}
