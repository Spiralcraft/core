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
