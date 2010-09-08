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
package spiralcraft.vfs.filters;


import spiralcraft.vfs.Resource;
import spiralcraft.vfs.ResourceFilter;

import java.util.List;
import java.util.LinkedList;


/**
 * A ResourceFilter which accepts all resources provided and stores
 *   them in a LinkedList.
 */
public class ListFilter
  implements ResourceFilter
{
  private final LinkedList<Resource> _list
    =new LinkedList<Resource>();
    
  @Override
  public boolean accept(Resource resource)
  { 
    _list.add(resource);
    return true;
  }

  public List<Resource> getList()
  { return _list;
  }
  
  public void clear()
  { _list.clear();
  }
}

