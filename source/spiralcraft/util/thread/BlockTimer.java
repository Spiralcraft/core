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
package spiralcraft.util.thread;

import spiralcraft.time.Clock;

public class BlockTimer
{

  private static final BlockTimer INSTANCE=new BlockTimer();
  
  public static final BlockTimer instance()
  { return INSTANCE;
  }
      
  private final Clock clock=Clock.instance();
  
  private ThreadLocalStack<Long> threadTime
    =new ThreadLocalStack<Long>();
  
  public long elapsedTimeNanos()
  { return clock.timeNanos()-threadTime.get();
  }
  
  public double elapsedTimeMillis()
  { return elapsedTimeNanos()/1000000.0;
  }
  
  public double elapsedTimeSeconds()
  { return elapsedTimeNanos()/1000000000.0;
  }
  
  public String elapsedTimeFormatted()
  { 
    long time=elapsedTimeNanos();
    if (time<10000)
    { return time+"ns";
    }
    else if (time<10000000)
    { return (time/1000000.0)+"ms";
    }
    else 
    { return (time/1000000000.0)+"s";
    }
  }
  
  public void reset()
  { threadTime.set(clock.timeNanos());
  }
  
  public void push()
  { threadTime.push(clock.timeNanos());
  }
  
  public void pop()
  { threadTime.pop();
  }
}
