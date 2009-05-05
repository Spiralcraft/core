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
 * A generic exception associated with the spiralcraft.data package
 *   functionality
 */
public class DataException
  extends Exception
{
  private static final long serialVersionUID=1;
  
  public DataException()
  { }
  
  public DataException(String message)
  { super(message);
  }
  
  public DataException(String message,Throwable cause)
  { super(message,cause);
  }
  
  public Throwable getRootCause()
  {
    Throwable cause=this;
    while (cause.getCause()!=null)
    { cause=cause.getCause();
    }
    return cause!=this?cause:null;
  }
  
  @Override
  public String toString()
  { 
    Throwable rootCause=getRootCause();
    if (rootCause!=null)
    { return super.toString()+" (cause: "+getRootCause()+")";
    }
    else
    { return super.toString();
    }
      
  }
}
