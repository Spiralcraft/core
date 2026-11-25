package spiralcraft.task;

import java.util.HashMap;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.util.string.StringConverter;

/**
 * <p>A Scenario which runs the one Case whose constant equals the value of
 *   its expression, or the default Case when none does. A null value takes
 *   the default. With no match and no default, nothing runs.
 * </p>
 *
 * <p>The task counterpart of spiralcraft.app.components.Switch.
 * </p>
 */
public class Switch<T>
  extends Branch<Void,Void>
{

  private Binding<T> x;
  private Case<?>[] cases=new Case<?>[0];
  private final HashMap<T,Case<?>> caseMap=new HashMap<>();
  private Case<?> defaultCase;

  /** The value matched against each Case's constant */
  public void setX(Binding<T> x)
  { this.x=x;
  }

  public void setCases(Case<?> ... cases)
  {
    children.removeIf(child -> child instanceof Case);
    this.cases=cases;
    for (Case<?> c:cases)
    { children.add(c);
    }
  }

  @Override
  protected Focus<?> bindExports(Focus<?> focusChain)
    throws BindException
  {
    if (x==null)
    { throw new BindException("Switch has no x",getDeclarationInfo());
    }
    x.bind(focusChain);
    return focusChain;
  }

  @Override
  protected void bindChildren(Focus<?> focusChain)
    throws ContextualException
  {
    super.bindChildren(focusChain);
    caseMap.clear();
    defaultCase=null;
    for (Case<?> c:cases)
    {
      if (c.isDefault())
      {
        if (defaultCase!=null)
        {
          throw new BindException
            ("Switch has more than one default Case",getDeclarationInfo());
        }
        defaultCase=c;
      }
      if (c.getConstant()!=null && c.getConstantX()!=null)
      {
        throw new BindException
          ("Case has both constant and constantX",c.getDeclarationInfo());
      }
      if (c.getConstant()==null && c.getConstantX()==null)
      {
        if (!c.isDefault())
        {
          throw new BindException
            ("Case has no constant and is not the default"
            ,c.getDeclarationInfo()
            );
        }
        continue;
      }
      T constant=constantOf(c);
      if (caseMap.containsKey(constant))
      {
        throw new BindException
          ("Switch repeats the constant "+constant,c.getDeclarationInfo());
      }
      caseMap.put(constant,c);
    }
  }

  @SuppressWarnings("unchecked")
  private T constantOf(Case<?> c)
    throws BindException
  {
    if (c.getConstantX()!=null)
    { return (T) c.getConstantX().get();
    }
    StringConverter<T> converter=x.getReflector().getStringConverter();
    if (converter!=null)
    { return converter.fromString(c.getConstant());
    }
    if (x.getContentType()==String.class)
    { return (T) c.getConstant();
    }
    throw new BindException
      ("No conversion from text to "+x.getContentType().getName()
        +" for the constant "+c.getConstant()
      ,c.getDeclarationInfo()
      );
  }

  @Override
  protected Scenario<?,?> select()
  {
    T value=x.get();
    Case<?> selected=value!=null?caseMap.get(value):null;
    if (selected==null)
    { selected=defaultCase;
    }
    if (debug)
    { log.fine("Value "+value+" selects "+selected+": "+x.getText());
    }
    return selected;
  }
}
