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
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeParameter;
import spiralcraft.data.lang.ToDataTranslator;
import spiralcraft.data.lang.ToStringTranslator;
import spiralcraft.lang.spi.Translator;
import spiralcraft.log.ClassLog;
import spiralcraft.rules.RuleSet;
import spiralcraft.util.ArrayUtil;
import spiralcraft.util.refpool.URIPool;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Implementation base class for common aggregate type functionality
 */
public abstract class AbstractAggregateType<T,Tcontent>
  extends Type<T>
{
  protected static final ClassLog log
    =ClassLog.getInstance(AbstractAggregateType.class);
  
  protected final URI uri;
  
  protected Type<Tcontent> contentType;
  protected Class<T> nativeClass;
  protected Type<?> archetype;
  protected Type<T> baseType;
  protected boolean linked;
  protected RuleSet<Type<T>,T> ruleSet;  
  protected String description;
  protected Translator<?,T> externalizer;
  protected TypeParameter<?>[] parameters;
  protected HashMap<String,Object> typeArguments;
  protected SchemeImpl scheme;
  protected UnifiedFieldSet unifiedFieldSet;

  protected AbstractAggregateType(URI uri)
  { this.uri=uri;
  }
  
  @Override
  public Type<?> getArchetype()
  { return archetype;
  }

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
  public URI getPackageURI()
  { return baseType.getPackageURI();
  }
  
  @Override
  public String getDescription()
  { return description;
  }
  
  public void setDescription(String description)
  { this.description=description;
  } 
  
  @Override
  public Type<T> getBaseType()
  { return baseType;
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
    if (type.hasArchetype(this))
    { return true;
    }
    if (getNativeClass()!=null && type.getNativeClass()!=null &&
        !getNativeClass().isAssignableFrom(type.getNativeClass())
        )
    { return false;
    }
    return getContentType().isAssignableFrom(type.getContentType());
  }
  
  @Override
  public Type<?> getMetaType()
  { 
    try
    { return getTypeResolver().resolve(URIPool.create(uri.toString().concat(".type")));
    }
    catch (DataException x)
    { throw new RuntimeException(x);
    }
  }
  
  @Override
  public <Targ> Targ getArgument(String name)
  { 
    @SuppressWarnings("unchecked")
    Targ ret
      =typeArguments!=null
        ?(Targ) typeArguments.get(name)
        :null;
    if (ret==null && contentType!=null)
    { ret=contentType.getArgument(name);
    }
    //log.fine(getURI()+".getArgument("+name+") returned "+ret);
    return ret;
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

  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   */
  @Override
  public Class<T> getNativeClass()
  { return nativeClass;
  }

  /**
   * Aggregate Types are always represented by Aggregates in data
   */
  @Override
  public boolean isPrimitive()
  { return false;
  }
  
  @Override
  public URI getURI()
  { return uri;
  }
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   *   
   * XXX Need an aggregateScheme- great for indexes, computations, etc.  
   */
  @Override
  public Scheme getScheme()
  { 
    link();
    return this.scheme;
  }
  
  @Override
  protected Scheme scheme()
  { return this.scheme;
  }
  
  /**
   * @return Fields here and in Base types.
   * 
   * XXX Need an aggregateScheme- great for indexes, computations, etc.  
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
  
  /**
   * Aggregates don't have fields, for now
   */
  @Override
  public <X> Field<X> getField(String name)
  { return null;
  }
  
  /**
   * 
   * @return The RuleSet associated with this type.
   */
  @Override
  public RuleSet<Type<T>,T> getRuleSet()
  { 
    if (ruleSet!=null)
    { return ruleSet;
    }
    else if (getBaseType()!=null)
    { return getBaseType().getRuleSet();
    }
    return null;
  }  
  
  @Override
  public boolean isAggregate()
  { return true;
  }
  
  @Override
  public Type<?> getContentType()
  { return contentType;
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
  public void setParameters(TypeParameter<?>[] parameters)
  { 
    this.parameters=parameters;
    // log.fine(getClass().getName()+"@"+System.identityHashCode(this)+" got parameters:"+getURI());
  }
  
  @Override
  public TypeParameter<?>[] getParameters()
  { return parameters;
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Key<T> getPrimaryKey()
  { 
    link();
    return (Key) getCoreType().getPrimaryKey();
  }

  
  protected void combineContentTypeParams()
  {
    parameters
      =ArrayUtil.concat
        (TypeParameter[].class,contentType.getParameters(),parameters);
    if (parameters.length==0)
    { parameters=null;
    }
    else
    {
//      if (contentType.getParameters()!=null && contentType.getParameters().length>0)
//      { log.fine("Copied parameters from "+contentType.getURI()+" to "+getURI());
//      }
    }
  }  
  
  @Override
  public void link()
  { 
    if (linked)
    { return;
    }
    linked=true;
    
    contentType.link();
    combineContentTypeParams();
    try
    {
      Type<?> contentArchetype=contentType.getArchetype();
      if (contentArchetype!=null)
      { 
        archetype
          =contentType.getTypeResolver().resolve
            (URIPool.create(contentArchetype.getURI().toString()
                        .concat(getAggregateQualifier())
                       )
            );
      }
      
      Type<?> contentBaseType=contentType.getBaseType();
      if (contentBaseType!=null)
      { 
        baseType
          =contentType.getTypeResolver().resolve
            (URIPool.create(contentBaseType.getURI().toString()
                        .concat(getAggregateQualifier())
                       )
            );
      }
    }
    catch (DataException x)
    { throw new RuntimeDataException("Error linking "+getURI(),x);
    }
    
    try
    {
      this.scheme=new SchemeImpl();
      this.scheme.setType(this);
      if (archetype!=null)
      { 
        archetype.link();
        this.scheme.setArchetypeScheme(archetype.getScheme());
      }
      this.scheme.resolve();
    }
    catch (DataException x)
    { throw new RuntimeException("Error linking "+getURI(),x);
    }
  }
  

  @Override
  public boolean isLinked()
  { return linked;
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
  public T fromString(String val)
    throws DataException
  { return null;
  }
  
  @Override
  public String toString(T val)
  { return null;
  }  

  @Override
  public String toString()
  { return super.toString()+":"+uri.toString();
  }  
  
  protected abstract String getAggregateQualifier();
  

  @Override
  public boolean isAbstract()
  {
    // TODO Auto-generated method stub
    return contentType.isAbstract();
  }


  @Override
  public boolean isExtendable()
  {
    // TODO Auto-generated method stub
    return contentType.isExtendable();
  }
  
  @Override
  public Comparator<T> getComparator()
  { return null;
  }
}