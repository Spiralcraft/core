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

import spiralcraft.common.ContextualException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.RuntimeDataException;
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
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.rules.Inspector;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleException;
import spiralcraft.rules.RuleSet;
import spiralcraft.rules.Violation;
import spiralcraft.ui.MetadataType;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.thread.BlockTimer;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.reflect.ReflectionType;

import java.net.URI;
import java.util.WeakHashMap;

@SuppressWarnings({"unchecked","rawtypes"})
/**
 * <P>Implementation of a standard Field.
 */
public class FieldImpl<T>
  implements Field<T>
{
  
      
  protected final ClassLog log=ClassLog.getInstance(getClass());
  
  
  private boolean locked;
  private int index;
  private String name;
  private String title;
  private String description;
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
  
  protected Tuple defaultUIMetadata;
  protected Channel uiMetadataChannel;
  protected Channel fieldMetadataChannel;
  
  protected boolean debug;
  protected boolean timeReads=false;

  private Reflector contentReflector;
  
  protected FieldSet fieldSet;

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
//        =URIPool.create(scheme.getType().getURI().toString()+"#"+getName());
//    }
//    else
//    {
//      this.uri
//        =URIPool.create("untyped#"+getName());
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
//      this.uri=URIPool.create("untyped#"+getName());
    }
    
  }
  
  @Override
  public Reflector<T> getContentReflector()
  { 
    assertType();
    if (contentReflector==null)
    { log.log(Level.WARNING,"Content reflector is null for "+getURI());
    }
    return contentReflector;
  }
  
  public Field getArchetypeField()
  { return archetypeField;
  }
  

  
  public void setArchetypeField(Field field)
    throws DataException
  {
    assertUnlocked();
    assertType();
    if (typeIsNull())
    { updateType(resolveType());
    }
    
    archetypeField=field;
    this.index=archetypeField.getIndex();
    
    if (archetypeField.getType()==null)
    { throw new DataException(archetypeField.getURI()+" has no type");
    }
    
    if (!archetypeField.getType().isAssignableFrom(this.getType()))
    { 
      generateURI();
      throw new TypeMismatchException
        ("Field "+getURI()+"'"
        +" cannot extend field "+archetypeField.getURI()
        +" as types are not compatible"
        ,field.getType()
        ,this.getType()
        );
    }
  }
  
  /**
   *@return the owning FieldSet
   */
  @Override
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
  
  /**
   * <p>The default UI metadata for this field, in the form of a Tuple of
   *   type class:/spiralcraft/ui/FieldMetadata
   * </p>
   * 
   * @param uiMetadata
   */
  public void setDefaultUIMetadata(Tuple uiMetadata)
  { this.defaultUIMetadata=uiMetadata;
  }
  
  @Override
  public boolean isUniqueValue()
  { 
    return this.uniqueValue 
      || (archetypeField!=null && archetypeField.isUniqueValue());
  }
  
  /**
   *@return the Scheme
   */
  public SchemeImpl getScheme()
  { 
    if (fieldSet instanceof Scheme)
    { return (SchemeImpl) fieldSet;
    }
    else
    { return null;
    }
  }

  @Override
  public URI getURI()
  { 
    if (uri==null)
    { 
      // We might not have all the information we need pre-resolution when
      //   the URI field is null, so just do the best we can.
      return createURI();
    }
    else
    { return uri;
    }
  }

  @Override
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
  
  @Override
  public Expression<T> getNewExpression()
  { 
    return (newExpression==null && archetypeField!=null)
      ?archetypeField.getNewExpression()
      :newExpression;
  }
  
  public void setNewExpression(Expression<T> newExpression)
  { this.newExpression=newExpression;
  }

  @Override
  public Expression<T> getDefaultExpression()
  { 
    return (defaultExpression==null && archetypeField!=null)
      ?archetypeField.getDefaultExpression()
      :defaultExpression;
  }
  
  public void setDefaultExpression(Expression<T> defaultExpression)
  { this.defaultExpression=defaultExpression;
  }
  
  @Override
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
  @Override
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
  @Override
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
  
  @Override
  public String getName()
  { return name;
  }
  
  @Override
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

  @Override
  public String getDescription()
  { 
    if (description==null)
    { return archetypeField!=null?archetypeField.getDescription():null;
    }
    else
    { return description;
    }
  }
  
  public void setDescription(String description)
  { 
    assertUnlocked();
    this.description=description;
  }
  

  /**
   * Set the data Type
   * 
   * @throws DataException
   */
  public void setType(Type<T> type)
    throws DataException
  { 
    if (type==null)
    { throw new IllegalArgumentException("Field type cannot be null");
    }
    assertUnlocked();
    updateType(type);
  }
  
  protected boolean typeIsNull()
  { return type==null;
  }
  
  @Override
  public Type<T> getType()
  { 
    if (locked && type==null)
    {
      // Late-resolved type
      synchronized (this)
      {
        if (type==null)
        {
            
          try
          { 
            updateType(resolveType());
            if (type==null)
            { 
              throw new RuntimeDataException
                ("Could not resolve type for field "+getURI(),null);
            }  
          }
          catch (DataException x)
          { 
            throw new RuntimeDataException
              ("Could not resolve type for field "+getURI(),x);
          }
        }
      }
    }
    return type;
  }
  
  protected void assertType()
  { 
    if (typeIsNull())
    {
      try
      { updateType(resolveType());
      }
      catch (DataException x)
      { 
        throw new RuntimeDataException
          ("Error asserting type for field "+getURI(),x);
      }
    }
  }
  
  protected void updateType(Type type) 
  {
    if (type==null)
    { throw new IllegalArgumentException("Type is null for field "+getURI());
    }
    this.type=type;
    try
    { this.contentReflector=DataReflector.getInstance(type);
    }
    catch (BindException x)
    { throw new RuntimeException(x);
    }
  }

  /**
   * Called before resolve() when a definitive Type is needed.
   *    
   * @throws DataException
   */
  protected Type resolveType()
    throws DataException
  { return null;
  }
  
  protected final Tuple widenTuple(Tuple t)
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
  
  @Override
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
      
      try
      {
        if (timeReads)
        { BlockTimer.instance().push();
        }
        T val=getValueImpl(t);
        // System.err.println("FieldImpl "+getURI()+": getValue()="+val);
        return val;
      }
      finally
      {
        if (timeReads)
        { 
          log.fine
            ("Read "+getURI()+": "
              +BlockTimer.instance().elapsedTimeFormatted()
            );
          BlockTimer.instance().pop();
        }
      }
      
    }

    throw new IllegalArgumentException
      ("Tuple does not belong to FieldSet "+getFieldSet());
      
  }
  

  @Override
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
        ("Tuple "+t+" does not belong to FieldSet "+getFieldSet());
    }
  }
  
  @Override
  public final boolean isDirty(DeltaTuple dt)
    throws DataException
  {
    if (dt==null)
    {
      throw new IllegalArgumentException
        ("DeltaTuple cannot be null");
    }
    
    dt=(DeltaTuple) widenTuple(dt);
    
    if (dt!=null)
    { return dt.isDirty(index);
    }
    else
    {
      throw new IllegalArgumentException
        ("Tuple "+dt+" does not belong to FieldSet "+getFieldSet());
    }
    
  }
  
  /**
   * Prevent further changes to this Field definition
   */
  void lock()
  { locked=true;
  }
  
  /**
   * Resolve any external dependencies.
   */
  void resolve()
    throws DataException
  {
    if (locked)
    { return;
    }
    lock();
    
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
  
  
  
  private URI createURI()
  {
    if (fieldSet.getType()==null)
    { return URIPool.create("untyped#"+getName());
    }
    else
    { return
        URIPool.create(fieldSet.getType().getURI().toString()+"#"+getName());

    }
    
  }
  
  
  protected void generateURI()
  { this.uri=createURI();
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
  
  public void setTimeReads(boolean timeReads)
  { this.timeReads=timeReads;
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
    (Channel<Tuple> source,Focus<?> focus,Expression<?>[] params)
    throws BindException
  { 
    Channel binding=source.getCached(this);
    if (binding==null)
    { 
      assertType();
      binding=new FieldChannel(source,focus);
      source.cache(this,binding);
    }
    return binding;
  }
  
  @Override
  public synchronized <X> Channel<X> resolveMeta(URI typeURI)
    throws ContextualException
  {
    if (typeURI.equals(MetadataType.FIELD.uri))
    { 
      if (uiMetadataChannel==null)
      { 
        uiMetadataChannel
          =new ConstantChannel
            (MetadataType.FIELD.reflector
            ,defaultUIMetadata
            );
      }
      return uiMetadataChannel;
    }
    else if (spiralcraft.data.types.meta.MetadataType.FIELD.uri.equals(typeURI)
          || BeanReflector.getInstance(FieldImpl.this.getClass())
              .isAssignableTo(typeURI)
        )
    { return getFieldMetadataChannel();
    }
    else if (ReflectionType.canonicalURI(getClass()).equals(typeURI))
    { return getFieldMetadataChannel();
    }
    else if (archetypeField!=null)
    { return archetypeField.resolveMeta(typeURI);
    }

    return null;
  }

  private Channel getFieldMetadataChannel()
  {
    if (fieldMetadataChannel==null)
    { 
      fieldMetadataChannel
        =new ConstantChannel
          (BeanReflector.getInstance(FieldImpl.this.getClass())
          ,FieldImpl.this
          );
    }
    return fieldMetadataChannel;
  }
  
  public class FieldChannel
    extends SourcedChannel<Tuple,T>
  {
    protected final Inspector<FieldImpl<T>,T> inspector;
    protected final Inspector<Type<T>,T> typeInspector;
    protected final Focus<?> focus;
    
    protected WeakHashMap<Type,Channel<T>> polyBindings;
    
    
    public FieldChannel
      (Channel<Tuple> source
      ,Focus<?> focus
      )
      throws BindException
    { 
      super(contentReflector,source);
      this.focus=focus;
      
      if (ruleSet!=null)
      { inspector=ruleSet.bind(contentReflector,focus);
      }
      else
      { inspector=null;
      }
      
      if (getType().getRuleSet()!=null)
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

      Tuple subtypeTuple;
      try
      { subtypeTuple=source.get();
      }
      catch (AccessException x)
      { throw new AccessException("Error reading field "+getURI(),x);
      }
      catch (ClassCastException x)
      { 
        throw new AccessException
          ("Error reading field "+getURI()+" from source "+source+" got ["+source.get()+"]",x);
      }
      
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
        { 
          // If we've widened, check for polymorphic field override
          Channel<T> polyChannel
            =(t!=subtypeTuple)
            ?polyChannel(subtypeTuple.getType())
            :null;
          if (polyChannel!=null)
          { 
            // Field is polymorphic for actual subtype
            return polyChannel.get();
          }
          else
          { return (T) t.get(index);
          }
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

    /**
     * Returns a delegated binding for this Field when overridden by
     *   a subtype field.
     * 
     * @param subtype
     * @return
     */
    protected Channel<T> polyChannel(Type<?> subtype)
    {
      
      // This field may be overridden in the subtype
      if (polyBindings==null)
      {
        synchronized(this)
        { 
          if (polyBindings==null)
          { polyBindings=new WeakHashMap<Type,Channel<T>>();
          }
        }
      }
      
      Channel<T> channel=polyBindings.get(subtype);
      if (channel==null)
      { 
        synchronized(polyBindings)
        {
          channel=polyBindings.get(subtype);
          if (channel==null)
          { 
            Field field=subtype.getField(getName());
            if (field!=FieldImpl.this)
            { 
              try
              { 
                Channel<? extends Tuple> subSource
                  =source.getCached("_"+subtype.getURI());
                if (subSource==null)
                { 
                  Reflector subtypeReflector=DataReflector.getInstance(subtype);
                  subSource
                    =new AspectChannel
                      (subtypeReflector,source);
                  source.cache("_"+subtype.getURI(),subSource);
                }
                  		
                
                
                channel=field.bindChannel
                  (subSource,focus.getParentFocus(),null);
              }
              catch (BindException x)
              { 
                throw new AccessException
                  ("Dynamic polymorphic binding failure for field "
                  +field.getURI()
                  ,x);
              }
            }
            else
            { channel=this;
            }
            polyBindings.put(subtype,channel);
          }
        }
      }
      
      if (channel!=this)
      { return channel;
      }
      else
      { return null;
      }
    }
    
    @Override
    public boolean isWritable()
    { return source.get() instanceof EditableTuple;
    }
    
    @Override
    protected boolean store(T val)
    {
      Tuple tuple=source.get();
      if (tuple==null)
      { 
        log.warning("Assignment failed to "+uri+"- tuple is null");
        return false;
      }
      if (!tuple.isMutable())
      { 
        log.warning("Assignment failed to "+uri+"- tuple is not editable");
        return false;
      }
      EditableTuple t=(EditableTuple) tuple;

      
      try
      { t=widenTuple(t);
      }
      catch (DataException x)
      { throw new AccessException(x.toString(),x);
      }
      
      if (t!=null)
      { 
        if (val!=null 
            && type.isPrimitive()
            && type.getNativeClass()!=val.getClass()
            && !type.getNativeClass().isAssignableFrom(val.getClass())
            )
        { 
          
          AccessException ax=new AccessException
            ("Type error setting field "+getURI()+" : expected value of "
            +type.getNativeClass()+" but got "
            +val.getClass()
            );
          log.log(Level.WARNING,"Type error",ax);
          throw ax;
        }
        
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
    
    @Override
    public synchronized <X> Channel<X> resolveMeta(Focus<?> focus,URI typeURI)
      throws BindException
    { 
      try
      {
        Channel<X> meta=FieldImpl.this.resolveMeta(typeURI);
        if (meta!=null)
        { return meta;
        }
        return super.resolveMeta(focus,typeURI);
      }
      catch (ContextualException x)
      { throw new BindException("Error resolving metadata "+typeURI,x);
      }
    }
    
    @Override
    public String toString()
    { return super.toString()+":"+uri;
    }
    
  }
}