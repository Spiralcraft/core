package spiralcraft.ui;

import java.beans.PropertyChangeSupport;

/**
 * Information about a Command for presentation purposes 
 */
public class CommandInfo
  extends PropertyChangeSupport
{

  private String _codeName;
  private String _buttonName;
  private String _shortDescription;
  private String _longDescription;

  public CommandInfo()
  { super(null);
  }

  /**
   * The name used in program code for this command
   */
  public String getCodeName()
  { return _codeName;
  }

  /**
   * The name used on a button or in a menu
   *   or a dictionary key to such a name.
   */
  public String getButtonName()
  { return _buttonName;
  }

  /**
   * A brief sentence describing this command,
   *   or a dictionary key to such a description
   */
  public String getShortDescription()
  { return _shortDescription;
  }

  
  /**
   * A long description of this command.
   *   or a dictionary key to such a description
   */
  public String getLongDescription()
  { return _longDescription;
  }

  /**
   * A dictionary key to an icon or image associated
   *   with this command.
   */
  public String getIconKey()
  { return _longDescription;
  }
}
