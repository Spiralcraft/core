package spiralcraft.stream.filters;


import spiralcraft.stream.ResourceFilter;
import spiralcraft.stream.Resource;

import java.util.List;
import java.util.LinkedList;


/**
 * A ResourceFilter which accepts all resources provided and stores
 *   them in a LinkedList.
 */
public class ListFilter
  implements ResourceFilter
{
  private final LinkedList _list=new LinkedList();
    
  public boolean accept(Resource resource)
  { 
    _list.add(resource);
    return true;
  }

  public List getList()
  { return _list;
  }
  
  public void clear()
  { _list.clear();
  }
}

