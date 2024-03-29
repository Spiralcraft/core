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


import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Key;
import spiralcraft.data.Method;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeParameter;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.lang.ToDataTranslator;
import spiralcraft.data.lang.ToStringTranslator;
import spiralcraft.data.util.InstanceResolver;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.Translator;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleSet;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.refpool.URIPool;

/**
 * Core implementation of a Type
 */
public class TypeImpl<T>
  extends Type<T>
{  
  protected final ClassLog log=ClassLog.getInstance(getClass());
  protected final Level debugLevel
    =ClassLog.getInitialDebugLevel
      (getClass()
      ,ClassLog.getInitialDebugLevel(TypeImpl.class,null)
      );
  
  protected Class<T> nativeClass;
  protected SchemeImpl scheme;
  protected final TypeResolver resolver;
  protected final URI uri;
  protected final URI packageURI;
  protected String description;
  protected Type<?> archetype;
  protected Type<T> baseType;
  protected boolean aggregate=false;
  protected Type<?> contentType=null;
  protected boolean extendable;
  protected boolean abztract;
  protected Comparator<T> comparator;
  protected FieldSet unifiedFieldSet;
  protected RuleSet<Type<T>,T> ruleSet;
  protected Rule<Type<T>,T>[] explicitRules;
  
  protected boolean rulesResolved;
  protected Translator<?,T> externalizer;
  
  private HashMap<String,Field<?>> linkedFieldMap;
  
  protected boolean linked;  
  protected TypeParameter<?>[] parameters;
  protected HashMap<String,Object> typeArguments;
  
  
  public TypeImpl(TypeResolver resolver,URI uri)
  { 
    this.resolver=resolver;
    this.uri=uri;
    this.packageURI=resolver.getPackageURI(uri);
    this.debug=debugLevel.isDebug();
  }
  
  @Override
  public String getDescription()
  { return description;
  }
  
  public void setDescription(String description)
  { this.description=description;
  } 
  
  @SuppressWarnings("unchecked")
  protected void addRules(Rule<Type<T>,T> ... rules)
  {    
    if (ruleSet==null)
    { createRuleSet();
    }
    ruleSet.addRules(rules);
  }
  
  protected void setArgument(String name,Object value)
  {
    if (typeArguments==null)
    { typeArguments=new HashMap<>();
    }
    typeArguments.put(name,value);
  }
  
  @Override
  public <Targ> Targ getArgument(String name)
  { 
    @SuppressWarnings("unchecked")
    Targ ret
      =typeArguments!=null
        ?(Targ) typeArguments.get(name)
        :null;
    if (ret==null && archetype!=null)
    { ret=archetype.getArgument(name);
    }
 //   log.fine(getURI()+".getArgument("+name+") returned "+ret);
    return ret;
  }
  
  @SuppressWarnings("unchecked")
  public void setRules(Rule<Type<T>,T> ... rules)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked: "+getURI());
    }
    explicitRules=rules;
  }
  
  @Override
  public void setParameters(TypeParameter<?>[] parameters)
  { 
    this.parameters=parameters;
//    log.fine(getClass().getName()+"@"+System.identityHashCode(this)+" got parameters:"+getURI());
  }
  
  @Override
  public TypeParameter<?>[] getParameters()
  { return parameters;
  }
  
  private void createRuleSet()
  {
    RuleSet<Type<T>,T> baseRules
      =(baseType!=null)?(baseType.getRuleSet()):null;
        
    ruleSet=new RuleSet<Type<T>,T>(this,baseRules);

    if (explicitRules!=null)
    { ruleSet.addRules(explicitRules);
    }
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void resolveRules()
  {
    if (ruleSet==null 
        && (explicitRules!=null
            || (baseType!=null 
                && baseType.getRuleSet()!=null
               )
            )
       )
    { createRuleSet();
    }
    
    if (archetype!=null && archetype.getRuleSet()!=null)
    {
      if (debug)
      { 
        log.fine
          ("Adding archetype rules for "
          +getURI()
          +" from "
          +archetype.getURI()
          );
      }      
      if (ruleSet==null)
      { createRuleSet();
      }
      ruleSet.addRuleSet((RuleSet) archetype.getRuleSet());
    }
    rulesResolved=true;
  }
  
  @Override
  public URI getPackageURI()
  { return packageURI;
  }
  
  @Override
  public Type<?> getArchetype()
  { return archetype;
  }
  
  public void setArchetype(Type<T> archetype)
  { 
    if (linked)
    { throw new IllegalStateException("Type "+getURI()+" already linked: "+this);
    }
    this.archetype=archetype;
    combineArchetypeParams();
  }

  protected void combineArchetypeParams()
  {
    parameters
      =ArrayUtil.concat
        (TypeParameter[].class,archetype.getParameters(),parameters);
    if (parameters.length==0)
    { parameters=null;
    }
//    else
//    {
//      if (archetype.getParameters()!=null && archetype.getParameters().length>0)
//      { log.fine("Copied parameters from "+archetype.getURI()+" to "+getURI());
//      }
//    }
  }
   
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  /**
   * <p>Whether this specific type is patterned after the specified archetype
   * </p>
   * 
   * <p>Does not search base types by design. Use isAssignableFrom(type)
   * </p>
   */
  @Override
  public boolean hasArchetype(Type<?> type)
  {
    if (this==type)
    { return true;
    }
    else if (archetype!=null)
    { return archetype.hasArchetype(type);
    }
    else
    { return false;
    }
  }

  /**
   * 
   * @return The RuleSet associated with this type.
   */
  @Override
  public RuleSet<Type<T>,T> getRuleSet()
  { 
    if (!rulesResolved && ruleSet==null)
    { 
      if (debug)
      {
        
        // Need to create one to return if we haven't resolved our own rules yet
        log.debug("Creating placeholder RuleSet on demand for Type "+getURI());
      }
      createRuleSet();
    }
    if (ruleSet!=null)
    { return ruleSet;
    }
    else if (getBaseType()!=null)
    { return getBaseType().getRuleSet();
    }
    return null;
  }  
  
  @Override
  public Type<T> getBaseType()
  { return baseType;
  }
  
  @Override
  protected void selfFocusCreated(Focus<Type<T>> selfFocus)
    throws BindException
  {

  }
  
  public void setBaseType(Type<T> baseType)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    this.baseType=baseType;
  }
  
  @Override
  public boolean hasBaseType(Type<?> type)
  {
    if (this==type)
    { return true;
    }
    else if (baseType!=null)
    { return baseType.hasBaseType(type);
    }
    else
    { return false;
    }
  }

  @Override
  public boolean isAssignableFrom(Type<?> type)
  {
    
    if (type==null)
    { return false;
    }
    
    if (type==this)
    { return true;
    }
    
    if (type.isPrimitive())
    { 
      if (getNativeClass()!=null)
      { return getNativeClass().isAssignableFrom(type.getNativeClass());
      }
      else
      { return false;
      }
    }
    else if (type.hasArchetype(this))
    { return true;
    }
    else
    {
      // Check native compatability
      if (getNativeClass()!=null
          && type.getNativeClass()!=null
          && !getNativeClass().isAssignableFrom(type.getNativeClass())
          )
      { 
        if (debug)
        {
          log.fine
            (this.getURI()+"("+this.getNativeClass()+")"
            +" is not native assignable from "
            +type.getURI()+"("+type.getNativeClass()+")"
            );
        }
        return false;
      }
    
      Type<?> baseType=type;
      while (baseType!=null)
      {
        if (baseType.hasArchetype(this))
        { return true;
        }
        baseType=baseType.getBaseType();
      }
      if (debug)
      { log.fine(this.getURI()+" is not assignable from "+type.getURI());
      }
      return false;
    }
  }
  
  @Override
  public Type<?> getMetaType()
  { 
    try
    { return resolver.resolve(URIPool.create(uri.toString().concat(".type")));
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
  
  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   */
  @Override
  public Class<T> getNativeClass()
  { return nativeClass;
  }
  
  @Override
  public TypeResolver getTypeResolver()
  { return resolver;
  }
  
  @Override
  public URI getURI()
  { return uri;
  }

  @SuppressWarnings("unchecked") // Scheme is not genericized
  public Key<T> getPrimaryKey()
  { 
    link();
    Key<T> key=null;
    if (getScheme()!=null)
    { key=(Key<T>) getScheme().getPrimaryKey();
    }
    if (key==null && getBaseType()!=null)
    { key=getBaseType().getPrimaryKey();
    }
    return key;
    
  }  
  
  /**
   * Default implementation is to set up the SchemeImpl scheme with 
   *   arechetype and base type and resolve it.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Dealing with rule
  @Override
  public void link()
  { 
    if (linked)
    { return;
    }
    
    // log.log(Level.FINE,"Linking "+getURI()+" "+linkStack(),new Exception());
    pushLink(getURI());
    try
    {

      linked=true;
      
      if (baseType!=null)
      { baseType.link();
      }
      
      if (archetype!=null)
      { 
//        log.fine("Linking archetype "+archetype.getURI()
//          +" in "+getURI()+" "+getClass().getName()+"@"+System.identityHashCode(this)
//          );
        archetype.link();
      }
      
    
      if ( scheme==null
           && (baseType!=null 
               || (archetype!=null && archetype.getScheme()!=null
                  )
              )
         )
      { 
        // Null subtype scheme causes problems
        scheme=new SchemeImpl();
      }
    
      if (scheme!=null)
      {
        scheme.setType(this);
        if (archetype!=null && archetype.getScheme()!=null)
        { scheme.setArchetypeScheme(archetype.getScheme());
        }
        scheme.resolve();
      
        // Add contextual field based rules
        for (FieldImpl<?> field:scheme.getFields())
        {
          if (field.isUniqueValue() && !isAbstract())
          { 
            // Abstract types don't get set-based rules
            if (debug)
            { log.fine("Adding field unique rule for "+field.getURI());
            }
            addRules((Rule<Type<T>,T>) new UniqueRule(this,field));
          }
        
          if (field.isRequired())
          {
            if (debug)
            { log.fine("Adding field required rule for "+field.getURI());
            }
            addRules((Rule<Type<T>,T>) new RequiredRule(this,field));
          }
        
        }
      }
    
      resolveRules();
      
      if (archetype!=null && archetype.getMethods()!=null)
      {
//        if (archetype.isAbstract())
//        { log.fine(getURI()+" Checking archetype methods in "+archetype.getURI());
//        }
        for (Method method: archetype.getMethods())
        {
//          if (archetype.isAbstract() || method.getName()=="getItemForId")
//          { log.fine(getURI()+" Checking archetype method "+method);
//          }
          if (method.isGeneric() && !hasLocalMethod(method.getName()))
          { 
            MethodImpl newMethod=((MethodImpl) method).extend();
            newMethod.setGeneric(true);
            newMethod.setDataType(this);
//            log.fine(getURI()+" Extended method "+method+" to "+newMethod);
            methods.add(newMethod);
          }
        }
      }
      
      if (methods!=null)
      {
        for (Method method:methods)
        { ((MethodImpl) method).resolve();
        }
      }
      
      optimizeMemberLookup();

    }
    catch (Exception x)
    { throw new RuntimeDataException("Error linking "+uri,x);
    }
    finally 
    { popLink();
    }
        

    
  }
  
  private boolean hasLocalMethod(String name)
  { 
    for (Method method:methods)
    { 
      if (method.getName()==name)
      { return true;
      }
    }
    return false;
    
  }

  protected void optimizeMemberLookup()
  {
    linkedFieldMap=new HashMap<String,Field<?>>();
    
    if (baseType!=null)
    { 
      
      // Note- the baseType might not be completely linked here due
      //   to circular references so this may not be complete
      for (Field<?> field: baseType.getFieldSet().fieldIterable())
      { linkedFieldMap.put(field.getName(),field);
      }
    }
      
    if (scheme!=null)
    {
      for (Field<?> field : scheme.fieldIterable())
      { linkedFieldMap.put(field.getName(),field);
      }
    }
  }
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  @Override
  public Scheme getScheme()
  { 
    link();
    return scheme;
  }
  
  @Override
  protected Scheme scheme()
  { return scheme;
  }
  
  /**
   * 
   * Allows Class definitions to define Fields for a Type without having to
   *   create an empty scheme.
   * 
   * @param fields
   */
  public void setFields(List<FieldImpl<?>> fields)
  {
    if (linked)
    { throw new IllegalStateException("Type already linked: "+toString());
    }
    if (scheme==null)
    { scheme=new SchemeImpl();
    }
    scheme.setFields(fields);
  }
  
  /**
   * 
   * Define Methods for this type
   * 
   * @param fields
   */
  public void setMethods(Method[] methods)
  {
    if (linked)
    { throw new IllegalStateException("Type already linked: "+toString());
    }
    for (Method method:methods)
    {
      ((MethodImpl) method).setDataType(this);
      this.methods.add(method);
    }

  }

  /**
   * Returns the Field with the specified name in this type or a base Type
   * @param name
   * @return
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Field<X> getField(String name)
  {
    link();

    Field<X> field=null;
    if (linkedFieldMap!=null)
    { 
      synchronized (linkedFieldMap)
      { field=(Field<X>) linkedFieldMap.get(name);
      }
      if (field!=null)
      { return field;
      }
      
    }
    
    if (field==null)
    {
      if (getScheme()!=null)
      { field=getScheme().<X>getFieldByName(name);
      }
      if (field==null && getBaseType()!=null)
      { field=getBaseType().<X>getField(name);
      }
    }
    
    if (field!=null && linkedFieldMap!=null)
    { 
      // When linkedFieldMap was created, the baseType might not have
      //   finished linking.
      synchronized (linkedFieldMap)
      { linkedFieldMap.put(name,field);
      }
    }
    return field;
  }

  /**
   * Returns a unified FieldSet that contains fields of this Type and of all 
   *   base Types
   */
  @Override
  public synchronized FieldSet getFieldSet()
  { 
    link();
    
    if (baseType==null && scheme!=null)
    { return scheme;
    }
    if (unifiedFieldSet==null)
    { unifiedFieldSet=new UnifiedFieldSet(this);
    }
    return unifiedFieldSet;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void setKeys(Key<T>[] keyArray)
  { 
    if (scheme==null)
    { scheme=new SchemeImpl();
    }
    scheme.setKeys( ArrayUtil.<Key,KeyImpl>convert(KeyImpl.class,keyArray) );

  }
  
  public void setScheme(Scheme scheme)
  {
    if (linked)
    { throw new IllegalStateException("Type already linked: "+toString());
    }
    
    if (this.scheme!=null && this.scheme.getFieldCount()>0)
    {
      // System.out.println("SETTING SCHEME");
      log.warning
        ("Overriding non-empty scheme: Type "
        +getURI()+" "
        +scheme.toString()
        );
    }
    this.scheme=(SchemeImpl) scheme;
  }
  
  
  /**
   * @return Whether this Type is an aggregate (array or collection) of another
   *   type.
   */
  @Override
  public boolean isAggregate()
  { return aggregate;
  }
  
  @Override
  public Type<?> getCoreType()
  {
    Type<?> ret=this;
    while (ret.isAggregate())
    { ret=ret.getContentType();
    }
    return ret;
  }

  @Override
  public boolean isPrimitive()
  { return false;
  }
  
  @Override
  public boolean isStringEncodable()
  { return false;
  }
  
  @Override
  public boolean isDataEncodable()
  { return true;
  }
    
  @Override
  public String toString()
  { 
    return super.toString()
      +":"+(uri!=null?uri.toString():"(delegated)")+":"
      +(linked?"":"linked=false:")
      +(scheme!=null?scheme.contentsToString():"(no scheme)")
      +(baseType!=null?"\r\nBase Type "+baseType:"")
      ;
  }
  
  @Override
  public String toString(T val)
  { throw new UnsupportedOperationException("Not string encodable");
  }
  
  @Override
  public T fromString(String val)
    throws DataException
  { throw new UnsupportedOperationException("Not string encodable");
  }

  @SuppressWarnings("unchecked")
  @Override
  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { 
    if (nativeClass==null || nativeClass.isAssignableFrom(data.getClass()))
    { return (T) data;
    }
    throw new UnsupportedOperationException
      ("Not depersistable: type="+toString()+"  data="+data);
  }
  
  @Override
  public DataComposite toData(T val)
    throws DataException
  { throw new UnsupportedOperationException("Not persistable: "+toString());
  }
  
  @Override
  public Type<?> getContentType()
  { return contentType;
  }

  @Override
  public boolean isAbstract()
  { return abztract;
  }

  public void setAbstract(boolean abztract)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    
    this.abztract=abztract;
  }
  


  @Override
  public boolean isExtendable()
  { return extendable;
  }

  public void setExtendable(boolean extendable)
  { 
    if (linked)
    { throw new IllegalStateException("Type already linked");
    }
    
    this.extendable=extendable;
  }
  
  @Override
  public boolean isLinked()
  { return linked;
  }
  
  @Override
  public synchronized Translator<?,T> getExternalizer()
    throws DataException
  { 
    if (externalizer==null)
    { 
      if (isDataEncodable())
      { externalizer=new ToDataTranslator<T>(this);
      }
      else if (isStringEncodable())
      { externalizer=new ToStringTranslator<T>(this);
     }
    }
    return externalizer;
  }
  
  @Override
  public Comparator<T> getComparator()
  { return comparator;
  }
  
  public void setComparator(Comparator<T> comparator)
  { this.comparator=comparator;
  }

  
  @SuppressWarnings("unused")
  private void assertLinked()
  {
    if (!linked)
    { 
      throw new IllegalStateException
        ("Type not linked: "+getURI()+" Link stack:"
          +"\r\n  "+ArrayUtil.format(linkStack(),"\r\n  ","")
        );
    }
  }

}
