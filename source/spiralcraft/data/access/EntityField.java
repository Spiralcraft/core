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

import spiralcraft.common.ContextualException;
import spiralcraft.common.attributes.AbstractAttributeContext;
import spiralcraft.data.Field;
import spiralcraft.data.FieldNotFoundException;

/**
 * Specifies persistence related characteristics of a Field as associated
 *   with a Schema Entity.
 * 
 * @author mike
 *
 */
public class EntityField
  extends AbstractAttributeContext<EntityField>
{

  private String name;
  private String storeName;
  private Entity entity;
  
  
  public void setName(String name)
  { this.name=name;
  }
  
  public void setStoreName(String storeName)
  { this.storeName=storeName;
  }
  
  public String getName()
  { return name;
  }
  
  public String getStoreName()
  { 
    if (storeName!=null)
    { return storeName;
    }

    if (base!=null)
    { return base.getStoreName();
    }
    else
    { return name;
    }
  }
  
  @Override
  public void resolve()
    throws ContextualException
  { 
    super.resolve();
    if (getTypeField()==null)
    { throw new FieldNotFoundException(entity.getType(),name);
    }
  }
  
  public Field<?> getTypeField()
  { return entity.getType().getField(name);
  }
  
  void setEntity(Entity entity)
  { this.entity=entity;
  }
}
