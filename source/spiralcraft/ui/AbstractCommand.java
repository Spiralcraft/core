package spiralcraft.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

/**
 * An abstract implementation of the Command interface.
 */
public abstract class AbstractCommand
  implements Command
{

  private final CommandInfo _commandInfo;

  private PropertyChangeSupport _propertyChangeSupport;

  public AbstractCommand()
  { _commandInfo=null;
  }

  public AbstractCommand(CommandInfo info)
  { _commandInfo=info;
  }

  /**
   * Return a CommandInfo object which describes this
   *   Command to the UI
   */
  public CommandInfo getInfo()
  { return _commandInfo;
  }

  /**
   * Execute the command
   */
  public abstract void execute();

  /**
   * Indicate whether the the UI should allow
   *   execution of this command.
   */
  public abstract boolean isEnabled();

  /**
   * Called by subclass to indicate that the enabled state
   *   has changed- fires an appropriate PropertyChangeEvent.
   */
  protected final void enabledChanged()
  {
    if (_propertyChangeSupport!=null)
    { 
      _propertyChangeSupport
        .firePropertyChange
          (isEnabled()
          ?new PropertyChangeEvent
            (this
            ,"enabled"
            ,Boolean.FALSE
            ,Boolean.TRUE
            )
          :new PropertyChangeEvent
            (this
            ,"enabled"
            ,Boolean.TRUE
            ,Boolean.FALSE
            )
          );
    }
  }

  /**
   * Add a listener to be notified when enabled state changes.
   */
  public void addPropertyChangeListener
    (PropertyChangeListener listener)
  {
    if (_propertyChangeSupport==null)
    { _propertyChangeSupport=new PropertyChangeSupport(this);
    }
    _propertyChangeSupport.addPropertyChangeListener(listener);    
  }

  public void removePropertyChangeListener
    (PropertyChangeListener listener)
  {
    if (_propertyChangeSupport!=null)
    {
      _propertyChangeSupport
        .removePropertyChangeListener(listener);
    }
  }
}
