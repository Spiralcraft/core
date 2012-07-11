//
// Copyright (c) 1998,2012 Michael Toth
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
package spiralcraft.common.attributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import spiralcraft.common.ContextualException;


/**
 * <p>Base for objects used to contain Attributes
 * </p>
 * 
 * <p>Allows for the construction of lightweight, inheritable structures
 *   that allow the loose association of strongly typed data with arbitrary
 *   objects.
 * </p>
 * 
 * @author mike
 *
 * @param <T>
 */
public abstract class AbstractAttributeContext<T extends AbstractAttributeContext<?>>
{
  protected T base;

  private final LinkedHashMap<Class<?>,Attribute> definedAttributes
    =new LinkedHashMap<Class<?>,Attribute>();
  
  private Attribute[] combinedAttributes;
  
  
  public void setExtends(T base)
  { this.base=base;
  }
  
  public T getBase()
  { return base;
  }
  
  
  /**
   * 
   * @return the Entities associated with this Schema
   */
  public Attribute[] getAttributes()
  { 
    return combinedAttributes;
    
  }

  /**
   * 
   * @param entities The Entities associated with this Schema
   */
  public void setAttributes(Attribute[] attributes)
  { 
    this.definedAttributes.clear();
    for (Attribute attribute:attributes)
    { this.definedAttributes.put(attribute.getClass(),attribute);
    }
  }
  
  @SuppressWarnings("unchecked")
  public <A extends Attribute> A getAttribute(Class<?> clazz)
  { 
    A ret=(A) definedAttributes.get(clazz);
    if (ret==null && base!=null)
    { ret=base.<A>getAttribute(clazz);
    }
    return ret;
  }
  
  protected Attribute[] getInheritedAttributes()
  {
    if (base!=null)
    { return base.getAttributes();
    }
    else
    { return new Attribute[0];
    }
  }
  
  protected void resolveBase()
    throws ContextualException
  { 
    if (base!=null)
    { base.resolve();
    }
  }
  
  protected void resolve()
    throws ContextualException
  {
    ArrayList<Attribute> allAttributes=new ArrayList<Attribute>();

    for (Attribute attribute:getInheritedAttributes())
    { 
      Attribute extendedAttribute=definedAttributes.get(attribute.getClass());
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
    for (Attribute attribute:definedAttributes.values())
    { 
      if (attribute.base==null)
      { allAttributes.add(attribute);
      }
      attribute.resolve();
    }
    this.combinedAttributes
      =allAttributes.toArray(new Attribute[allAttributes.size()]);

  }
}
