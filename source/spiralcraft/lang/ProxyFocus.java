package spiralcraft.lang;

/**
 * Delegates to another Focus.
 */
public class ProxyFocus
  implements Focus
{
  private Focus _focus;

  public void setFocus(Focus val)
  { _focus=val;
  }

  public Environment getEnvironment()
  { 
    if (_focus!=null)
    { return _focus.getEnvironment(); 
    }
    else
    { return null;
    }
  }

  public Optic getSubject()
  { 
    if (_focus!=null)
    { return _focus.getSubject();
    }
    else
    { return null;
    }

  }

  public Focus findFocus(String name)
  { 
    if (_focus!=null)
    { return _focus.findFocus(name);
    }
    else
    { return null;
    }
  }

  public Focus getParentFocus()
  { 
    if (_focus!=null)
    { return _focus.getParentFocus();
    }
    else
    { return null;
    }
  }

  public Channel bind(Expression expression)
    throws BindException
  { 
    if (_focus!=null)
    { return _focus.bind(expression);
    }
    else
    { throw new BindException("No focus");
    }
  }
  
}
