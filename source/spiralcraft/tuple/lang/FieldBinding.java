package spiralcraft.tuple.lang;

import spiralcraft.tuple.Tuple;

import spiralcraft.lang.optics.LenseBinding;
import spiralcraft.lang.optics.Binding;

/**
 * A binding to a value of a field
 */
public class FieldBinding
  extends LenseBinding
{
  private final FieldLense _lense;
  
  public FieldBinding(Binding source,FieldLense lense)
  { 
    super(source,lense,null);
    _lense=lense;
  }

  /**
   * Field bindings are never static, since the data in a Tuple can
   *   change even if the Tuple does not.
   */
  public boolean isStatic()
  { return false;
  }
  
  public boolean set(Object val)
  { 
    
    Tuple tuple=(Tuple) getSourceValue();
    if (tuple!=null)
    { 
      tuple.set(_lense.getField(),val);
      return true;
    }
    return false;
  }
}
