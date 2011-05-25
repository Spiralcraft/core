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
package spiralcraft.data.spi;

import java.lang.ref.WeakReference;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.Identifier;
import spiralcraft.data.Key;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.Scheme;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.log.ClassLog;

/**
 * Abstract Base class for Tuple functionality.
 */
public abstract class AbstractTuple
  implements Tuple
{
  protected static final ClassLog log
    =ClassLog.getInstance(AbstractTuple.class);
  
  protected final FieldSet fieldSet;
  protected AbstractTuple baseExtent;
  protected Identifier id;
  protected boolean debug;
  protected WeakReference<?> behaviorRef;

  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public AbstractTuple(FieldSet fieldSet)
  { this.fieldSet=fieldSet;
  }
  
  protected abstract AbstractTuple createBaseExtent(FieldSet fieldSet);
  
  protected abstract AbstractTuple createBaseExtent(Tuple tuple)
    throws DataException;

  protected abstract AbstractTuple createDeltaBaseExtent(DeltaTuple tuple)
    throws DataException;
  
//  /**
//   * Construct an ArrayTuple with an empty set of data
//   */
//  public AbstractTuple(FieldSet fieldSet,Tuple baseExtent)
//  { 
//    this.fieldSet=fieldSet;
//    this.hasScheme=fieldSet instanceof Scheme;
//    this.baseExtent=baseExtent;
//  }

  @Override
  public Object get(String fieldName)
    throws DataException
  { 
    Field<?> field=null;
    if (fieldSet.getType()!=null)
    { 
      field=fieldSet.getType().getField(fieldName);
      if (field==null)
      { throw new FieldNotFoundException(fieldSet.getType(),fieldName);
      }
    }
    else
    {
      field=fieldSet.getFieldByName(fieldName);
      if (field==null)
      { throw new FieldNotFoundException(fieldSet,fieldName);
      }
    }
    
    return field.getValue(this);
  }
  
  @Override
  public Tuple getBaseExtent()
  { return baseExtent;
  }
  
  @Override
  public boolean isTuple()
  { return true;
  }
  
  @Override
  public Tuple asTuple()
  { return this;
  }
  
  @Override
  public boolean isAggregate()
  { return false;
  }
  
  @Override
  public Aggregate<?> asAggregate()
  { throw new UnsupportedOperationException("Not an Aggregate");
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Identifier getId()
  { 
    if (id==null && getType()!=null)
    { 
      Key<Tuple> primaryKey=(Key<Tuple>) getType().getPrimaryKey();
      if (primaryKey!=null)
      { setId((KeyIdentifier<Tuple>) primaryKey.getFunction().key(this));
      }
      else
      { 
        setId(new PojoIdentifier<Tuple>(this));
        if (debug)
        { log.fine("Created new PojoId for Tuple "+this);
        }
      }
    }
    return id;
  }
    
  void setId(Identifier id)
  { 
    if (debug)
    { log.fine("Setting id "+id+" for Tuple "+this);
    }
    this.id=id;
    if (baseExtent!=null)
    { baseExtent.setId(id);
    }
  }
  
  @Override
  public Type<?> getType()
  { return fieldSet.getType();
  }
  
  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  public Scheme getScheme()
  { 
    if (fieldSet instanceof Scheme)
    { return (Scheme) fieldSet;
    }
    else
    { return null;
    }
  }
  
  @Override
  public Tuple widen(Type<?> type)
    throws DataException
  {
    final Type<?> myType=getType();
    if (myType!=null)
    {
      if (myType==type || myType.hasArchetype(type))
      { 
        // == Comparison speeds common case 
        return this;
      }
      else
      {
        Tuple baseExtent=resolveBaseExtent();
        if (baseExtent!=null)
        { return baseExtent.widen(type);
        }
        else
        { 
          throw new TypeMismatchException
            ("Type "+myType+" has no base type compatible with "
            +" wider type "+type
            );
        }
      }
    }
    else
    { return null;
    }
  }
  
  @Override
  public Tuple snapshot()
    throws DataException
  { 
    if (isMutable())
    { return new ArrayTuple(this);
    }
    else
    { return this;
    }
  }
  
  @Override
  public boolean isMutable()
  { return false;
  }
  
  @Override
  public synchronized Object getBehavior()
    throws DataException
  {
    if (getType()==null)
    { return null;
    }
    
    if (getType().getNativeClass()==null)
    { return null;
    }
    
    Object behavior=null;
    if (behaviorRef!=null)
    { behavior=behaviorRef.get();
    }
    
    StaticInstanceResolver instanceResolver=null;
    if (behavior!=null)
    { instanceResolver=new StaticInstanceResolver(behavior);
    }
    Object newBehavior=getType().fromData(this,instanceResolver);
    
    if (newBehavior!=behavior)
    { behaviorRef=new WeakReference<Object>(newBehavior);
    }
    
    return newBehavior;
    
  }
  
  
  public static final boolean tupleEquals(Tuple a,Tuple t)
  {
    for (Field<?> field : a.getFieldSet().fieldIterable())
    { 
      Object thisVal;
      Object otherVal;
      
      try
      {
        thisVal=a.get(field.getIndex());
        otherVal=t.get(field.getIndex());
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error reading field ["+field+"]:"+x,x);
      }
      
      if (thisVal!=null)
      { 
        if (!thisVal.equals(otherVal))
        { return false;
        }
      }
      else
      {
        if (otherVal!=null)
        { return false;
        }
      }
    }
    
    if (a.getBaseExtent()!=null)
    { return a.getBaseExtent().equals(t.getBaseExtent());
    }
    return true;
  }
  
  @Override
  public final boolean equals(Object o)
  {
    if (o==null)
    { return false;
    }
    
    if (o==this)
    { return true;
    }
    
    if (!(o instanceof Tuple))
    { return false;
    }
    
    return tupleEquals(this,(Tuple) o);
  }
  
  @Override
  public String toString()
  { return tupleToString(this);
  }
  
  @Override
  public String toText(String indent)
    throws DataException
  {
    return tupleToText(this,indent);
  }
  
  public static String fieldsToString(Tuple tuple)
  {
    StringBuilder sb=new StringBuilder();
    sb.append(tuple.getClass().getName()+"@"+tuple.hashCode());
    if (tuple.getType()!=null)
    { sb.append(":").append(tuple.getType().getURI());
    }
    else
    { sb.append(":(untyped)"+tuple.getFieldSet());
    }
    sb.append("[");
    boolean first=true;
    for (Field<?> field : tuple.getFieldSet().fieldIterable())
    { 
      Object val;
      try
      { val=tuple.get(field.getIndex());
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error reading field ["+field+"]:"+x,x);
      }

      if (!(val instanceof DataComposite))
      {
        if (val!=null)
        { 
          if (!first)
          { sb.append(",");
          }
          else
          { first=false;
          }
          
          String stringVal=val.toString();
            
          sb.append(field.getName())
            .append("=")
            .append("[")
            .append(stringVal)
            .append("]");
        }
      }
      else
      {
        DataComposite compositeVal=(DataComposite) val;

        if (!first)
        { sb.append(",");
        }
        else
        { first=false;
        }
          
        String stringVal
          =compositeVal.getClass().getName()+":"
            +compositeVal.getType().getURI();
        sb.append(field.getName())
          .append("=")
          .append("<")
          .append(stringVal)
          .append(">");
          
      }
      
    }
    sb.append("]");
    return sb.toString();
  }
  
  public static String tupleToString(Tuple tuple)
  {
    StringBuffer sb=new StringBuffer();
    sb.append(fieldsToString(tuple));
    if (tuple.getBaseExtent()!=null)
    { sb.append("baseExtent="+tuple.getBaseExtent());
    }
    return sb.toString();
  }
  
  protected <X> void copyFieldTo
    (Field<X> field,EditableTuple dest)
    throws DataException
  { 
    dest.getFieldSet().<X>getFieldByIndex(field.getIndex())
      .setValue(dest,field.getValue(this));
  }
  

  
  public static String tupleToText(Tuple tuple,String indent)
    throws DataException
  { 
    StringBuilder sb=new StringBuilder();
    sb.append("\r\n").append(indent);
    sb.append(tuple.getClass().getName()+"@"+tuple.hashCode());
    sb.append("\r\n").append(indent).append("==");
    FieldSet fieldSet=tuple.getFieldSet();
    if (tuple.getType()!=null)
    { 
      sb.append(tuple.getType().getURI());
      fieldSet=tuple.getType().getFieldSet();
    }
    else
    { sb.append("(untyped)-"+fieldSet);
    }
    sb.append("\r\n").append(indent);
    sb.append("[");
    boolean first=true;
    String indent2=indent.concat("  ");
    for (Field<?> field : fieldSet.fieldIterable())
    { 
      
      Object val=field.getValue(tuple);
      if (val!=null)
      { 
        sb.append("\r\n").append(indent2);
        if (!first)
        { sb.append(",");
        }
        else
        { first=false;
        }
        
        sb.append(field.getName()).append("=");
        if (val instanceof DataComposite)
        { 
          sb.append("\r\n").append(indent2)
            .append("[")
            .append(((DataComposite) val).toText(indent2+"  "));
          sb.append("\r\n").append(indent2)
            .append("]");
        }
        else
        { 
          sb.append("[");
          sb.append(val.toString());
          sb.append("]");
        }
          
          
      }
    }
    sb.append("\r\n").append(indent); 
    sb.append("]");
    return sb.toString();
  }
  
  @Override
  public String dumpData()
    throws DataException
  { return toText("| ");
  }
  
  protected Tuple resolveBaseExtent()
  { 
    if (baseExtent!=null)
    { return baseExtent;
    }
    return null;
  }
  
  

  public boolean isDebug()
  {
    return debug;
  }

  public void setDebug(
    boolean debug)
  {
    if (debug)
    { log.fine("Debugging "+this);
    }
    this.debug = debug;
  }
  
  
}