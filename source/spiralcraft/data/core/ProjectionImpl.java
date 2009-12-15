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
import spiralcraft.data.ProjectionField;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Projection;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.lang.BoundTuple;
import spiralcraft.data.lang.TupleReflector;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.AbstractChannel;

/**
 * <p>Implements a Projection- a FieldSet that references a set of expressions
 *   evaluated against a subject.
 * </p>
 *   
 * @author mike
 *
 */
public class ProjectionImpl<T>
  implements Projection<T>
{
  protected final ArrayList<ProjectionFieldImpl<?>> fields
  =new ArrayList<ProjectionFieldImpl<?>>();

  protected final HashMap<String,ProjectionFieldImpl<?>> fieldMap
    =new HashMap<String,ProjectionFieldImpl<?>>();
 
  
  protected FieldSet masterFieldSet;
  
  protected boolean resolved;
  
  protected TupleReflector<Tuple> reflector;
  
  private Type<?> type;
  
  protected boolean debug;


  public ProjectionImpl()
  {


  }
  
  
  /**
   * <p>Create a projection that targets another FieldSet and simply subsets
   *   the Fields in that FieldSet.
   * </p>
   * 
   * @param masterFieldSet
   * @param fieldNames
   * @throws DataException
   */
  public ProjectionImpl(FieldSet masterFieldSet,String ... fieldNames)
    throws DataException
  {
    this.masterFieldSet=masterFieldSet;
    for (String fieldName : fieldNames)
    { 
      Field<?> masterField=masterFieldSet.getFieldByName(fieldName);
      if (masterField==null)
      { throw new FieldNotFoundException(masterFieldSet,fieldName);
      }
      addMasterField(fieldName,masterField);
    }
    reflector=new TupleReflector<Tuple>(this,Tuple.class);
  }


  public ProjectionImpl(FieldSet masterFieldSet,Expression<?>[] expressions)
    throws DataException
  {    
    this.masterFieldSet=masterFieldSet;
    int i=0;
    for (Expression<?> expression : expressions)
    {
      ProjectionFieldImpl<?> field=makeField(expression);
      field.setIndex(fields.size());
      field.setName("field"+(i++));
      fields.add(field);
      fieldMap.put(field.getName(),field);
    }
    reflector=new TupleReflector<Tuple>(this,Tuple.class);
    
  }  
  
  private <X> ProjectionFieldImpl<X> makeField(Expression<X> expression)
    throws DataException
  {
      ProjectionFieldImpl<X> field=new ProjectionFieldImpl<X>();
      field.setFieldSet(this);
      try
      {
        field.setType
          (TupleReflector.getInstance
            (masterFieldSet).<X>getTypeAsSubject(expression)
          );
      }
      catch (BindException x)
      { throw new DataException("Error reflecting Scheme "+masterFieldSet,x);
      }
      
      field.setExpression(expression);
      return field;
    
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  public Iterable<? extends ProjectionField<?>> fieldIterable()
  { return fields;
  }

  @SuppressWarnings("unchecked") // Map value cast
  public <X> ProjectionField<X> getFieldByIndex(int index)
  { return (ProjectionField<X>) fields.get(index);
  }

  @SuppressWarnings("unchecked") // Map value cast
  public <X> ProjectionField<X> getFieldByName(String name)
  { return (ProjectionField<X>) fieldMap.get(name);
  }
  
  public int getFieldCount()
  { return fields.size();
  }

  public FieldSet getMasterFieldSet()
  { return masterFieldSet;
  }


  
  protected <X> void addMasterField(String name,Field<X> masterField)
    throws DataException
  { 
    assertUnresolved();
    
    ProjectionFieldImpl<X> field=new ProjectionFieldImpl<X>();
    field.setFieldSet(this);
    field.setIndex(fields.size());
    field.setType(masterField.getType());
    field.setName(name);
    field.setExpression(Expression.create("."+masterField.getName()));
    fields.add(field);
    fieldMap.put(field.getName(),field);
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
    for (ProjectionFieldImpl<?> field: fields)
    { field.resolve();
    }
    
// TODO: Experimental
    
    // Try to create a type that uniquely identifies this projection.
    //   as long as this projection is simply a subset of the fields in the
    //   masterFieldSet.
    if (masterFieldSet!=null && masterFieldSet.getType()!=null)
    {
      this.type
        =new FieldSetType
          (masterFieldSet.getType().getURI().resolve("-projection"),this);
      this.type.link();
    }
    resolved=true;
  }
  
  public Expression<?>[] getTargetExpressions()
  {
    Expression<?>[] ret=new Expression[getFieldCount()];
    for (int i=0;i<ret.length;i++)
    { ret[i]=this.getFieldByIndex(i).getExpression();
    }
    return ret;
  }
  
  @Override
  public Channel<Tuple> bindChannel
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?>[] args
    )
    throws BindException
  { 
    
    if (focus.getSubject()!=source)
    { focus=focus.chain(source);
    } 
    
    return new ProjectionChannel(focus);
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
      for (ProjectionFieldImpl<?> field : fields)
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
    for (Field<?> field:fields)
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
  
  @Override
  public String toString()
  { return contentsToString();
  }
  
}


