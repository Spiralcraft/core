package spiralcraft.ui;

import java.beans.PropertyChangeListener;

/**
 * Implements the Command design pattern, permitting isolation of UI and
 *   application logic. Applications that wish to expose behavioral control
 *   to a UI should provide it through the Command interface 
 */
public interface Command
{
  /**
   * Return a CommandInfo object which describes this
   *   Command to the UI
   */
  public CommandInfo getInfo();

  /**
   * Execute the command
   */
  public void execute();

  /**
   * Indicate whether the the UI should allow
   *   execution of this command.
   */
  public boolean isEnabled();

  /**
   * Add a listener to be notified when enabled state changes.
   */
  public void addPropertyChangeListener
    (PropertyChangeListener listener);

  public void removePropertyChangeListener
    (PropertyChangeListener listener);
}
