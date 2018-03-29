package spiralcraft.service;

import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * Generic service that performs an operation at intervals.
 * 
 * @author mike
 *
 */
public class WorkerService<Tc>
  extends ThreadService
  implements Service
{
  { setAutoStart(false);
  }
  
  private Binding<Void> action;
  private Binding<Tc> context;
  private SimpleChannel<Tc> contextChannel;
  
  public void setContext(Binding<Tc> context)
  { this.context=context;
  }
  
  /**
   * The action that will run periodically
   * @param action
   */
  public void setAction(Binding<Void> action)
  { 
    this.removeExportContextual(this.action);
    this.action=action;
    this.addExportContextual(this.action);
  }
  
  @Override
  protected void runOnce()
  {
    if (this.action!=null)
    { this.action.get();
    }
  }
  
  @Override
  public void start()
    throws LifecycleException
  { 
    resetContext();
    super.start();
    
  }
  
  public void resetContext()
  {
    if (context!=null)
    { contextChannel.set(context.get());
    }
  }
  
  @Override
  protected Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    if (context!=null)
    {
      context.bind(chain);
      contextChannel=new SimpleChannel<Tc>(context.getReflector());
      chain=chain.chain(contextChannel);
    }
    return super.bindImports(chain);
  }
  
}
