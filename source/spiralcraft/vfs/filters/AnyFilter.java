//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.vfs.filters;

import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;

/**
 * <p>Accepts a Resource if any of the contained filters accepts it
 * </p>
 */
public class AnyFilter
  implements ResourceFilter
{
  private final ResourceFilter[] filters;

  public AnyFilter(ResourceFilter ... filters)
  { this.filters=filters;
  }
  
  
  @Override
  public boolean accept(Resource resource)
  { 
    for (ResourceFilter filter:filters)
    {
      if (filter.accept(resource))
      { return true;
      }
    }
    return false;
  }



}

