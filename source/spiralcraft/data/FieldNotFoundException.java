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
package spiralcraft.data;


/**
 * Throws when a Type is not found by the TypeResolver
 */
public class FieldNotFoundException
  extends DataException
{
  private static final long serialVersionUID=1;
  
  private static final String makeFieldList(FieldSet fieldSet)
  {
    StringBuilder fieldList=new StringBuilder();
    boolean first=true;
    for (Field<?> field:fieldSet.fieldIterable())
    {
      if (first)
      { first=false;
      }
      else
      { fieldList.append(",");
      }
      fieldList.append(field.getName());
    }
    return fieldList.toString();
  }
  
  private static final String makeFieldList(Type<?> type)
  {
    StringBuilder fieldList=new StringBuilder();
    boolean first=true;
    while (type!=null)
    {
      if (type.getScheme()!=null)
      {
        for (Field<?> field:type.getScheme().fieldIterable())
        {
          if (first)
          { first=false;
          }
          else
          { fieldList.append(",");
          }
          fieldList.append(field.getName());
        }
      }
      type=type.getBaseType();
    }
    return fieldList.toString();
  }

  public FieldNotFoundException(Type<?> type,String fieldName)
  { this("",type,fieldName); 
  }

  public FieldNotFoundException(FieldSet fieldSet,String fieldName)
  { 
    super
      ("Field '"+fieldName+"' not found in "
      +(( (fieldSet instanceof Scheme) && ((Scheme) fieldSet).getType()!=null )
       ?" type "+((Scheme) fieldSet).getType().getURI()+":"
       :" field set "
       )
      +":["+makeFieldList(fieldSet)+"]"
      );
  }

  public FieldNotFoundException(
    String message,
    Type<?> type,
    String fieldName)
  {
    super
      (message+": Field '"+fieldName+"' not found in type "+type.getURI()
      +": fields=["+makeFieldList(type)+"]"
      );
    
    // TODO Auto-generated constructor stub
  }
}
