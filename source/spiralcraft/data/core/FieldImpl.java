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
import spiralcraft.log.ClassLog;
import spiralcraft.rules.Inspector;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleException;
import spiralcraft.rules.RuleSet;
import spiralcraft.rules.Violation;

import spiralcraft.data.lang.DataReflector;

import java.net.URI;

@SuppressWarnings("unchecked")
/**
 * <P>Implementation of a standard Field.
 */
public class FieldImpl<T>
  implements Field<T>
{
  protected final ClassLog log=ClassLog.getInstance(getClass());
  
  private boolean locked;
  private FieldSet fieldSet;
  private int index;
  private String name;
  private String title;
  private Type<T> type;
  private Field archetypeField;
  private URI uri;
  private boolean isScheme;
  private Expression<T> defaultExpression;
  private Expression<T> fixedExpression;
  private Expression<T> newExpression;
  private RuleSet<FieldImpl<T>,T> ruleSet;
  private Rule<FieldImpl<T>,T>[] explicitRules;

  private boolean uniqueValue;
  private boolean required;
  private boolean tranzient;
  
  protected boolean debug;

  protected Reflector contentReflector;

//  protected boolean debugData;
  
  
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
  
  public Reflector<T> getContentReflector()
  { return contentReflector;
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
   * <p>Indicates that the field must contain a unique value, if non-null, 
   *   across all instances available in the space. This is less restrictive
   *   than a Unique key, because it allows for multiple null values
   * </p>
   * 
   * @param unique
   */
  public void setUniqueValue(boolean uniqueValue)
  { this.uniqueValue=uniqueValue;
  }
  
  public boolean isUniqueValue()
  { 
    return this.uniqueValue 
      || (archetypeField!=null && archetypeField.isUniqueValue());
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

  public RuleSet<? extends FieldImpl<T>,T> getRuleSet()
  { return ruleSet;
  }
  
  public void setRules(Rule<FieldImpl<T>,T>[] rules)
  { explicitRules=rules;
  }
  
  protected void addRules(Rule<FieldImpl<T>,T> ... rules)
  {
    if (ruleSet==null)
    { 
      ruleSet
        =new RuleSet<FieldImpl<T>,T>
          (this,archetypeField!=null?archetypeField.getRuleSet():null);
    }
    ruleSet.addRules(rules);
  }
  
  public Expression<T> getNewExpression()
  { 
    return (newExpression==null && archetypeField!=null)
      ?archetypeField.getNewExpression()
      :newExpression;
  }
  
  public void setNewExpression(Expression<T> newExpression)
  { this.newExpression=newExpression;
  }

  public Expression<T> getDefaultExpression()
  { 
    return (defaultExpression==null && archetypeField!=null)
      ?archetypeField.getDefaultExpression()
      :defaultExpression;
  }
  
  public void setDefaultExpression(Expression<T> defaultExpression)
  { this.defaultExpression=defaultExpression;
  }
  
  public Expression<T> getFixedExpression()
  { 
    return (fixedExpression==null && archetypeField!=null)
      ?archetypeField.getFixedExpression()
      :fixedExpression;
  }
  
  public void setFixedExpression(Expression<T> fixedExpression)
  { this.fixedExpression=fixedExpression;
  }

  /**
   *@return Whether this field has the same type, constraints and attributes
   *   as the specified field.
   */
  public boolean isFunctionalEquivalent(Field<?> field)
  { 
    return field.getType()==getType()
      && newExpression==null
      && defaultExpression==null
      && fixedExpression==null
      && explicitRules==null
      && tranzient==field.isTransient()
      && (required?field.isRequired():true)
      && (uniqueValue?field.isUniqueValue():true)
      && (title!=null?title.equals(field.getTitle()):true)
      ;
        
  }
  
  /**
   *@return Whether this field is stored or whether it is recomputed every time
   *  the value is accessed.
   */
  public boolean isTransient()
  { return tranzient;
  }
  

  public void setTransient(boolean tranzient)
  { this.tranzient=tranzient;
  }
  
  
  public void setRequired(boolean required)
  { this.required=required;
  }
  
  @Override
  public boolean isRequired()
  { return required || (archetypeField!=null && archetypeField.isRequired());
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
    { return archetypeField!=null?archetypeField.getTitle():name;
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
   * 
   * @throws DataException
   */
  public void setType(Type<T> type)
    throws DataException
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
  
  public Type<T> getType()
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
  
  public final T getValue(Tuple t)
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
      T val=getValueImpl(t);
      // System.err.println("FieldImpl "+getURI()+": getValue()="+val);
      return val;
    }

    throw new IllegalArgumentException
      ("Tuple does not belong to FieldSet "+getFieldSet());
      
  }
  

  public final void setValue(EditableTuple t,T value)
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
    
    if (explicitRules!=null)
    { addRules(explicitRules);
    }    
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
  protected T getValueImpl(Tuple tuple)
    throws DataException
  { return (T) tuple.get(index);
  }

  /**
   * Implement the field data value update mechanism
   * 
   * @param tuple The EditableTuple to be updated
   * @param value The data value to update
   * @throws DataException
   */
  protected void setValueImpl(EditableTuple tuple,T value)
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
  
  /**
   * <p>Log link stage and special conditions for debugging purposes
   * </p>
   * 
   * 
   * @param debug
   */
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Log data updates
   * </p>
   * 
   * @param debugData
   */
//  public void setDebugData(boolean debugData)
//  { this.debugData=debugData;
//  }

  @Override
  public Channel<T> bindChannel
    (Focus<Tuple> focus)
    throws BindException
  { 
    Channel source=focus.getSubject();
    Channel binding=source.getCached(this);
    if (binding==null)
    { 
      binding=new FieldChannel(focus);
      source.cache(this,binding);
    }
    return binding;
  }
  
  public class FieldChannel
    extends AbstractChannel<T>
  {
    protected final Channel<? extends Tuple> source;
    protected final Inspector<FieldImpl<T>,T> inspector;
    protected final Inspector<Type<T>,T> typeInspector;
    
    public FieldChannel(Focus<? extends Tuple> focus)
      throws BindException
    { 
      super(contentReflector);
      this.source=focus.getSubject();
      
      if (ruleSet!=null)
      { inspector=ruleSet.bind(contentReflector,focus);
      }
      else
      { inspector=null;
      }
      
      if (type.getRuleSet()!=null)
      { 
        typeInspector=getType().getRuleSet().bind
          (contentReflector,focus);
      }
      else
      { typeInspector=null;
      }

    }

    @Override
    protected T retrieve()
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
        { return (T) t.get(index);
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
    protected boolean store(T val)
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
        if (typeInspector!=null)
        {
          Violation<T>[] violations=typeInspector.inspect(val);
          if (violations!=null)
          { 
            throw new AccessException
              (new RuleException(violations));
          }
        }

        if (inspector!=null)
        {
          Violation<T>[] violations=inspector.inspect(val);
          if (violations!=null)
          { 
            throw new AccessException
              (new RuleException(violations));
          }
        }
        
        
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