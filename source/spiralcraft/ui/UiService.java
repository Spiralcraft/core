package spiralcraft.ui;

import spiralcraft.service.Service;
import spiralcraft.service.ServiceException;

/**
 * Generic UI service
 */
public class UiService
  implements Service
{
  private Controller[] _rootControllers;

  public void setRootControllers(Controller[] val)
  { _rootControllers=val;
  }

  public void init()
    throws ServiceException
  {
    if (_rootControllers!=null)
    { 
      for (int i=0;i<_rootControllers.length;i++)
      { _rootControllers[i].init();
      } 
    }
  }

  public void destroy()
    throws ServiceException
  {
    if (_rootControllers!=null)
    { 
      for (int i=0;i<_rootControllers.length;i++)
      { _rootControllers[i].destroy();
      } 
    }
  }
}
