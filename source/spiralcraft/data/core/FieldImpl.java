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

import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DataException;

import java.net.URI;

/**
 * Core implementation of a Field
 */
public class FieldImpl
  implements Field
{
  private boolean locked;
  private FieldSet fieldSet;
  private int index;
  private String name;
  private Type<?> type;
  private Field archetypeField;
  private URI uri;
  private boolean isScheme;
  
  /**
   * Set the scheme
   */
  void setScheme(SchemeImpl scheme)
  { 
    assertUnlocked();
    isScheme=true;
    this.fieldSet=scheme;
    if (scheme.getType()!=null)
    { 
      this.uri
        =URI.create(scheme.getType().getURI().toString()+"#"+getName());
    }
    else
    {
      this.uri
        =URI.create("untyped#"+getName());
      
    }
  }
  
  void setFieldSet(FieldSet fieldSet)
  {
    assertUnlocked();
    if (fieldSet instanceof Scheme)
    { setScheme((SchemeImpl) fieldSet);
    }
    else
    { 
      this.fieldSet=fieldSet;
      this.uri=URI.create("untyped#"+getName());
    }
    
  }
  
  void setArchetypeField(Field field)
    throws DataException
  {
    archetypeField=field;
    this.index=archetypeField.getIndex();
    if (!this.type.hasArchetype(archetypeField.getType()))
    { 
      throw new DataException
        ("Field "+getUri()+"'"
        +" cannot extend field "+archetypeField.getUri()
        +": type "+archetypeField.getType()+" is not an archetype of "
        +this.type
        );
    }
  }
  
  /**
   *@return the owning FieldSet
   */
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  /**
   *@return the Scheme
   */
  public Scheme getScheme()
  { 
    if (fieldSet instanceof Scheme)
    { return (Scheme) fieldSet;
    }
    else
    { return null;
    }
  }

  public URI getUri()
  { return uri;
  }
  
  
  /**
   *@return Whether this field has the same type, constraints and attributes
   *   as the specified field.
   */
  boolean isFunctionalEquivalent(Field field)
  { return field.getType()==getType();
  }
  
  /**
   * Set the index
   */
  void setIndex(int index)
  { 
    assertUnlocked();
    this.index=index;
  }

  /**
   * Return the index
   */
  public int getIndex()
  { return index;
  }
  
  /**
   * Set the field name
   */
  public void setName(String name)
  { 
    assertUnlocked();
    this.name=name;
  }
  
  public String getName()
  { return name;
  }
  
  /**
   * Set the data Type
   */
  public void setType(Type<?> type)
  { 
    assertUnlocked();
    this.type=type;
  }
  
  public Type<?> getType()
  { return type;
  }

  private Tuple widenTuple(Tuple t)
    throws DataException
  {
    if (isScheme)
    { 
      Scheme scheme=(Scheme) fieldSet;
      // Find the Tuple which stores this field
      if (scheme.getType()!=null)
      { t=t.widen(scheme.getType());
      }
    }
    else
    { 
      if (fieldSet!=t.getFieldSet())
      { t=null;
      }
    }
    return t;
    
  }
  
  private EditableTuple widenTuple(EditableTuple t)
    throws DataException
  {
    if (isScheme)
    { 
      Scheme scheme=(Scheme) fieldSet;
      // Find the Tuple which stores this field
      if (scheme.getType()!=null)
      { t=t.widen(scheme.getType());
      }
    }
    else
    { 
      if (fieldSet!=t.getFieldSet())
      { t=null;
      }
    }
    return t;
  }
  
  public Object getValue(Tuple t)
    throws DataException
  { 
    t=widenTuple(t);
    
    if (t!=null)
    { return t.get(index);
    }
      
    throw new IllegalArgumentException
      ("Field '"+name+"' not in Tuple FieldSet "+t.getFieldSet());
      
  }
  
  public void setValue(EditableTuple t,Object value)
    throws DataException
  { 
    t=widenTuple(t);
    
    if (t!=null)
    { t.set(index,value);
    } 
    else
    {
      throw new IllegalArgumentException
        ("Field "+getUri()
        +" not in Tuple FieldSet "+t.getFieldSet()
        );
    }
  }
  
  void lock()
  { locked=true;
  }
  
  public String toString()
  { return super.toString()+":"+uri;
  }
  
  private final void assertUnlocked()
  { 
    if (locked)
    { throw new IllegalStateException("Field is in read-only state");
    }
  }
}