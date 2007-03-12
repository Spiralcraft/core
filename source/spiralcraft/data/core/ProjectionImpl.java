//
// Copyright (c) 1998,2005 Michael Toth
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

import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Projection;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Tuple;

import spiralcraft.data.spi.ArrayTuple;
import spiralcraft.data.spi.ListCursor;

import spiralcraft.data.transport.Cursor;

import spiralcraft.data.lang.CursorBinding;
import spiralcraft.data.lang.BoundTuple;

import spiralcraft.lang.DefaultFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Focus;

public class ProjectionImpl
  implements Projection
{
  protected final ArrayList<Field> fields
  =new ArrayList<Field>();

  protected final HashMap<String,Field> fieldMap
    =new HashMap<String,Field>();
 
  protected final ArrayList<Mapping> mappings
    =new ArrayList<Mapping>();
  
  protected FieldSet masterFieldSet;
  
  protected boolean resolved;

  public ProjectionImpl()
  {
  }
  
  public ProjectionImpl(FieldSet masterFieldSet,String ... fieldNames)
    throws DataException
  {
    this.masterFieldSet=masterFieldSet;
    for (String fieldName : fieldNames)
    { 
      Field masterField=masterFieldSet.getFieldByName(fieldName);
      if (masterField==null)
      { throw new FieldNotFoundException(masterFieldSet,fieldName);
      }
      addMasterField(fieldName,masterField);
    }
  }
  
  public Iterable<? extends Field> fieldIterable()
  { return fields;
  }

  public Field getFieldByIndex(int index)
  { return fields.get(index);
  }

  public Field getFieldByName(String name)
  { return fieldMap.get(name);
  }

  public int getFieldCount()
  { return fields.size();
  }

  public FieldSet getMasterFieldSet()
  { return masterFieldSet;
  }

  protected void addMasterField(String name,Field masterField)
  { 
    assertUnresolved();
    
    FieldImpl field=new FieldImpl();
    field.setFieldSet(this);
    field.setIndex(fields.size());
    field.setType(masterField.getType());
    fields.add(field);
    fieldMap.put(field.getName(),field);
    mappings.add(new FieldMapping(masterField));
  }
  
  protected void assertUnresolved()
  { 
    if (resolved)
    { throw new IllegalStateException("Projection definition cannot be modified");
    }
  }
  
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;
  }
  
  public Tuple project(Tuple source)
    throws DataException
  { 
    ListCursor cursor=new ListCursor<Tuple>(masterFieldSet,source);
    cursor.dataNext();
    return bind(cursor).dataGetTuple();
  }
  
  public Cursor bind(Cursor source)
    throws DataException
  {
    try
    {
      if (source.dataGetFieldSet()!=masterFieldSet)
      { 
        throw new DataException
          ("Cursor of type "+source.dataGetFieldSet()
          +" cannot be bound by a Projection of type "
          +masterFieldSet.toString()
          );
      }
      CursorBinding cursorBinding=new CursorBinding(source);
      DefaultFocus focus=new DefaultFocus<Tuple>(cursorBinding);
      return new ProjectionCursor(cursorBinding,focus);
    }
    catch (BindException x)
    { 
      throw new DataException
        ("Error binding Projection "+toString()+": "+x
        ,x
        );
    }
  }
  
  class ProjectionCursor
    implements Cursor
  {
    private final BoundTuple boundTuple;
    
    public ProjectionCursor(CursorBinding source,Focus focus)
      throws BindException
    { 
      Optic[] bindings=new Optic[mappings.size()];
      int i=0;
      for (Mapping mapping: mappings)
      { bindings[i++]=mapping.bind(source,focus);
      }
      boundTuple=new BoundTuple(ProjectionImpl.this,bindings);
    }
    
    public FieldSet dataGetFieldSet()
    { return ProjectionImpl.this;
    }
    
    public Tuple dataGetTuple()
      throws DataException
    { 
      return new ArrayTuple(boundTuple);
    }
  }
  
}

/**
 * Maps a local field to a data source (master field or expression)
 */
abstract class Mapping
{
  public abstract Optic bind(CursorBinding source,Focus focus)
    throws BindException;

}

class FieldMapping
  extends Mapping
{
  private final Field masterField;
  
  public FieldMapping(Field masterField)
  { this.masterField=masterField;
  }
  
  public Optic bind(CursorBinding source,Focus focus)
    throws BindException
  { return source.resolve(focus,masterField.getName(),null);
    
  }
}

