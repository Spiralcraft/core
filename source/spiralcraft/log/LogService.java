//
// Copyright (c) 1998,2018 Michael Toth
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
package spiralcraft.log;

import java.io.IOException;
import java.util.HashMap;

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.service.Service;

/**
 * Provides access to one or more named log sinks that can be used to record
 *   information from application components.
 *   
 * The log sinks are structure-agnostic and are usually asychronous.
 * 
 * @author mike
 *
 */
public class LogService
  extends AbstractComponent
  implements Service
{
  private HashMap<String,LogSink> sinkMap=new HashMap<>();
  
  public void setSinks(LogSink[] sinks)
  { 
    this.setContents(sinks);
    for (LogSink sink:sinks)
    { sinkMap.put(sink.name, sink);
    }
  }
  
  public void write(String sinkName,String line)
  {
    LogSink sink=sinkMap.get(sinkName);
    
    if (sink==null)
    { 
      // XXX send the message to a default log?
      throw new IllegalArgumentException("No log named '"+sinkName+"' found");
    }
    try
    { sink.write(line);
    }
    catch (IOException x)
    { throw new RuntimeException("IOException writing message to log '"+sinkName+"': "+line,x);
    }
  }
  
  public LogSink getSink(String sinkName)
  { return sinkMap.get(sinkName);
  }
}
