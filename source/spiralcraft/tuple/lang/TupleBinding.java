package spiralcraft.tuple.lang;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.Binding;
import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Prism;

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Scheme;

import java.beans.PropertyChangeSupport;

/**
 * A spiralcraft.lang binding for Tuples
 */
public class TupleBinding
  extends AbstractBinding
{
  private Tuple _tuple;
  
  public TupleBinding(Scheme scheme)
    throws BindException
  { super(SchemePrism.getInstance(scheme),false);
  }
  
  public TupleBinding(Scheme scheme,Tuple data)
    throws BindException
  { 
    super(SchemePrism.getInstance(scheme),true);
    _tuple=data;
  }

  public Scheme getScheme()
  { return ((SchemePrism) getPrism()).getScheme();
  }

  protected Object retrieve()
  { return _tuple;
  }
  
  protected boolean store(Object val)
  { 
    _tuple=(Tuple) val;
    return true;
  }

}

