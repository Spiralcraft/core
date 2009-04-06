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

import spiralcraft.command.CommandFactory;

/**
 * A Step for use in a StepControl. In concrete subclasses
 *   a Step usually references a concrete UI component
 *   to be displayed when this Step is active.
 */
public abstract class Step
  implements Control
{
  private CommandFactory<?,?> _onEnter;
  private CommandFactory<?,?> _onExit;
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

  public void setOnEnter(CommandFactory<?,?> commandFactory)
  { _onEnter=commandFactory;
  }

  public void setOnExit(CommandFactory<?,?> commandFactory)
  { _onExit=commandFactory;
  }

  void stepEntered()
  { 
    if (_onEnter!=null)
    { _onEnter.command().execute();
    }
  }

  void stepExited()
  {
    if (_onExit!=null)
    { _onExit.command().execute();
    }
  }
}
