package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;

/**
 * Exposes a named set of Bindings accessible from the source Binding.
 *
 * Prisms are used to support Optic.resolve() for a specific targetClass,
 *   independently of the actual source of data.
 */
public interface Prism
{

  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException;

}
