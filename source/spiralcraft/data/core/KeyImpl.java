//
// Copyright (c) 1998,2010 Michael Toth
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


import spiralcraft.data.DataComposite;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Key;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;

import spiralcraft.data.query.EquiJoin;
import spiralcraft.data.query.Query;
import spiralcraft.data.query.Selection;
import spiralcraft.data.query.Scan;
import spiralcraft.data.spi.DataKeyFunction;
import spiralcraft.data.spi.IdentifierFunction;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.Reflector;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.KeyFunction;
import spiralcraft.util.string.StringConverter;
import spiralcraft.util.string.StringUtil;

/**
 * Implements a Key
 * 
 * @author mike
 *
 * @param <T>
 */
public class KeyImpl<T extends DataComposite>
  extends ProjectionImpl<T>
  implements Key<T>
{
  private Scheme scheme;
  private String name;
  private String description;
  
  private boolean primary;
  private boolean unique;
  private Type<?> foreignType;
  private KeyImpl<?> importedKey;
  private String[] fieldNames;
  private Query query;
  private Query foreignQuery;
  private String title;
  private KeyFunction<KeyTuple,T> identifierFunction;
  private StringConverter<?>[] stringConverters;
  private boolean resolved;
  private RelativeField<T> relativeField;
  private boolean child;
  
  /**
   * Construct an unresolved KeyImpl which will be configured and resolved
   *   manually by the containing object.
   */
  public KeyImpl()
  { }

  
  public KeyImpl(RelativeField<T> relativeField)
  {
    this.relativeField=relativeField;
    this.name=relativeField.getName();
    if (relativeField.getType().isAggregate())
    { this.foreignType=relativeField.getType().getContentType();
    }
    else
    { this.foreignType=relativeField.getType();
    }
    if (relativeField.isUniqueValue())
    { this.unique=true;
    }
    if (relativeField.getReferencedFieldNames()!=null
        || relativeField.getReferencedKeyName()!=null
        || !relativeField.getType().isAggregate()
        )
    {
      importedKey=new KeyImpl<Tuple>();
      importedKey.fieldNames=relativeField.getReferencedFieldNames();
      importedKey.name=relativeField.getReferencedKeyName();
      importedKey.unique=!relativeField.getType().isAggregate();
    }
    this.fieldNames=relativeField.getFieldNames();
    this.child=relativeField.isChild();
    setScheme(relativeField.getScheme());
  }
  
  public KeyImpl(FieldSet fieldSet,String fieldList)
    throws DataException
  { super(fieldSet,StringUtil.tokenize(fieldList,","));
  }
  
  @Override
  public String getName()
  { return name;
  }
  
  public void setChild(boolean child)
  { this.child=child;
  }
  
  public boolean isChild()
  { return child;
  }
  
  public void setName(String name)
  { this.name=name;
  }

  @Override
  public String getDescription()
  { return description;
  }
  
  public void setDescription(String description)
  { this.description=description;
  }
  
  @Override
  public String getTitle()
  { return title;
  }
  
  public void setTitle(String title)
  { this.title=title;
  }
  

  
  /**
   * @return The Scheme to which this Key belongs.
   */
  @Override
  public Scheme getScheme()
  { return scheme;
  }
  
  void setScheme(Scheme scheme)
  { 
    assertUnresolved();
    this.scheme=scheme;
    if (scheme.getType()!=null)
    { 
      // Make sure we include base types.
      this.masterFieldSet=scheme.getType().getFieldSet();
    }
    else
    { this.masterFieldSet=scheme;
    }
  }
  
  public RelativeField<T> getRelativeField()
  { return relativeField;
  }
  
  /**
   * @return Whether this Key is the primary key for its Scheme
   */
  @Override
  public boolean isPrimary()
  { return primary;
  }
  
  public void setPrimary(boolean primary)
  { this.primary=primary;
  }
  
  /**
   * @return Whether this Key uniquely identifies a single Tuple
   */
  @Override
  public boolean isUnique()
  { return primary || unique;
  }
  
  public void setUnique(boolean unique)
  { this.unique=unique;
  }
  
  /**
   * @return A Type which maps data to this Key's Fields.
   */
  @Override
  public Type<?> getForeignType()
  { return foreignType;
  }
  
  /**
   * A Type which maps data to this Key's Fields.
   */
  public void setForeignType(Type<?> foreignType)
  { this.foreignType=foreignType;
  }

  /**
   * @return A Key based on the foreign Type that defines the set of related 
   *   fields and the cardinality of the relation.
   */
  @Override
  public Key<?> getImportedKey()
  { return importedKey;
  }
  
  public void setImportedKey(Key<?> key)
  { this.importedKey=(KeyImpl<?>) key;
  }

  @Override
  public Query getQuery()
  { return query;
  }
  
  public Query getForeignQuery()
    throws DataException
  { 
    resolve();
    return foreignQuery;
  }
  
  public void setFieldList(String fieldList)
  {
    assertUnresolved();
    this.fieldNames
      =StringUtil.tokenize(fieldList,",");
  }
  
  @Override
  public String[] getFieldNames()
  { return fieldNames;
  }
  
  public void setFieldNames(String[] fieldNames)
  { 
    assertUnresolved();
    this.fieldNames=fieldNames;
  }
  
  private KeyImpl<?> findReciprocalKey()
  {
    Key<?>[] keys=foreignType.getKeys();
    if (keys!=null)
    {
      for (Key<?> key : keys)
      { 
        // Check for reciprocal definition
        if (key.getForeignType()!=null
            && key.getForeignType().isAssignableFrom(scheme.getType())
            )
        { return (KeyImpl<?>) key;
        }
      }
    }
    return null;

  }
  
  @Override
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;

    if (foreignType!=null)
    { 
      foreignType.link();
    
      if (importedKey==null)
      {

        importedKey=findReciprocalKey();
        if (importedKey==null)
        {
        
          // Use default reference to a primary key
          importedKey=(KeyImpl<?>) foreignType.getPrimaryKey();
        }
        if (importedKey==null)
        {
          throw new DataException("In "+scheme.getType().getURI()+"#"+getName()
            +", no "
            +" suitable foreign Key found in foreign type "+foreignType.getURI()
            );
        }
      }
      else if (importedKey.getName()!=null)
      { 
        // Use a named reference to a foreign key
        if (importedKey.getFieldNames()!=null)
        { 
          throw new DataException
            ("imported key reference '"+importedKey.getName()+"'"
            +" in "+getType().getURI()
            +"' cannot also define a fieldList"
            );
        }
        
        Key<?>[] keys=foreignType.getKeys();
        for (Key<?> key : keys)
        { 
          if (importedKey.getName().equals(key.getName()))
          { importedKey=(KeyImpl<?>) key;
          }
        }
        
        
        
      }
      else if (importedKey.getFieldNames()==null)
      {
        KeyImpl<?> reciprocalKey=findReciprocalKey();
        if (reciprocalKey!=null)
        { importedKey.fieldNames=reciprocalKey.fieldNames;
        }
        else
        {
          if (foreignType.getPrimaryKey()==null)
          { 
            if (!foreignType.isLinked())
            { 
              log.fine(foreignType.getURI()+" is not linked");
              log.fine(foreignType.toString());
            
            }
            throw new DataException("Foreign type "+foreignType.getURI()
              +" for key "+getScheme().getType().getURI()+"#"+name+" does not "
              +" contain a suitable key to reference and has no primary key ");
          }
          String[] fieldNames=foreignType.getPrimaryKey().getFieldNames();
          
          if (importedKey.isUnique())
          { importedKey.fieldNames=fieldNames;
          }
          else
          { importedKey.fieldNames
              =ArrayUtil.truncate(fieldNames,this.fieldNames.length);
          }
        }
        
      }
    }
    
    if (fieldNames==null)
    { 
      throw new DataException
        ("Key in "+getType().getURI()+" has no fields");
    }
    
    stringConverters=new StringConverter<?>[fieldNames.length];
    int i=0;
    for (String fieldName: fieldNames)
    { 
      Field<?> masterField=masterFieldSet.getFieldByName(fieldName);
      if (masterField==null)
      { 
        
        throw new FieldNotFoundException
          ("Error binding Key "+name
           ,scheme!=null?scheme.getType():null
           ,fieldName
          );
      }
      if (masterField.getType()==null)
      { throw new DataException("Field "+masterField.getURI()+" has no type");
      }

      addMasterField(masterField.getName(),masterField);
      
      stringConverters[i]
        =masterField.getContentReflector().getStringConverter();
      i++;
    }
    
    
    if (title==null)
    { title=name;
    }
    if (title==null)
    {
      StringBuilder titleBuf=new StringBuilder();
      for (String fieldName: fieldNames)
      {
        Field<?> field=masterFieldSet.getFieldByName(fieldName);
        if (titleBuf.length()>0)
        { titleBuf.append(",");
        }
        titleBuf.append(field.getTitle());
      }
      title=titleBuf.toString();
    }

    
    projectionId=StringUtil.implode(',',',',fieldNames);
    

    
    
    if (importedKey!=null)
    { 
      
      if (foreignType==null)
      { 
        throw new DataException
          ("Key with imported Key must also have a foreign Type: "+toString());
      }

//      // Querying another type while resolving introduced a
//      // cycle.
//      
//      // Reconfirmed this issue 2008-07-29
//        //   foreign type is not resolved yet.
//      
//      Scheme importedScheme=foreignType.getScheme();
//      if (importedScheme==null)
//      { 
//        throw new DataException
//          ("Foreign Type must have a Scheme "+foreignType);
//      }
//
//      
//      
//      importedKey.setScheme(importedScheme);
//      importedKey.resolve();
      
      createForeignEquiJoin();

      
      
    }
    
    createLocalEquiJoin();

    super.resolve();
    
    try
    { identifierFunction=new IdentifierFunction<T>(this);
    }
    catch (BindException x)
    { throw new DataException("Error resolving identifier function",x);
    }
  }

  @Override
  public KeyFunction<KeyTuple,T> getIdentifierFunction()
  { return identifierFunction;
  }
  
  @Override
  protected DataKeyFunction<KeyTuple,T> resolveKeyFunction()
    throws BindException
  { 
    return new DataKeyFunction<KeyTuple,T>(this);
  }
  
  @Override
  public StringConverter<?>[] getStringConverters()
  { return stringConverters;
  }
  

  
  private void createLocalEquiJoin()
    throws DataException
   
  {
    if (scheme==null)
    { return;
    }
    EquiJoin ej=new EquiJoin();
    Expression<?>[] rhsExpressions=new Expression<?>[fieldNames.length];
    int i=0;
    for (String fieldName : fieldNames)
    {
      try
      { rhsExpressions[i++]=Expression.parse(fieldName);
      }
      catch (ParseException x)
      {
        throw new DataException
          ("Error parsing Key expression '"+fieldName+"':"+x,x);
      }

    }
//    ej.setDebug(true);
    ej.setExpressions
      (getTargetExpressions()
      ,rhsExpressions
      );
    ej.setSource(new Scan(scheme.getType()));
    ej.setDebug(debug);
    query=ej;

  }

  protected void createLocalSelection()
    throws DataException
  {
    if (scheme==null)
    { return;
    }
    StringBuilder expression=new StringBuilder();
    for (String fieldName: fieldNames)
    { 
      if (expression.length()>0)
      { expression.append(" && ");
      }
      expression.append("."+fieldName+"=="+fieldName);
    }
//    System.err.println("KeyImpl: expression= ["+expression+"]");
    
    try
    {
      query
        =new Selection
          (new Scan(scheme.getType())
          ,Expression.<Boolean>parse(expression.toString())
          );
    }
    catch (ParseException x)
    { 
      throw new DataException
        ("Error parsing Key expression '"+expression.toString()+"':"+x,x);
    }

  }
  
  private void createForeignEquiJoin()
    throws DataException
  {
    if (scheme==null)
    { return;
    }
    
    EquiJoin ej=new EquiJoin();
//    ej.setDebug(true);
    
    // Build lhs Expressions with dot-prefix
    String[] foreignFieldNames=importedKey.getFieldNames();
    Expression<?>[] lhsExpressions=new Expression<?>[foreignFieldNames.length];
    int i=0;
    for (String fieldName : foreignFieldNames)
    { 
      try
      { lhsExpressions[i++]=Expression.parse("."+fieldName);
      }
      catch (ParseException x)
      {
        throw new DataException
          ("Error parsing Key expression '"+fieldName+"':"+x,x);
      }
    }
    
    // Build rhs Expressions without dot-prefix
    String[] localFieldNames=getFieldNames();
    Expression<?>[] rhsExpressions=new Expression<?>[localFieldNames.length];
    i=0;
    for (String fieldName : localFieldNames)
    { 
      try
      { rhsExpressions[i++]=Expression.parse(fieldName);
      }
      catch (ParseException x)
      {
        throw new DataException
          ("Error parsing Key expression '"+fieldName+"':"+x,x);
      }
    }

    ej.setExpressions
      (lhsExpressions
      ,rhsExpressions
      );
    if (ej.getLHSExpressions().size()!=ej.getRHSExpressions().size())
    { 
      throw new DataException
        ("Key '"+name+"' of Type "+getScheme().getType()+" must have the"
          +" same number of fields on both the imported and the local side"
        );
        	
    }
    foreignQuery=ej;
    ej.setSource(new Scan(foreignType));
    
  }
  
  protected void createForeignSelection()
    throws DataException
  {
    if (scheme==null)
    { return;
    }
    
    // XXX May be obsolete
    StringBuilder expression=new StringBuilder();
    String[] foreignFieldNames=importedKey.getFieldNames();
    for (int i=0;i<fieldNames.length;i++)
    {
      if (expression.length()>0)
      { expression.append(" && ");
      }
      expression.append("."+foreignFieldNames[i])
        .append("==").append(fieldNames[i]);
    }
//      System.err.println("KeyImpl: expression= ["+expression+"]");
      
    try
    {
      foreignQuery
        =new Selection
          (new Scan(getForeignType())
          ,Expression.<Boolean>parse(expression.toString())
          );
    }
    catch (ParseException x)
    { 
      throw new DataException
        ("Error parsing Key expression '"+expression.toString()+"':"+x,x);
    }
  }
  
  
  public Reflector<Tuple> getReflector()
  { return reflector;
  }
  
  /**
   * Return a copy of this key definition specialized for a subtype
   * 
   * @param t
   * @return
   */
  public KeyImpl<Tuple> specialize(Scheme scheme)
  {
    KeyImpl<Tuple> specialized=new KeyImpl<Tuple>();
    specialized.setScheme(scheme);
    specialized.setFieldNames(fieldNames);
    specialized.setPrimary(primary);
    specialized.setUnique(unique);
    specialized.setTitle(title);
    specialized.setName(name);
    return specialized;
    
  }
  
  public String toString()
  {
    StringBuilder builder=new StringBuilder();
    if (this.scheme!=null)
    { builder.append(this.scheme.getType().getURI()); 
    }
    builder.append("#");
    builder.append("[")
           .append(ArrayUtil.format(getFieldNames(),",",""))
           .append("]");
    if (primary)
    { builder.append("-primary");
    }
    return builder.toString();
  }
}
