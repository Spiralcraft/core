package spiralcraft.ui;

/**
 * A Step for use in a StepControl. In concrete subclasses
 *   a Step usually references a concrete UI component
 *   to be displayed when this Step is active.
 */
public abstract class Step
  implements Control
{
  private Command _onEnter;
  private Command _onExit;
  private boolean _skipNext;
  private boolean _skipBack;
  private boolean _backEnabled=true;
  private boolean _nextEnabled=true;
  
  public void setSkipNext(boolean val)
  { _skipNext=val;
  }
  
  public boolean getSkipNext()
  { return _skipNext;
  }

  public void setSkipBack(boolean val)
  { _skipBack=val;
  }
  
  public boolean getSkipBack()
  { return _skipBack;
  }

  public boolean isBackEnabled()
  { return _backEnabled;
  }
  
  public void setBackEnabled(boolean val)
  { _backEnabled=val;
  }
  
  public boolean isNextEnabled()
  { return _nextEnabled;
  }
  
  public void setNextEnabled(boolean val)
  { _nextEnabled=val;
  }

  public void setOnEnter(Command command)
  { _onEnter=command;
  }

  public void setOnExit(Command command)
  { _onExit=command;
  }

  void stepEntered()
  { 
    if (_onEnter!=null)
    { _onEnter.execute();
    }
  }

  void stepExited()
  {
    if (_onExit!=null)
    { _onExit.execute();
    }
  }
}
