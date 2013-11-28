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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import spiralcraft.data.Key;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.lang.Expression;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.ArrayKey;
import spiralcraft.util.ArrayUtil;

/**
 * Core implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  private final ClassLog log=ClassLog.getInstance(SchemeImpl.class);
  private Level logLevel
    =ClassLog.getInitialDebugLevel(SchemeImpl.class,Level.INFO);
  
  protected Type<?> type;
  protected Key<Tuple> primaryKey;
  private boolean resolved;
  private Scheme archetypeScheme;

  protected final ArrayList<FieldImpl<?>> localFields
    =new ArrayList<FieldImpl<?>>();
  protected final HashMap<String,FieldImpl<?>> localFieldMap
    =new HashMap<String,FieldImpl<?>>();

  protected final ArrayList<Field<?>> fields
    =new ArrayList<Field<?>>();
  protected final HashMap<String,Field<?>> fieldMap
    =new HashMap<String,Field<?>>();

  protected final ArrayList<KeyImpl<Tuple>> keys
    =new ArrayList<KeyImpl<Tuple>>();
//  protected final HashMap<String,KeyImpl> keyMap
//    =new HashMap<String,KeyImpl>();
  
  
  protected final HashMap<ArrayKey,ProjectionImpl<Tuple>> projectionMap
    =new HashMap<ArrayKey,ProjectionImpl<Tuple>>();
    
  @Override
  public Type<?> getType()
  { return type;
  }
  
  public void setArchetypeScheme(Scheme scheme)
  { 
    assertUnresolved();
    archetypeScheme=scheme;
  }
  
  @Override
  public boolean hasArchetype(Scheme scheme)
  {
    if (this==scheme)
    { return true;
    }
    else if (archetypeScheme!=null)
    { return archetypeScheme.hasArchetype(scheme);
    }
    else
    { return false;
    }
  }
  
  public void setType(Type<?> type)
  { 
    assertUnresolved();
    this.type=type;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Map cast
  public <X> Field<X> getFieldByIndex(int index)
  { return (Field<X>) fields.get(index);
  }
  
  @Override
  public String[] getFieldNames()
  { 
    String[] ret=new String[fields.size()];
    int i=0;
    for (Field<?> field:fields)
    { ret[i++]=field.getName();
    }
    return ret;
  }
  
  @Override
  @SuppressWarnings("unchecked") // Map cast
  public <X> Field<X> getFieldByName(String name)
  { return (Field<X>) fieldMap.get(name);
  }
  
  public Field<?> getLocalFieldByName(String name)
  { return localFieldMap.get(name);
  }
  
  /**
   *@return An Iterable that iterates through all fields of this Type and its
   *  archetype.
   */
  @Override
  public Iterable<? extends Field<?>> fieldIterable()
  { return fields;
  }

  /**
   * Get the list of local fields
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<FieldImpl> getFields()
  { return (List<FieldImpl>) localFields.clone();
  }
  
  /**
   * Set the list of local fields
   */
  public void setFields(List<FieldImpl<?>> fields)
  { 
    assertUnresolved();
    clearFields();
    for (FieldImpl<?> field : fields)
    { 
      // System.out.println("Field "+field.toString());
      addField(field);
    }
  }

  /**
   * Add a local Field
   */
  public void addField(FieldImpl<?> field)
  { 
    assertUnresolved();
    if (localFieldMap.get(field.getName())!=null)
    { 
      throw new IllegalArgumentException
        ("Field name '"+field.getName()+"' is not unique"
        );
    }
    localFields.add(field);
    localFieldMap.put(field.getName(),field);
  }
  
  /**
   * Clear the set of local fields
   */
  private void clearFields()
  { 
    assertUnresolved();
    localFields.clear();
    localFieldMap.clear();
  }

  @Override
  public int getFieldCount()
  { return fields.size();
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
      fieldList.append("\r\n       #");
      if (!first)
      { fieldList.append(",");
      }
      else
      { first=false;
      }
      fieldList.append
        (field.getClass().getSimpleName()+":"
         +field.getName()+"("+field.getType().getURI()+")"
        );
    }
    fieldList.append("]");
    return fieldList.toString();
  }
  
  @Override
  public String toString()
  {
    if (getType()!=null)
    { return type.toString();
    }
    else
    {
      return super.toString()
        +"(untyped):"
        +contentsToString()
        ;
    }
  }
  
  public void resolve()
    throws DataException
  {
    if (resolved)
    { return;
    }
    resolved=true;

//    System.err.println("SchemeImpl.resolve() for "+getType());
    int fieldIndex=0;
    if (archetypeScheme!=null)
    { 
      fieldIndex=archetypeScheme.getFieldCount();
      for (Field<?> field: archetypeScheme.fieldIterable())
      { 
        fields.add(field);
        fieldMap.put(field.getName(),field);
      }
    }
    
    
    for (FieldImpl<?> field:localFields)
    {
      if (field.getName()==null)
      { throw new DataException("Field "+field+" name is null");
      }
      
      Field<?> archetypeField=null;
      field.setScheme(this);
      // field.resolveType();
      
      if (archetypeScheme!=null)
      { 
        archetypeField=
          archetypeScheme.getFieldByName(field.getName());
      }
      
      if (archetypeField!=null)
      { 
        if (field.isFunctionalEquivalent(archetypeField))
        {
          // Field is redundant. Don't replace archetype field, which is
          //   already mapped
          continue;
        }
        
        // Field with same name will have same index as archetype field,
        //   extending field functionality in compatible way
        try
        { 
          field.setArchetypeField(archetypeField);
          fields.set(field.getIndex(),field);
//        System.err.println
//        ("Field "+field.getUri()
//        +" overriding field "+archetypeField.getUri()
//        );
        }
        catch (TypeMismatchException x)
        { System.err.println("SchemeImpl: Ignoring archetype field override: "+x);
        }
      }
      else
      { 
        field.setIndex(fieldIndex++);
        fields.add(field);
      }
      fieldMap.put(field.getName(),field);
      
    }
    
    
    preprocessKeyDeclarations();
    
    resolveLocalKeys();
    
    for (FieldImpl<?> field: localFields)
    { field.resolve();
    }
    
    resolveRelativeKeys();

    
    // Transient fields 
    for (FieldImpl<?> field: localFields)
    { field.resolve();
    }
    
    for (ProjectionImpl<?> projection: projectionMap.values())
    { projection.resolve();
    }
  }

  
  
  /**
   * <p>Process important components of the Key declarations (e.g.
   *   finding the primary key) before fields are resolved
   * </p>
   *   
   * @throws DataException
   */
  void preprocessKeyDeclarations()
    throws DataException
  {
    
    ArrayList<KeyImpl<Tuple>> keys=new ArrayList<KeyImpl<Tuple>>();
    
    // Pull in key definitions from the archetype, if any
    if (archetypeScheme!=null && !getType().isAggregate())
    {
      for (Key<Tuple> key: archetypeScheme.keyIterable())
      { 
        if (key.getForeignType()==null)
        { keys.add(((KeyImpl<Tuple>) key).specialize(this));
        }
        
      }
    }
    
    keys.addAll(this.keys);
    
    for (KeyImpl<Tuple> key:keys)
    {
      if (key.isPrimary())
      { 
        if (getInheritedPrimaryKey()!=null)
        {
          throw new DataException
            ("Duplicate primary key: "+key+" in scheme "+toString());
        }
        else
        { this.primaryKey=key;
        }
      }
      key.setScheme(this);
    
    }
    this.keys.clear();
    this.keys.addAll(keys);
    
  }
  
  void resolveLocalKeys()
    throws DataException
  {
    for (KeyImpl<Tuple> key:keys)
    {
      if (key.getForeignType()==null)
      { key.resolve();
      }
    }
  }

  void resolveRelativeKeys()
    throws DataException
  {
    
    for (KeyImpl<Tuple> key:keys)
    {
      if (key.getForeignType()!=null)
      {
        key.resolve();
    
        if (key.getRelativeField()==null
            && key.getName()!=null 
           )
        {
          // Expose a Field to provide direct access to the join
      
          if (getFieldByName(key.getName())!=null)
          { 
            throw new DataException
              ("Key "+getType().getURI()+"."+key.getName()+": Field '"
                +key.getName()+"' already exists"
              );
          }
          else
          { addNewKeyField(key);
          }
        }
    
      }
    }
  }

  
  void addKey(KeyImpl<Tuple> key)
  { this.keys.add(key);
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We only know the actual type at runtime
  private void addNewKeyField(KeyImpl key)
    throws DataException
  { 
    addFieldPostResolve
      (new RelativeField(key));
  }
  
  private <X> void addFieldPostResolve(FieldImpl<X> field)
    throws DataException
  { 
    field.setIndex(fields.size());
    field.setScheme(this);
    fields.add(field);
    field.resolve();
    fieldMap.put(field.getName(), field);
  }
  
  public void assertUnresolved()
  {
    if (resolved)
    { throw new IllegalStateException("Already resolved "+getType());
    }
  }

  @Override
  public Key<Tuple> getKeyByIndex(int index)
  { return keys.get(index);
  }

  @Override
  public int getKeyCount()
  { return keys.size();
  }

  @Override
  public Key<Tuple> getPrimaryKey()
  { return primaryKey;
  }
  
  /**
   * Return a primary key defined in the base-type (identity) 
   *   inheritance hierarchy
   *   
   * @return
   */
  @SuppressWarnings("unchecked")
  private Key<Tuple> getInheritedPrimaryKey()
  { 
    if (primaryKey==null 
        && type!=null
        && type.getBaseType()!=null
        )
    { return (Key<Tuple>) type.getBaseType().getPrimaryKey();
    }
    return primaryKey;
  }
  
  public void setKeys(KeyImpl<Tuple>[] keyArray)
  { 
    for (KeyImpl<Tuple> key: keyArray)
    { keys.add(key);
    }
  }
  
  @Override
  public Iterable<? extends Key<Tuple>> keyIterable()
  { return keys;
  }
  
  @Override
  public Key<?> findKey(String[] fieldNames)
  { 
    for (Key<?> key:keys)
    {
      if (ArrayUtil.arrayEquals(key.getFieldNames(),fieldNames))
      { return key;
      }
    }
    return null;
  }

  @Override
  public Key<?> findKey(Expression<?>[] signature)
  {
    for (KeyImpl<Tuple> key : keys)
    { 
      if (Arrays.deepEquals(key.getTargetExpressions(),signature))
      { return key;
      }
    }
    return null;
  }
  
  private ProjectionImpl<Tuple> createProjection(Expression<?>[] signature)
    throws DataException
  {
    ArrayKey sigKey=ArrayUtil.asKey(signature);
    synchronized (projectionMap)
    {
    
      
      ProjectionImpl<Tuple> projection=projectionMap.get(sigKey);
      if (projection!=null)
      { return projection;
      }

      
      for (Key<?> key : getType().getKeys())
      { 
        @SuppressWarnings("unchecked")
        KeyImpl<Tuple> keyImpl=(KeyImpl<Tuple>) key;
        if (Arrays.deepEquals(key.getTargetExpressions(),signature))
        { 
          projectionMap.put(ArrayUtil.asKey(key.getTargetExpressions()),keyImpl);
          return keyImpl;
        }
      }
      
      
      if (logLevel.isDebug())
      {
        log.debug
          ("Creating new projection for: "+getType().getURI()+"#"
            +ArrayUtil.format(signature,",","")
          );
      }
      
      projection=new ProjectionImpl<Tuple>(this,signature);
      projectionMap.put(sigKey,projection);
      if (resolved)
      { projection.resolve();
      }
      return projection;
      
    }
    
  }
  
  @Override
  public Projection<Tuple> getProjection(Expression<?>[] signature)
    throws DataException
  {
    Projection<Tuple> projection=projectionMap.get(ArrayUtil.asKey(signature));
    if (projection!=null)
    { return projection;
    }
    
    
    
    return createProjection(signature);
  }
}