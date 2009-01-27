//
// Copyright (c) 2008,2009 Michael Toth
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
//
package spiralcraft.log;


import spiralcraft.common.LifecycleException;
import spiralcraft.exec.ExecutionContext;

public class ConsoleHandler
  implements EventHandler
{

  private static final Formatter DEFAULT_FORMATTER
    =new DefaultFormatter();
  
  private Formatter formatter=DEFAULT_FORMATTER;
  
  @Override
  public synchronized void handleEvent(Event event)
  { 
    ExecutionContext.getInstance().err().println(formatter.format(event));
  }

  @Override
  public void start()
    throws LifecycleException
  { handleEvent(Event.create(null,Level.INFO,"ConsoleHandler started"));
  }

  @Override
  public void stop()
    throws LifecycleException
  {
  }

}
