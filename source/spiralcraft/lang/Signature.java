//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.lang;

/**
 * Provides metadata about a bindable member exported by a Reflector.
 * 
 * @author mike
 */
public class Signature
{
  private final String name;
  private final Reflector<?>[] parameters;
  private final Reflector<?> type;
  
  public Signature(String name,Reflector<?> type)
  { 
    this.name=name;
    this.parameters=null;
    this.type=type;
  }
  
  public Signature(String name,Reflector<?> type,Reflector<?> ... parameters)
  { 
    this.name=name;
    this.parameters=parameters;
    this.type=type;
  }
  
  public String getName()
  { return name;
  }
  
  public Reflector<?>[] getParameters()
  { return parameters;
  }
  
  public Reflector<?> getType()
  { return type;
  }
  
  @Override
  public String toString()
  {
    StringBuffer ret=new StringBuffer();
    ret.append(name);
    ret.append(" : ");
    ret.append(type.getTypeURI());
    if (parameters!=null)
    { 
      ret.append(" (");
      int i=0;
      for (Reflector<?> ptype:parameters)
      { 
        if (i++>0)
        { ret.append(",");
        }
        ret.append(ptype);
      }
      ret.append(")");
    }
    return ret.toString();
  }
}
