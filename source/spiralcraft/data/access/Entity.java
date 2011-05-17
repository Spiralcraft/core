//
// Copyright (c) 1998,2010 Michael Toth
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

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.access.DeltaTrigger;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayUtil;

/**
 * <p>Represents a persistent Type within a Schema. A Store will map the
 *   Entity definitions in a Schema to storage and retrieval pathways,
 *   eg. tables, files.
 * </p>
 * 
 * @author mike
 *
 */
public class Entity
  extends SchemaMetaObject<Entity>
{
  private Type<?> type;
  private String name;
  private DeltaTrigger[] deltaTriggers;
  private DeltaTrigger[] allDeltaTriggers;
  
  private LinkedHashMap<String,EntityField> fields
    =new LinkedHashMap<String,EntityField>();
  private EntityField[] allFields;

  private Schema schema;
  
  private Level logLevel=Level.INFO;
  private boolean isAbstract;
  
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
  
  public boolean isAbstract()
  { return isAbstract;
  }
  
  /**
   * Abstract Entities exist only to provide attributes to for
   *   subtypes, and cannot be queried or stored directly.
   * 
   * @param isAbstract
   */
  public void setAbstract(boolean isAbstract)
  { this.isAbstract=isAbstract;
  }
  
  
  public void setDebug(boolean debug)
  { 
    if (debug)
    { 
      if (!logLevel.isDebug())
      { logLevel=Level.DEBUG;
      }
    }
    else
    { 
      if (logLevel.isDebug())
      { logLevel=Level.INFO;
      }
    }
  }
  
  public boolean isDebug()
  { return logLevel.isDebug();
  }
  
  public Level getLogLevel()
  { return logLevel;
  }
  
  public void setLogLevel(Level logLevel)
  { 
    if (logLevel==null)
    { throw new IllegalArgumentException("logLevel cannot be null");
    }
    this.logLevel=logLevel;
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
  { 
    return allDeltaTriggers;
  }

  public void addDeltaTrigger(DeltaTrigger dt)
  { 
    if (this.allDeltaTriggers!=null)
    { this.allDeltaTriggers=ArrayUtil.append(this.allDeltaTriggers,dt);
    }
    else
    { this.allDeltaTriggers=new DeltaTrigger[]{dt};
    }
  }
  
  public void setFields(EntityField[] fields)
  { 
    for (EntityField field:fields)
    { this.fields.put(field.getName(),field);
    }
  }
  
  public EntityField[] getFields()
  { return this.allFields;
  }
  
  /**
   * @return the Entity which governs a base type of this Entity's type,
   *   skipping base types that have no corresponding Entity definition
   * 
   */
  public Entity getBaseTypeEntity()
  {
    Type<?> baseType=type.getBaseType();
    Entity entity=null;
    while (baseType!=null && entity==null)
    { 
      entity=schema.getEntity(baseType);
      baseType=baseType.getBaseType();
    }
    return entity;
  }
  
  void setSchema(Schema schema)
  { this.schema=schema;
  }
  
  @Override
  void resolve()
    throws DataException
  {
    ArrayList<EntityField> allFields=new ArrayList<EntityField>();
    if (base!=null)
    {
      allDeltaTriggers=
        ArrayUtil.concat(base.getDeltaTriggers(),deltaTriggers);

      for (EntityField field:base.getFields())
      { 
        EntityField extendedField=fields.get(field.getName());
        if (extendedField!=null)
        { 
          extendedField.setExtends(field);
          allFields.add(extendedField);
        }
        else
        { allFields.add(field);
        }
        
      }
    }
    else
    { allDeltaTriggers=deltaTriggers;
    }

    
    for (EntityField field:fields.values())
    { 
      if (field.base==null)
      { allFields.add(field);
      }
      field.setEntity(this);
      field.resolve();
    
    }
    this.allFields=allFields.toArray(new EntityField[allFields.size()]);
    super.resolve();
  }  
}
