package spiralcraft.lang.optics;

import spiralcraft.lang.Optic;

import spiralcraft.lang.optics.Lense;
import spiralcraft.lang.optics.Prism;

public class StringConcatLense
  implements Lense
{
  private final Prism _prism;
  
  public StringConcatLense(Prism prism)
  { _prism=prism;
  }
  
  public Prism getPrism()
  { return _prism;
  }

  public Object translateForGet(Object source,Optic[] modifiers)
  { 
    Object modifier=modifiers[0].get();
    if (modifier==null)
    { return source;
    }
    if (source==null)
    { return modifier.toString();
    }
    return ((String) source).concat(modifier.toString());
  }

  public Object translateForSet(Object source,Optic[] modifiers)
  { 
    String string=(String) source;
    Object concat=modifiers[0].get();
    if (concat==null)
    { return string;
    }
    if (string.endsWith(concat.toString()))
    { return string.substring(0,string.indexOf(concat.toString()));
    }
    return null;
  }


}
