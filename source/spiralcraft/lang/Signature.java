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

import java.util.Arrays;

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
  private final int hashCode;
  
  
  public Signature(String name,Reflector<?> type)
  { 
    this.name=name;
    this.parameters=null;
    this.type=type;
    hashCode=calcHash();
  }
  
  public Signature(String name,Reflector<?> type,Reflector<?> ... parameters)
  { 
    this.name=name;
    this.parameters=parameters;
    this.type=type;
    hashCode=calcHash();
  }
  
  private int calcHash()
  {
    int hash=17;
    hash= 37*hash + name.hashCode();
    if (type!=null)
    { hash= 37*hash + type.hashCode();
    }
    if (parameters!=null)
    { hash=37*hash + Arrays.hashCode(parameters);
    }
    return hash;
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
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public boolean equals(Object o)
  { 
    if (Signature.class!=o.getClass())
    { return false;
    }
    if (hashCode!=o.hashCode())
    { return false;
    }
    Signature sig=(Signature) o;
    return name.equals(sig.name) 
            && (type==null) == (sig.type==null)
            && (type==null || type.equals(sig.type))
            && (parameters==null) == (sig.parameters==null)
            && (parameters==null || Arrays.equals(parameters,sig.parameters))
            ;
  }
  
  public boolean hides(Signature base)
  {
    if (!name.equals(base.getName()))
    { return false;
    }
    Reflector<?>[] baseParams=base.getParameters();
    if (parameters==null)
    { return baseParams==null;
    }
    else if (baseParams==null || baseParams.length!=parameters.length)
    { return false;
    }
    else
    {
      for (int i=0;i<parameters.length;i++)
      { 
        if (!baseParams[i].isAssignableFrom(parameters[i]))
        { return false;
        }
      }
    }
    return true;
  }
  
  @Override
  public String toString()
  {
    StringBuffer ret=new StringBuffer();
    ret.append(name);
    if (type!=null)
    {
      ret.append(" : ");
      ret.append(type.getTypeURI());
    }
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
