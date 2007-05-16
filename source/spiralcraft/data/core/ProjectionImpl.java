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
import spiralcraft.data.BoundProjection;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Tuple;

import spiralcraft.data.lang.BoundTuple;
import spiralcraft.data.lang.TupleFocus;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;

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
    // XXX We should move to ProjectionFields
    
    FieldImpl field=new FieldImpl();
    field.setFieldSet(this);
    field.setIndex(fields.size());
    field.setType(masterField.getType());
    field.setName(masterField.getName());
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
  
  
  public BoundProjection createBinding()
    throws DataException
  { return new Binding();
  }
  
    
  class Binding
    implements BoundProjection
  {
    private final TupleFocus<Tuple> focus;
    
    private final BoundTuple boundTuple;
    
    public Binding()
      throws DataException
    { 
      focus=new TupleFocus<Tuple>(masterFieldSet);
      
      Optic[] bindings=new Optic[mappings.size()];
      int i=0;
      for (Mapping mapping: mappings)
      { 
        try
        { bindings[i++]=mapping.bind(focus);
        }
        catch (BindException x)
        { throw new DataException("Error binding mapping '"+mapping+"' "+x,x);
        }
      }
      boundTuple=new BoundTuple(ProjectionImpl.this,bindings);
    }

    public Projection getProjection()
    { return ProjectionImpl.this;
    }
    
    public Tuple project(Tuple masterTuple)
      throws DataException
    { 
      focus.setTuple(masterTuple);
      return boundTuple;
    }
  }
  
}

/**
 * Maps a local field to a data source (master field or expression)
 */
abstract class Mapping
{
  public abstract Optic bind(TupleFocus<Tuple> focus)
    throws BindException;

}

class FieldMapping
  extends Mapping
{
  private final Field masterField;
  
  public FieldMapping(Field masterField)
  { this.masterField=masterField;
  }
  
  public Optic bind(TupleFocus<Tuple> focus)
    throws BindException
  { return focus.getSubject().resolve(focus,masterField.getName(),null);
    
  }
}

