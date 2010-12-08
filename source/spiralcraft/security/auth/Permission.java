//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.security.auth;

import java.net.URI;

import spiralcraft.common.Immutable;
import spiralcraft.data.Tuple;

/**
 * <p>A security query that indicates whether some action can be taken against
 *   some resource. 
 * </p>
 * 
 * <p>The action and/or resource may be specified in a subtype or may simply
 *   be implied by the subtype
 * </p>
 *  
 * @author mike
 *
 */

@Immutable
public class Permission
{
  private final URI id;
  
  public Permission(Tuple data)
  { this.id=data.getType().getURI();
  }
  
  public URI getId()
  { return id;
  }
  
  @Override
  public int hashCode()
  { return getClass().hashCode()+(37*id.hashCode());
  }
  
  @Override
  public boolean equals(Object o)
  { return o!=null && o.getClass()==getClass() && ((Permission) o).id==id; 
  }
  
  public boolean implies(Permission p)
  { return equals(p);
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+id;
  }
}
