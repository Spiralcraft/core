package spiralcraft.tuple.lang;

import spiralcraft.tuple.Tuple;

import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.Binding;

public class FieldBinding
  extends LenseBinding
{
  private final FieldLense _lense;
  
  public FieldBinding(Binding source,FieldLense lense)
  { 
    super(source,lense,null);
    _lense=lense;
  }


  public boolean set(Object val)
  { 
    if (isStatic())
    { return false;
    }
    
    Tuple tuple=(Tuple) getSourceValue();
    if (tuple!=null)
    { 
      tuple.set(_lense.getField(),val);
      return true;
    }
    return false;
  }
}
