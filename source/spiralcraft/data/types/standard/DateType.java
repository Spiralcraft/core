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
package spiralcraft.data.types.standard;

import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.net.URI;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateType
  extends PrimitiveTypeImpl<Date>
{
  private SimpleDateFormat format;
  
  private String dateFormat="yyyy-MM-dd HH:mm:ss.SSS Z";
  
  public DateType(TypeResolver resolver,URI uri)
  { super(resolver,uri,Date.class);
  }
  
  @Override
  public synchronized  Date fromString(String str)
    throws DataException
  { 
    if (!linked)
    { throw new DataException("Type "+getURI()+" not linked");
    }
    
    if (str!=null)
    { 
      try
      { return format.parse(str);
      }
      catch (ParseException x)
      { 
        throw new DataException
          ("Date formatting error. Format is "+dateFormat
          ,x
          );
      }
    }
    else
    { return null;
    }
  }
  
  @Override
  public synchronized String toString(Date date)
  { 
    if (date!=null)
    { return format.format(date);
    }
    else
    { return null;
    }
  }

  @Override
  protected void linkPrimitive()
  { format=new SimpleDateFormat(dateFormat);
  }
  
  public String getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(
    String dateFormat)
  {
    
    if (linked)
    { throw new IllegalStateException("Type "+getURI()+" is already linked");
    }
    this.dateFormat = dateFormat;
  }

  
}