package spiralcraft.command;

/**
 * A one-time association between a Command, a CommandContext, and 
 *   a ParameterSet.
 *
 * An Invocation is created by resolving a command and a set
 *   of parameters within a given CommandContext.
 */
public class Invocation
{
  private final Command _command;
  private final CommandContext _context;
  private final ParameterSet _params;
  private Object _result;
  private Throwable _throwable;
  private boolean _invoked;  

  Invocation
    (CommandContext context
    ,Command command
    )
  { 
    _context=context;
    _command=command;
    _params=command.newParameterSet();
  }
  

  public ParameterSet getParameterSet()
  { return _params;
  }
  
  public synchronized void invoke()
  { 
    if (_invoked)
    { throw new IllegalStateException("Invocation can only be invoked once");
    }
    try
    { _result=_command.execute(_context,_params);
    }
    catch (Throwable x)
    { _throwable=x;
    }
    finally
    { _invoked=true;
    }
  }
  
  public Throwable getThrowable()
  { return _throwable;
  }
  
  public Object getResult()
  { return _result;
  }
  
  public boolean failed()
  { return _throwable!=null;
  }
}
