//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.ui;

import java.beans.PropertyChangeSupport;

/**
 * Information about a Command for presentation purposes 
 */
public class CommandInfo
  extends PropertyChangeSupport
{

  private static final long serialVersionUID = 1L;

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
