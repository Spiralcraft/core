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


import spiralcraft.data.Key;
import spiralcraft.data.Scheme;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;

import spiralcraft.data.query.Query;
import spiralcraft.data.query.Selection;
import spiralcraft.data.query.Scan;

import spiralcraft.lang.Expression;
import spiralcraft.lang.ParseException;
import spiralcraft.lang.Reflector;

import spiralcraft.util.StringUtil;

public class KeyImpl
  extends ProjectionImpl
  implements Key
{
  private Scheme scheme;
  private String name;
  private int index;
  private boolean primary;
  private boolean unique;
  private Type<?> foreignType;
  private KeyImpl importedKey;
  private String[] fieldNames;
  private Query query;

  public String getName()
  { return name;
  }
  
  public void setName(String name)
  { this.name=name;
  }
  
  
  /**
   * Construct an unresolved KeyImpl which will be configured and resolved
   *   manually by the containing object.
   */
  public KeyImpl()
  { }
  
  public KeyImpl(FieldSet fieldSet,String fieldList)
  {
  
  }
  
  /**
   * @return The Scheme to which this Key belongs.
   */
  public Scheme getScheme()
  { return scheme;
  }
  
  public void setScheme(Scheme scheme)
  { 
    assertUnresolved();
    this.scheme=scheme;
    this.masterFieldSet=scheme;
  }
  
  
  /**
   * @return The index of this Key within the set of Keys belonging to
   *   Scheme
   */
  public int getIndex()
  { return index;
  }
  
  public void setIndex(int val)
  { index=val;
  }
  
  /**
   * @return Whether this Key is the primary key for its Scheme
   */
  public boolean isPrimary()
  { return primary;
  }
  
  public void setPrimary(boolean primary)
  { this.primary=primary;
  }
  
  /**
   * @return Whether this Key uniquely identifies a single Tuple
   */
  public boolean isUnique()
  { return unique;
  }
  
  public void setUnique(boolean unique)
  { this.unique=unique;
  }
  
  /**
   * @return A Type which provides data for this Key's Fields.
   */
  public Type<?> getForeignType()
  { return foreignType;
  }
  
  /**
   * @return A Key from the foreign Type that originates the data values
   *   for this Key's Fields. 
   */
  public KeyImpl getImportedKey()
  { return importedKey;
  }
  
  public void setImportedKey(KeyImpl key)
  { this.importedKey=key;
  }

  public Query getQuery()
  { return query;
  }
  
  public void setFieldList(String fieldList)
  {
    assertUnresolved();
    this.fieldNames
      =StringUtil.tokenize(fieldList,",");
  }
  
  
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }

    for (String fieldName: fieldNames)
    { 
      Field masterField=masterFieldSet.getFieldByName(fieldName);
      if (masterField==null)
      { throw new FieldNotFoundException(scheme.getType(),fieldName);
      }
      addMasterField(masterField.getName(),masterField);
    }
    if (importedKey!=null)
    { 
      if (foreignType==null)
      { foreignType=scheme.getType();
      }
      
      if (foreignType==null)
      { 
        throw new DataException
          ("Key with imported Key must also have a foreign Type");
      }
      
      Scheme importedScheme=foreignType.getScheme();
      if (importedScheme==null)
      { 
        throw new DataException
          ("Foreign Type must have a Scheme");
      }
      importedKey.setScheme(importedScheme);
      importedKey.resolve();
    }
    
    StringBuilder expression=new StringBuilder();
    for (String fieldName: fieldNames)
    { 
      if (expression.length()>0)
      { expression.append(" && ");
      }
      expression.append(fieldName).append("==").append("..").append(fieldName);
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

    if (getForeignType()!=null && name!=null)
    {
      // Expose a Field to provide direct access to the join
      
      if (getFieldByName(name)!=null)
      { 
        throw new DataException
          ("Key "+getScheme().getType().getURI()+"."+name+": Field '"
            +name+"' already exists"
          );
      }
      else
      { ((SchemeImpl) getScheme()).addField(new KeyField(this));
      }
    }
    
    super.resolve();
    
    
  }
 
  public Reflector<Tuple> getReflector()
  { return reflector;
  }
}
