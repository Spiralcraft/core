package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * An abstract Optic for representing a Number
 */
public class StringPrism
  extends BeanPrism
{
  private static final Lense _STRING_CONCAT_LENSE=new StringConcatLense();

  public StringPrism()
  { super(String.class);
  }
  
  public synchronized Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    if (name.equals("+"))
    { 
      return new LenseBinding
        (source
        ,_STRING_CONCAT_LENSE
        ,new Optic[] {focus.bind(params[0])}
        );
    }
    return super.resolve(source,focus,name,params);
  }

}

class StringConcatLense
  implements Lense
{

  public Class getTargetClass()
  { return String.class;
  }

  public Object translateForGet(Object source,Object[] modifiers)
  { 
    if (modifiers[0]==null)
    { return source;
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
