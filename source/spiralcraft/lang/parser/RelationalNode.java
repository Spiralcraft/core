package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;

public class RelationalNode
  extends LogicalNode
{

  private final boolean _greaterThan;
  private final boolean _equals;

  public RelationalNode(boolean greaterThan,boolean equals,Node op1,Node op2)
  { 
    super(op1,op2);
    _greaterThan=greaterThan;
    _equals=equals;
  }

  public Object translateForGet(Object val,Optic[] mods)
  { 
    Comparable val1=(Comparable) val;
    Comparable val2=(Comparable) mods[0].get();
    
    if (val1==null)
    { 
      if (val2==null && _equals)
      { return true;
      }
      else
      { return null;
      }
    }
    
    int result=val1.compareTo(val2);
    
    if (_greaterThan)
    {
      if (_equals)
      { return result>=0;
      }
      else
      { return result>0;
      }
    }
    else
    {
      if (_equals)
      { return result<=0;
      }
      else
      { return result<0;
      }
    }
  }
  
  public Object translateForSet(Object val,Optic[] mods)
  { 
    // Not reversible
    throw new UnsupportedOperationException();
  }
  
  public String getSymbol()
  { 
    if (_greaterThan)
    { 
      if (_equals)
      { return ">=";
      }
      else
      { return ">";
      }
    }
    else
    {
      if (_equals)
      { return "<=";
      }
      else
      { return "<";
      }
    }
  }

}
