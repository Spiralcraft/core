package spiralcraft.service;

import java.net.URI;

import spiralcraft.app.Component;
import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.StandardContainer;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Context;
import spiralcraft.util.ArrayUtil;
import spiralcraft.vfs.context.ContextResourceMap;

/**
 * Publishes a set of Services into the focus chain.
 * 
 * @author mike
 *
 */
public class ResourceContext
  extends AbstractComponent
{


  
  ContextResourceMap resourceMap
    =new ContextResourceMap();
  { chainOuterContext(resourceMap);
  }
  
  Context[] threadContextuals
    =new Context[0];
  
  
  public void setResourceContextURI(URI resourceContextURI)
  { resourceMap.putDefault(resourceContextURI);
  }
  
  public void setServices(final Service[] services)
  {
    this.childContainer
      =new StandardContainer(this)
    {
      { 
        log=ResourceContext.this.log;
        logLevel=ResourceContext.this.logLevel;
        
        children=new Component[services.length];
        int i=0;
        for (Component service:services)
        { children[i++]=service;
        }
      }
      
      
      @Override
      protected Focus<?> bindChild(Focus<?> context,Component service) 
        throws ContextualException
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

  public void push()
  {
    resourceMap.push();
    for (Context tc : threadContextuals)
    { tc.push();
    }
    
  }

  public void pop()
  {
    for (int i=threadContextuals.length;--i>=0;)
    { threadContextuals[i].pop();
    }
    resourceMap.pop();    
  }

}
