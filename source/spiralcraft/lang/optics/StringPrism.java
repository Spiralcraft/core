package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * An abstract Optic for representing a String
 */
public class StringPrism
  extends BeanPrism
{
  private final Lense _concatLense;

  public StringPrism()
  { 
    super(String.class);
    _concatLense=new StringConcatLense(this);
  }
  
  public synchronized Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    if (name.equals("+"))
    { 
      return new LenseBinding
        (source
        ,_concatLense
        ,new Optic[] {focus.bind(params[0])}
        );
    }
    return super.resolve(source,focus,name,params);
  }

}

class StringConcatLense
  implements Lense
{
  private final Prism _prism;
  
  public StringConcatLense(Prism prism)
  { _prism=prism;
  }
  
  public Prism getPrism()
  { return _prism;
  }

  public Object translateForGet(Object source,Object[] modifiers)
  { 
    if (modifiers[0]==null)
    { return source;
    }
    if (source==null)
    { return (String) modifiers[0];
    }
    return ((String) source).concat((String) modifiers[0]);
  }

  public Object translateForSet(Object source,Object[] modifiers)
  { 
    String string=(String) source;
    String concat=(String) modifiers[0];
    if (string.endsWith(concat))
    { return string.substring(0,string.indexOf(concat));
    }
    return null;
  }


}
