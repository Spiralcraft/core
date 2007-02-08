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
package spiralcraft.data.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;

/**
 * Core implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  protected Type type;
  protected final ArrayList<FieldImpl> fields
    =new ArrayList<FieldImpl>();
  protected final HashMap<String,FieldImpl> fieldMap
    =new HashMap<String,FieldImpl>();
  private boolean resolved;
  
  public Type getType()
  { return type;
  }
  
  public void setType(Type type)
  { 
    assertUnresolved();
    this.type=type;
  }
  
  public Field getFieldByIndex(int index)
  { return fields.get(index);
  }
  
  public Field getFieldByName(String name)
  { return fieldMap.get(name);
  }

  public Iterable<? extends Field> fieldIterable()
  { return fields;
  }
  
  public List<FieldImpl> getFields()
  { return (List<FieldImpl>) fields.clone();
  }
  
  public void setFields(List<FieldImpl> fields)
  { 
    assertUnresolved();
    clearFields();
    for (FieldImpl field : fields)
    { 
      // System.out.println("Field "+field.toString());
      addField(field);
    }
  }

  protected void addField(FieldImpl field)
  { 
    
    if (fieldMap.get(field.getName())!=null)
    { 
      throw new IllegalArgumentException
        ("Field name '"+field.getName()+"' is not unique"
        );
    }
    field.setIndex(fields.size());
    field.setScheme(this);
    field.lock();
    fields.add(field);
    fieldMap.put(field.getName(),field);
  }
  
  public int getFieldCount()
  { return fields.size();
  }
  
  private void clearFields()
  { 
    assertUnresolved();
    fields.clear();
    fieldMap.clear();
  }
  
  public String toString()
  {
    StringBuilder fieldList=new StringBuilder();
    fieldList.append("[");
    boolean first=true;
    for (FieldImpl field:fields)
    { 
      if (!first)
      { fieldList.append(",");
      }
      else
      { first=false;
      }
      fieldList.append(field.toString());
    }
    fieldList.append("]");
    return super.toString().concat(fieldList.toString());
  }
  
  public void resolve()
  {
    if (!resolved)
    { resolved=true;
    }
  }
  
  public void assertUnresolved()
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved");
    }
  }
}