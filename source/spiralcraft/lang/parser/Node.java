package spiralcraft.lang.parser;

import spiralcraft.lang.optics.Prism;

import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticAdapter;

/**
 * A Node in an Expression parse tree
 */
public abstract class Node
{

  /**
   * Stubbed bind method for unimplemented nodes.
   *
   *@return An optic with no functionality
   */
  public Optic bind(Focus focus)
    throws BindException
  { 
    System.err.println(getClass().getName()+" not implemented");
    return new OpticAdapter()
    {
      public Prism getPrism()
      { 
        try
        { return OpticFactory.getInstance().findPrism(Void.class);
        }
        catch (BindException x)
        { // shouldn't happen
        }
        return null;
      }
    };
  }

  public abstract void dumpTree(StringBuffer out,String prefix);

}
