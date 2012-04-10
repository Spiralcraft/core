//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.common.declare;

import java.net.URI;

/**
 * Associates a Declarable object with information from the declaration points
 *   involved in its construction.
 * 
 * @author mike
 *
 */
public class DeclarationInfo
{

  private final DeclarationInfo base;
  private final URI declaredType;
  private final URI location;
  
  public DeclarationInfo(DeclarationInfo base,URI declaredType,URI location)
  { 
    this.base=base;
    this.declaredType=declaredType;
    this.location=location;
  }
  
  public URI getLocation()
  {
    if (location!=null)
    { return location;
    }
    else if (base!=null)
    { return base.getLocation();
    }
    else
    { return null;
    }
  }

  public URI getDeclaredType()
  {
    if (declaredType!=null)
    { return declaredType;
    }
    else if (base!=null)
    { return base.getDeclaredType();
    }
    else
    { return null;
    }
  }
  
  /**
   * <p>Determine whether this Declarable instance is based on a declaration
   *   type identified by the specified URI by examining all the declarations
   *   associated with this Declarable
   * </p>
   * 
   * @param typeURI
   * @return
   */
  public boolean instanceOf(URI typeURI)
  { 
    if (declaredType!=null && declaredType.equals(typeURI))
    { return true;
    }
    else if (base!=null)
    { return base.instanceOf(typeURI);
    }
    else
    { return false;
    }
        
  }
  
  @Override
  public String toString()
  {
    URI location=getLocation();
    URI declaredType=getDeclaredType();
    StringBuffer ret=new StringBuffer();
    
    if (declaredType!=null)
    { 
      ret.append(declaredType.toString());
      if (location!=null)
      { ret.append(" @ ").append(location.toString());
      }
    }
    else
    {
      if (location!=null)
      { ret.append(location.toString());
      }
      else
      { ret.append("???");
      }
    }
    
    return ret.toString();
  }
}
