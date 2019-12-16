package spiralcraft.service;

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.ConstantChannel;
import spiralcraft.lang.spi.VoidChannel;

public abstract class AbstractService
  extends AbstractComponent
  implements Service
{
  private Binding<?>[] publishedConstantImports;
  /**
   * Specify bindings that will be evaluated at bind-time and the result made available
   *   to this Service and its children in the focus chain.
   *   
   * Evaluated before bindImports is called in AbstractComponent so generic
   *   functionality can access the constant imports
   * 
   * @param publishedConstants
   */
  public void setPublishedConstantImports(Binding<?>[] publishedConstantImports)
  { this.publishedConstantImports=publishedConstantImports;
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes"})
  protected Focus<?> bindImports(Focus<?> importFocus)
    throws ContextualException
  {
    Focus<?> focus=importFocus;
    if (publishedConstantImports!=null)
    {
      // Parent channel may get in the way
      focus=focus.chain(new VoidChannel());    
      for (Binding<?> c : publishedConstantImports)
      { 
        c.bind(importFocus);
        ConstantChannel cch=new ConstantChannel(c);
        focus.addFacet(focus.chain(cch));
      }
    }
    return super.bindImports(focus);
  }
}
