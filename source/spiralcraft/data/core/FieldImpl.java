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
import spiralcraft.data.TypeMismatchException;

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.AbstractChannel;

import spiralcraft.data.lang.DataReflector;

import java.net.URI;

@SuppressWarnings("unchecked")
/**
 * <P>Implementation of a standard Field.
 */
public class FieldImpl
  implements Field
{
  private boolean locked;
  private FieldSet fieldSet;
  private int index;
  private String name;
  private String title;
  private Type<?> type;
  private Field archetypeField;
  private URI uri;
  private boolean isScheme;
  private boolean stored=true;
  private Reflector contentReflector;
  private Expression<?> defaultExpression;
  private Expression<?> fixedExpression;
  private Expression<?> newExpression;
  
  protected boolean debug;
  
  
  /**
   * Set the scheme
   */
  void setScheme(SchemeImpl scheme)
  { 
    assertUnlocked();
    isScheme=true;
    this.fieldSet=scheme;
//    if (scheme.getType()!=null)
//    { 
//      this.uri
//        =URI.create(scheme.getType().getURI().toString()+"#"+getName());
//    }
//    else
//    {
//      this.uri
//        =URI.create("untyped#"+getName());
//      
//    }
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
//      this.uri=URI.create("untyped#"+getName());
    }
    
  }
  public Field getArchetypeField()
  { return archetypeField;
  }
  
  public void setArchetypeField(Field field)
    throws DataException
  {
    assertUnlocked();
    archetypeField=field;
    this.index=archetypeField.getIndex();
    
    if (!archetypeField.getType().isAssignableFrom(this.getType()))
    { 
      generateURI();
      throw new TypeMismatchException
        ("Field "+getURI()+"'"
        +" cannot extend field "+archetypeField.getURI()
        +" as types are not compatible"
        ,field.getType()
        ,this.type
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

  public URI getURI()
  { return uri;
  }
  
  public Expression<?> getNewExpression()
  { return newExpression;
  }
  
  public void setNewExpression(Expression newExpression)
  { this.newExpression=newExpression;
  }

  public Expression<?> getDefaultExpression()
  { return defaultExpression;
  }
  
  public void setDefaultExpression(Expression defaultExpression)
  { this.defaultExpression=defaultExpression;
  }
  
  public Expression<?> getFixedExpression()
  { return fixedExpression;
  }
  
  public void setFixedExpression(Expression fixedExpression)
  { this.fixedExpression=fixedExpression;
  }

  /**
   *@return Whether this field has the same type, constraints and attributes
   *   as the specified field.
   */
  public boolean isFunctionalEquivalent(Field field)
  { return field.getType()==getType();
  }
  
  /**
   *@return Whether this field is stored or whether it is recomputed every time
   *  the value is accessed.
   */
  public boolean isStored()
  { return stored;
  }
  

  public void setStored(boolean stored)
  { this.stored=stored;
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
  
  public String getTitle()
  { 
    if (title==null)
    { return name;
    }
    else
    { return title;
    }
  }
  
  public void setTitle(String title)
  { 
    assertUnlocked();
    this.title=title;
  }
  
  /**
   * Set the data Type
   */
  public void setType(Type<?> type)
  { 
    assertUnlocked();
    this.type=type;
    try
    { this.contentReflector=DataReflector.getInstance(type);
    }
    catch (BindException x)
    { 
      throw new IllegalArgumentException
        ("Could not find spiralcraft.lang.Reflector for Type "+type.getURI());
    }
  }
  
  public Type<?> getType()
  { return type;
  }

  /**
   * Called before resolve() when a definitive Type is needed.
   *    
   * @throws DataException
   */
  protected void resolveType()
    throws DataException
  {
  }
  
  private Tuple widenTuple(Tuple t)
    throws DataException
  {
    final Type fieldSetType=fieldSet.getType();
    
    if (fieldSetType!=null)
    { t=t.widen(fieldSetType);
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
      final Type schemeType=scheme.getType();
      if (schemeType!=null)
      { t=t.widen(schemeType);
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
  
  public final Object getValue(Tuple t)
    throws DataException
  { 
    if (t==null)
    { 
      throw new IllegalArgumentException
        ("Tuple cannot be null");
    }
    
    t=widenTuple(t);
    
    if (t!=null)
    { 
      Object val=getValueImpl(t);
      // System.err.println("FieldImpl "+getURI()+": getValue()="+val);
      return val;
    }

    throw new IllegalArgumentException
      ("Tuple does not belong to FieldSet "+getFieldSet());
      
  }
  

  public final void setValue(EditableTuple t,Object value)
    throws DataException
  { 
    if (t==null)
    {
      throw new IllegalArgumentException
        ("Tuple cannot be null");
    }
    
    t=widenTuple(t);
    
    if (t!=null)
    { setValueImpl(t,value);
    } 
    else
    {
      throw new IllegalArgumentException
        ("Tuple does not belong to FieldSet "+getFieldSet());
    }
  }
  
  /**
   * Prevent further changes to this Field definition
   */
  void lock()
  { locked=true;
  }
  
  void unlock()
  { locked=false;
  }
  
  /**
   * Resolve any external dependencies.
   */
  void resolve()
    throws DataException
  {
    if (!locked)
    { lock();
    }
    if (getType()==null)
    { 
      generateURI();
      throw new DataException("Field must have a type: "+uri);
    }
    generateURI();
    
  }
  
  protected void generateURI()
  {
    if (fieldSet.getType()==null)
    { this.uri=URI.create("untyped#"+getName());
    }
    else
    { this.uri
        =URI.create(fieldSet.getType().getURI().toString()+"#"+getName());

    }
  }
  

  
  @Override
  public String toString()
  { return super.toString()+":"+uri;
  }
  
  /**
   * Implements the field data value retrieval mechanism
   *   
   * @param tuple The Tuple that contains the value
   * @return The data value of the Field in the specified Tuple
   * @throws DataException
   */
  protected Object getValueImpl(Tuple tuple)
    throws DataException
  { return tuple.get(index);
  }

  /**
   * Implement the field data value update mechanism
   * 
   * @param tuple The EditableTuple to be updated
   * @param value The data value to update
   * @throws DataException
   */
  protected void setValueImpl(EditableTuple tuple,Object value)
    throws DataException
  { tuple.set(index,value);
  }

  
  /**
   * Ensure that this Field definition is still modifyable or throw an
   *   exception
   *
   */
  private final void assertUnlocked()
  { 
    if (locked)
    { throw new IllegalStateException("Field is in read-only state");
    }
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }

  @Override
  public Channel<?> bind
    (Focus<? extends Tuple> focus)
    throws BindException
  { 
    Channel source=focus.getSubject();
    Channel binding=source.getCached(this);
    if (binding==null)
    { 
      binding=new FieldChannel(source);
      source.cache(this,binding);
    }
    return binding;
  }
  
  public class FieldChannel
    extends AbstractChannel
  {
    protected final Channel<? extends Tuple> source;
    
    public FieldChannel(Channel<? extends Tuple> source)
    { 
      super(contentReflector);
      this.source=source;
    }

    @Override
    protected Object retrieve()
    {
      Tuple t;
      Tuple subtypeTuple=source.get();
      if (subtypeTuple==null)
      { 
        // Defines x.f to be null if x is null
        return null;
      }
      
      if (fieldSet==subtypeTuple.getFieldSet())
      { 
        // This is the majority case
        t=subtypeTuple;
      }
      else
      {
        try
        { 
          // This was is a hot spot block, this the short circuit above
          t=widenTuple(subtypeTuple);
        }
        catch (DataException x)
        { throw new AccessException(x.toString(),x);
        }
      }
      
      if (t!=null)
      { 
        try
        { return t.get(index);
        }
        catch (DataException x)
        { throw new AccessException(x.toString(),x);
        }
        catch (IndexOutOfBoundsException x)
        {
          throw new AccessException
            ("Internal index error applying field "+getURI()+"("+getType().getURI()+")"
            +" to Tuple of type "+t.getFieldSet().getType()
            );
        }
      }
      else
      {
        throw new AccessException
          ("Field '"+getURI()+"' not in Tuple FieldSet "+subtypeTuple.getFieldSet()
          +" \r\n      fieldSet="+fieldSet
          );
      }

    }

    @Override
    public boolean isWritable()
    { return source.get() instanceof EditableTuple;
    }
    
    @Override
    protected boolean store(Object val)
    {
      EditableTuple t=(EditableTuple) source.get();
      if (t==null)
      { return false;
      }
      
      try
      { t=widenTuple(t);
      }
      catch (DataException x)
      { throw new AccessException(x.toString(),x);
      }
      
      if (t!=null)
      { 
        try
        { 
          t.set(index,val);
          return true;
        }
        catch (DataException x)
        { throw new AccessException(x.toString(),x);
        }
        
      } 
      else
      {
        throw new IllegalArgumentException
          ("Tuple does not have field "
           +getName()+" because it does not belong to FieldSet "
           +getFieldSet()
           );

      }      
    }
    
    
  }
}