package spiralcraft.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

/**
 * An abstract implementation of the Command interface.
 *
 * Provides built-in support for implementing 'enabled' property
 *   changes.
 */
public abstract class AbstractCommand
  implements Command
{

  private final CommandInfo _commandInfo;
  private PropertyChangeSupport _propertyChangeSupport;
  private boolean _wasEnabled;

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
   * Called by Command containers to allow a Command
   *   to reset its state when the context changes.
   *
   * Overriding methods should call super.reset() after
   *   any changes are made which would affect the enabled
   *   state of the command.
   */
  public void reset()
  { enabledChanged();
  }
  
  /**
   * Called by subclass to indicate that the enabled state
   *   has changed- fires an appropriate PropertyChangeEvent.
   */
  protected final void enabledChanged()
  {
    boolean isEnabled=isEnabled();
    if (_propertyChangeSupport!=null
        && _wasEnabled!=isEnabled
       )
    { 
      _propertyChangeSupport
        .firePropertyChange
          (isEnabled
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
    _wasEnabled=isEnabled;
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
