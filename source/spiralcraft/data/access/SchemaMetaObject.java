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

import spiralcraft.data.DataException;

/**
 * <p>Base for objects that represent extensible Schema components- Schema, 
 *   Entity, EntityField, and SchemaAttribute.
 * </p>
 * 
 * <p>Provides support for multiple layers of inheritance to allow
 *   customization for progressively specific implementation scenarios. 
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public abstract class SchemaMetaObject<T extends SchemaMetaObject<?>>
{
  protected T base;

  private final LinkedHashMap<Class<?>,SchemaAttribute> definedAttributes
    =new LinkedHashMap<Class<?>,SchemaAttribute>();
  
  private SchemaAttribute[] combinedAttributes;
  
  
  public void setExtends(T base)
  { this.base=base;
  }
  
  
  /**
   * 
   * @return the Entities associated with this Schema
   */
  public SchemaAttribute[] getAttributes()
  { 
    return combinedAttributes;
    
  }

  /**
   * 
   * @param entities The Entities associated with this Schema
   */
  public void setAttributes(SchemaAttribute[] attributes)
  { 
    this.definedAttributes.clear();
    for (SchemaAttribute attribute:attributes)
    { this.definedAttributes.put(attribute.getClass(),attribute);
    }
  }
  
  @SuppressWarnings("unchecked")
  public <A extends SchemaAttribute> A getAttribute(Class<?> clazz)
  { 
    A ret=(A) definedAttributes.get(clazz);
    if (ret==null && base!=null)
    { ret=base.<A>getAttribute(clazz);
    }
    return ret;
  }
  
  protected SchemaAttribute[] getInheritedAttributes()
  {
    if (base!=null)
    { return base.getAttributes();
    }
    else
    { return new SchemaAttribute[0];
    }
  }
  
  void resolve()
    throws DataException
  {
    ArrayList<SchemaAttribute> allAttributes=new ArrayList<SchemaAttribute>();

    for (SchemaAttribute attribute:getInheritedAttributes())
    { 
      SchemaAttribute extendedAttribute=definedAttributes.get(attribute.getClass());
      if (extendedAttribute!=null)
      { 
        // Add the extended attribute in the same position the base would
        //   have been added
        extendedAttribute.base=attribute;
        allAttributes.add(extendedAttribute);
      }
      else
      { 
        // Add the base attribute
        allAttributes.add(attribute);
      }
      
    }
    
    
    
    // Add the rest of the newly defined attributes that don't override
    //   anything.
    for (SchemaAttribute attribute:definedAttributes.values())
    { 
      if (attribute.base==null)
      { allAttributes.add(attribute);
      }
      attribute.resolve();
    }
    this.combinedAttributes
      =allAttributes.toArray(new SchemaAttribute[allAttributes.size()]);

  }
}
