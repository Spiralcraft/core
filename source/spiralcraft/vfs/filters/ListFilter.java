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
 * <p>A ResourceFilter which stores all resources accepted by the chained filter
 *   in a LinkedList
 * </p>
 */
public class ListFilter
  implements ResourceFilter
{
  private final LinkedList<Resource> _list
    =new LinkedList<Resource>();

  private final ResourceFilter next;
  private final ResourceFilter resultFilter;
  
  public ListFilter()
  { 
    next=null;
    resultFilter=null;
  }
  
  public ListFilter(ResourceFilter next)
  { 
    this.next=next;
    this.resultFilter=null;
  }

  public ListFilter(ResourceFilter next,ResourceFilter resultFilter)
  { 
    this.next=next;
    this.resultFilter=resultFilter;
  }

  @Override
  public boolean accept(Resource resource)
  { 
    if (next==null || next.accept(resource))
    {
      if (resultFilter==null || resultFilter.accept(resource))
      { _list.add(resource);
      }
      return true;
    }
    return false;
  }

  public ResourceFilter getNext()
  { return next;
  }
  
  public List<Resource> getList()
  { return _list;
  }
  
  public void clear()
  { _list.clear();
  }
}

