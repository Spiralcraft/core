package spiralcraft.ui;

import spiralcraft.service.Service;
import spiralcraft.service.ServiceAdapter;
import spiralcraft.service.ServiceException;
import spiralcraft.service.ServiceResolver;

/**
 * Generic UI service, typically subclassed for a soecific View, 
 *   such as Swing or WebUI. 
 */
public class UiService
  extends ServiceAdapter
{
  private Control _rootControl;

  public void setRootControl(Control val)
  { _rootControl=val;
  }

  public void init(ServiceResolver resolver)
    throws ServiceException
  { _rootControl.init();
  }

  public void destroy()
    throws ServiceException
  { _rootControl.destroy();
  }

}
