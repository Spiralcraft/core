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
import spiralcraft.data.Method;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.util.InstanceResolver;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.rules.Rule;
import spiralcraft.rules.RuleSet;

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
  protected Type<?> archetype;
  protected Type<T> baseType;
  protected boolean aggregate=false;
  protected Type<?> contentType=null;
  protected boolean extendable;
  protected boolean abztract;
  protected Comparator<T> comparator;
  protected FieldSet unifiedFieldSet;

  
  private boolean linked;
  
  public TypeImpl(TypeResolver resolver,URI uri)
  { 
    this.resolver=resolver;
    this.uri=uri;
    this.packageURI=resolver.getPackageURI(uri);
    this.debug=debugLevel.canLog(Level.DEBUG);
  }
  
  protected void addRules(Rule<Type<T>,T> ... rules)
  {    
    if (ruleSet==null)
    { createRuleSet();
    }
    ruleSet.addRules(rules);
  }
  
  @SuppressWarnings("unchecked") // Vararg array and Rule cast from archetyp
  private void createRuleSet()
  {
    RuleSet<Type<T>,T> baseRules
      =(baseType!=null)?(baseType.getRuleSet()):null;
        
    ruleSet=new RuleSet<Type<T>,T>(this,baseRules);
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
      for (Rule<?,?> rule : archetype.getRuleSet())
      { ruleSet.addRules((Rule<Type<T>,T>) rule);
      }
    }
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
    { throw new IllegalStateException("Type already linked");
    }
    this.archetype=archetype;
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
  
  @Override
  public Type<T> getBaseType()
  { return baseType;
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
    { return getNativeClass().isAssignableFrom(type.getNativeClass());
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
    { return resolver.resolve(URI.create(uri.toString().concat(".type")));
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

  
  /**
   * Default implementation is to set up the SchemeImpl scheme with 
   *   arechetype and base type and resolve it.
   */
  @SuppressWarnings("unchecked") // Dealing with rule
  @Override
  public void link()
    throws DataException
  { 
    if (linked)
    { return;
    }
    
    if (debug)
    { log.fine("Linking Type "+getURI());
    }
    linked=true;
    
    if (baseType!=null && scheme==null)
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
        if (field.isUniqueValue())
        { 
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
    if (methods!=null)
    {
      for (Method method:methods)
      { ((MethodImpl) method).resolve();
      }
    }
    
    if (ruleSet==null)
    { 
      if (archetype!=null
          && archetype.getRuleSet()!=null
         )
      { createRuleSet();
      }
    }
    
        
    if (debug)
    { log.fine("Done Linking Type "+getURI());
    }
    
  }
    
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  @Override
  public Scheme getScheme()
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
   * Returns the Field with the specified name in this type or a base Type
   * @param name
   * @return
   */
  @Override
  public <X> Field<X> getField(String name)
  {
    if (!linked)
    { throw new IllegalStateException("Type not linked: "+this);
    }
    Field<X> field=null;
    if (getScheme()!=null)
    { field=getScheme().<X>getFieldByName(name);
    }
    if (field==null && getBaseType()!=null)
    { field=getBaseType().<X>getField(name);
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
    if (!linked)
    { 
      throw new IllegalStateException
        ("Call to getFieldSet() before type linked "+getURI());
    }
//    if (!linked && debug)
//    { 
//      log.log(Level.DEBUG,"Call to getFieldSet() before type linked "+getURI()
//        ,new Exception("trace")
//      );
//    }
    
    if (baseType==null && scheme!=null)
    { return scheme;
    }
    if (unifiedFieldSet==null)
    { unifiedFieldSet=new UnifiedFieldSet(this);
    }
    return unifiedFieldSet;
  }
  
  public void setKeys(KeyImpl<Tuple>[] keyArray)
  { 
    if (scheme==null)
    { scheme=new SchemeImpl();
    }
    scheme.setKeys(keyArray);

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

  @Override
  public T fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { throw new UnsupportedOperationException("Not depersistable");
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
  public Comparator<T> getComparator()
  { return comparator;
  }
  
  public void setComparator(Comparator<T> comparator)
  { this.comparator=comparator;
  }
  
}
