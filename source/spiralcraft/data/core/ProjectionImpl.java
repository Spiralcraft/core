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

import spiralcraft.data.DataComposite;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Projection;
import spiralcraft.data.BoundProjection;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.lang.BoundTuple;
import spiralcraft.data.lang.TupleFocus;
import spiralcraft.data.lang.TupleReflector;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.AbstractChannel;

/**
 * Implements a Projection- a FieldSet that references a field-by-field
 *   transformation of a master FieldSet.
 *   
 * @author mike
 *
 * 
 */
public class ProjectionImpl
  implements Projection
{
  protected final ArrayList<ProjectionField> fields
  =new ArrayList<ProjectionField>();

  protected final HashMap<String,Field> fieldMap
    =new HashMap<String,Field>();
 
  
  protected FieldSet masterFieldSet;
  
  protected boolean resolved;
  
  protected TupleReflector<Tuple> reflector;
  
  private Type<?> type;


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
    try
    { reflector=new TupleReflector<Tuple>(this,Tuple.class);
    }
    catch (BindException x)
    { throw new DataException(x.toString());
    }
  }

  public Type<?> getType()
  { return type;
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
    
//    FieldImpl field=new FieldImpl();
    ProjectionField field=new ProjectionField();
    field.setFieldSet(this);
    field.setIndex(fields.size());
    field.setType(masterField.getType());
    field.setName(name);
    field.setMasterField(masterField);
    field.setExpression(Expression.create("."+masterField.getName()));
    fields.add(field);
    fieldMap.put(field.getName(),field);
    //mappings.add(new FieldMapping(masterField));
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
    for (ProjectionField field: fields)
    { field.resolve();
    }
// TODO: Experimental
    if (masterFieldSet.getType()!=null)
    {
      this.type
        =new FieldSetType
          (masterFieldSet.getType().getURI().resolve("-projection"),this);
      this.type.link();
    }
    resolved=true;
  }
  
  
  public Channel<Tuple> bind(Focus<?> focus)
    throws BindException
  { return new ProjectionChannel(focus);
  }
  
  public class ProjectionChannel
    extends AbstractChannel<Tuple>
  {
     
    private final Focus<?> focus;
    private final BoundTuple boundTuple;
    
    public ProjectionChannel(Focus<?> masterFocus)
      throws BindException
    { 
      super(new TupleReflector<Tuple>(ProjectionImpl.this,Tuple.class));
      this.focus=masterFocus; 
//      Channel<?>[] bindings=new Channel[mappings.size()];
//      int i=0;
//      for (Mapping mapping: mappings)
//      { bindings[i++]=mapping.bind(focus);
//      }

      Channel<?>[] bindings=new Channel[fields.size()];
      int i=0;
      for (ProjectionField field : fields)
      { bindings[i++]=focus.bind(field.getExpression()); 
      }
      
      boundTuple=new BoundTuple(ProjectionImpl.this,bindings);
      
    }

    @Override
    public boolean isWritable()
    { return true;
    }
    
    @Override
    protected Tuple retrieve()
    { return boundTuple;
    }

    @Override
    protected boolean store(
      Tuple val)
    {
      // Anonymous positional copy
      if (val.getFieldSet().getFieldCount()==getFieldCount())
      { 
        int numFields=getFieldCount();
        for (int i=0;i<numFields;i++)
        { 
          try
          { boundTuple.set(i,val.get(i));
          }
          catch (DataException x)
          { throw new AccessException("Error updating Projection",x);
          }
        }
        return true;
      }
      // TODO Auto-generated method stub
      return false;
    }
    
    
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
      fieldList.append("\r\n  ");
      if (!first)
      { fieldList.append(",");
      }
      else
      { first=false;
      }
      fieldList.append(field.toString());
    }
    fieldList.append("]");
    return fieldList.toString();
  }
  
  public String toString()
  { return contentsToString();
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
      
      Channel<?>[] bindings=new Channel[fields.size()];
      int i=0;
      for (ProjectionField field : fields)
      { 
        try
        { bindings[i++]=field.bind(focus);
        }
        catch (BindException x)
        { throw new DataException("Error binding mapping '"+field+"' "+x,x);
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


