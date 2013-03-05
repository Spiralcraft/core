package spiralcraft.lang.kit;

import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.common.ContextualException;

public class BindingContext
  extends AbstractChainableContext
{
  
  private Binding<?>[] bindings;
  
  public BindingContext()
  {
  }
  
  public BindingContext(Binding<?>[] bindings)
  { this.bindings=bindings;
  }
  
  public void setBindings(Binding<?>[] bindings)
  { this.bindings=bindings;
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { 
    chain=chain.chain(chain.getSubject());
    if (bindings!=null)
    {
      for (Binding<?> binding:bindings)
      { 
        binding.bind(chain);
        chain.addFacet(chain.chain(binding));
      }
    }    
    return chain;
  }
}
