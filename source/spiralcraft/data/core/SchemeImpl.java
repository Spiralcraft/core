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

import spiralcraft.data.Key;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeMismatchException;

/**
 * Core implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  protected Type<?> type;
  protected Key primaryKey;
  private boolean resolved;
  private Scheme archetypeScheme;

  protected final ArrayList<FieldImpl> localFields
    =new ArrayList<FieldImpl>();
  protected final HashMap<String,FieldImpl> localFieldMap
    =new HashMap<String,FieldImpl>();

  protected final ArrayList<Field> fields
    =new ArrayList<Field>();
  protected final HashMap<String,Field> fieldMap
    =new HashMap<String,Field>();

  protected final ArrayList<KeyImpl> keys
    =new ArrayList<KeyImpl>();
  protected final HashMap<String,KeyImpl> keyMap
    =new HashMap<String,KeyImpl>();
  
  
  public Type<?> getType()
  { return type;
  }
  
  public void setArchetypeScheme(Scheme scheme)
  { 
    assertUnresolved();
    archetypeScheme=scheme;
  }
  
  public boolean hasArchetype(Scheme scheme)
  {
    if (this==scheme)
    { return true;
    }
    else if (archetypeScheme!=null)
    { return archetypeScheme.hasArchetype(scheme);
    }
    else
    { return false;
    }
  }
  
  public void setType(Type<?> type)
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
  
  public Field getLocalFieldByName(String name)
  { return localFieldMap.get(name);
  }
  
  /**
   *@return An Iterable that iterates through all fields of this Type and its
   *  archetype.
   */
  public Iterable<? extends Field> fieldIterable()
  { return fields;
  }

  /**
   * Get the list of local fields
   */
  @SuppressWarnings("unchecked")
  public List<FieldImpl> getFields()
  { return (List<FieldImpl>) localFields.clone();
  }
  
  /**
   * Set the list of local fields
   */
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

  /**
   * Add a local Field
   */
  public void addField(FieldImpl field)
  { 
    assertUnresolved();
    if (localFieldMap.get(field.getName())!=null)
    { 
      throw new IllegalArgumentException
        ("Field name '"+field.getName()+"' is not unique"
        );
    }
    localFields.add(field);
    localFieldMap.put(field.getName(),field);
  }
  
  /**
   * Clear the set of local fields
   */
  private void clearFields()
  { 
    assertUnresolved();
    localFields.clear();
    localFieldMap.clear();
  }

  public int getFieldCount()
  { return fields.size();
  }
  
  public String contentsToString()
  {
    if (fields==null)
    { return "(no fields)";
    }
    
    StringBuilder fieldList=new StringBuilder();
    fieldList.append("[");
    boolean first=true;
    for (Field field:fields)
    { 
      fieldList.append("\r\n       #");
      if (!first)
      { fieldList.append(",");
      }
      else
      { first=false;
      }
      fieldList.append
        (field.getClass().getSimpleName()+":"
         +field.getName()+"("+field.getType().getURI()+")"
        );
    }
    fieldList.append("]");
    return fieldList.toString();
  }
  
  public String toString()
  {
    if (getType()!=null)
    { return type.toString();
    }
    else
    {
      return super.toString()
        +"(untyped):"
        +contentsToString()
        ;
    }
  }
  
  public void resolve()
    throws DataException
  {
    if (resolved)
    { return;
    }
    resolved=true;

//    System.err.println("SchemeImpl.resolve() for "+getType());
    int fieldIndex=0;
    if (archetypeScheme!=null)
    { 
      fieldIndex=archetypeScheme.getFieldCount();
      for (Field field: archetypeScheme.fieldIterable())
      { 
        fields.add(field);
        fieldMap.put(field.getName(),field);
      }
    }
    
    
    for (FieldImpl field:localFields)
    {
      if (field.getName()==null)
      { throw new DataException("Field "+field+" name is null");
      }
      
      Field archetypeField=null;
      field.setScheme(this);
      field.resolveType();
      
      if (archetypeScheme!=null)
      { 
        archetypeField=
          archetypeScheme.getFieldByName(field.getName());
      }
      
      if (archetypeField!=null)
      { 
        if (field.isFunctionalEquivalent(archetypeField))
        {
          // Field is redundant. Don't replace archetype field, which is
          //   already mapped
          continue;
        }
        
        // Field with same name will have same index as archetype field,
        //   extending field functionality in compatible way
        try
        { 
          field.setArchetypeField(archetypeField);
          fields.set(field.getIndex(),field);
//        System.err.println
//        ("Field "+field.getUri()
//        +" overriding field "+archetypeField.getUri()
//        );
        }
        catch (TypeMismatchException x)
        { System.err.println("SchemeImpl: Ignoring archetype field override: "+x);
        }
      }
      else
      { 
        field.setIndex(fieldIndex++);
        fields.add(field);
      }
      field.lock();
      fieldMap.put(field.getName(),field);
      
    }
    
    for (FieldImpl field: localFields)
    { 
      // XXX There may be fields in this list that were not made part of this
      //   scheme because they were redundant of their Archetype field. Should
      //   we always extend an Archetype field (in the above block) to avoid
      //   this?
      field.resolve();
    }
    
    int keyIndex=0;
    for (KeyImpl key:keys)
    {
      key.setScheme(this);
      key.setIndex(keyIndex++);
      if (key.isPrimary())
      { 
        if (primaryKey!=null)
        {
          throw new DataException
            ("Scheme can only have one primary key: "+toString());
        }
        else
        { primaryKey=key;
        }
      }
      key.resolve();
      
      if (key.getForeignType()!=null && key.getName()!=null)
      {
        // Expose a Field to provide direct access to the join
        
        if (getFieldByName(key.getName())!=null)
        { 
          throw new DataException
            ("Key "+getType().getURI()+"."+key.getName()+": Field '"
              +key.getName()+"' already exists"
            );
        }
        else
        { addFieldPostResolve(new KeyField(key));
        }
      }
      
    }
    
  }
  
  private void addFieldPostResolve(FieldImpl field)
    throws DataException
  { 
    field.setIndex(fields.size());
    field.setScheme(this);
    fields.add(field);
    field.resolve();
    fieldMap.put(field.getName(), field);
  }
  
  public void assertUnresolved()
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved "+getType());
    }
  }

  public Key getKeyByIndex(int index)
  { return keys.get(index);
  }

  public int getKeyCount()
  { return keys.size();
  }

  public Key getPrimaryKey()
  { 
    if (primaryKey==null 
        && type!=null
        && type.getBaseType()!=null
        )
    { return type.getBaseType().getScheme().getPrimaryKey();
    }
    return primaryKey;
  }

  public void setKeys(KeyImpl[] keyArray)
  { 
    for (KeyImpl key: keyArray)
    { keys.add(key);
    }
  }
  
  public Iterable<? extends Key> keyIterable()
  { return keys;
  }
}