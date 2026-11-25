package spiralcraft.task;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;

/**
 * <p>One case of a Switch: the scenarios it runs when the Switch's value
 *   equals its constant or, for the default Case, when no Case's does.
 * </p>
 *
 * <p>The constant is given as text, converted to the type of the Switch's
 *   value, or as an expression, evaluated once when the Case is bound.
 * </p>
 */
public class Case<T>
  extends Chain<Void,Void>
{

  private String constant;
  private Binding<T> constantX;
  private boolean deflt;

  /** The constant as text, converted by the value's reflector */
  public void setConstant(String constant)
  { this.constant=constant;
  }

  public String getConstant()
  { return constant;
  }

  /** The constant as an expression, evaluated once at bind */
  public void setConstantX(Binding<T> constantX)
  { this.constantX=constantX;
  }

  public Binding<T> getConstantX()
  { return constantX;
  }

  /** Whether this Case runs when no Case's constant matches */
  public void setDefault(boolean deflt)
  { this.deflt=deflt;
  }

  public boolean isDefault()
  { return deflt;
  }

  @Override
  protected Focus<?> bindImports(Focus<?> focusChain)
    throws ContextualException
  {
    if (constantX!=null)
    { constantX.bind(focusChain);
    }
    return super.bindImports(focusChain);
  }
}
