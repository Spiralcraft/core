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
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.FieldNotFoundException;

import spiralcraft.util.StringUtil;

public class KeyImpl
  extends ProjectionImpl
  implements Key
{
  private Scheme scheme;
  private int index;
  private boolean primary;
  private boolean unique;
  private Type foreignType;
  private KeyImpl importedKey;
  private String[] fieldNames;


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
  
  /**
   * @return Whether this Key uniquely identifies a single Tuple
   */
  public boolean isUnique()
  { return unique;
  }
  
  /**
   * @return A Type which provides data for this Key's Fields.
   */
  public Type getForeignType()
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
    super.resolve();

  }
  
}
