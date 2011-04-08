package spiralcraft.service;

import java.net.URI;

import spiralcraft.app.Component;
import spiralcraft.app.spi.AbstractComponent;
import spiralcraft.app.spi.StandardContainer;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Context;
import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.context.ContextResourceMap;

/**
 * Provides 
 * 
 * @author mike
 *
 */
public class ResourceContext
  extends AbstractComponent
  implements Context
{


  
  ContextResourceMap resourceMap
    =new ContextResourceMap();
  
  Context[] threadContextuals
    =new Context[0];
  
  @Override
  public Focus<?> bind(Focus<?> focus) 
    throws BindException
  {    
    resourceMap.setParent(ContextResourceMap.get());
    resourceMap.push();
    try
    { return super.bind(focus);
    }
    finally
    { resourceMap.pop();
    }
  }
  
  public void setResourceContextURI(URI resourceContextURI)
  { resourceMap.putDefault(resourceContextURI);
  }
  
  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer()
    {
      { 
        children=new Component[services.length];
        int i=0;
        for (Component service:services)
        { children[i++]=service;
        }
      }
      
      
      @Override
      protected Focus<?> bindChild(Focus<?> context,Component service) 
        throws BindException
      { 
        Focus<?> ret=super.bindChild(context,service);
        if (service instanceof Service && ret!=context)
        { context.addFacet(ret);
        }
        if (service instanceof Context)
        { 
          threadContextuals
            =ArrayUtil.append(threadContextuals,(Context) service);
        }
        return ret;
      }
    };
  }

  @Override
  public void push()
  {
    resourceMap.push();
    for (Context tc : threadContextuals)
    { tc.push();
    }
    
  }

  @Override
  public void pop()
  {
    for (int i=threadContextuals.length;--i>=0;)
    { threadContextuals[i].pop();
    }
    resourceMap.pop();    
  }

}
