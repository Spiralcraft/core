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
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.task;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;

/**
 * <p>Cycles through chained scenarios
 * </p>
 *  
 * @author mike
 */
public class Cycle<Tcontext,Tresult>
  extends Chain<Tcontext,Tresult>
{

  
  { addChainCommandAsResult=true;
  }
  
  private long periodMs;
  private long maxCycles;
  private long maxRuntimeMs;
  private Binding<Tresult> x;
  private boolean addResult=false;
    
  public void setAddResult(boolean addResult)
  { this.addResult=addResult;
  }
  
  public void setX(Binding<Tresult> x)
  { this.x=x;
  }

  /**
   * Terminate after running for the specified time
   * 
   * @param maxCycles
   */
  public void setMaxRuntimeMs(long maxRuntimeMs)
  { this.maxRuntimeMs=maxRuntimeMs;
  }

  /**
   * Terminate after running for the specified number of cycles
   * 
   * @param maxCycles
   */
  public void setMaxCycles(long maxCycles)
  { this.maxCycles=maxCycles;
  }
  
  /**
   * The period of a cycle, measured from start-to-start
   * 
   * @param periodMs
   */
  public void setPeriodMs(long periodMs)
  { this.periodMs=periodMs;
  }
  
  @Override
  public Focus<?> bindExports(Focus<?> focus)
    throws ContextualException
  { 
    if (x!=null)
    { x.bind(focus);
    }
    return super.bindExports(focus);
  }
  
  protected class CycleTask
    extends ChainTask
  {
    
    @Override
    protected void work()
      throws InterruptedException
    { 
      
      long start=System.currentTimeMillis();
      long cycles=0;
      while (true)
      {
        long lastRun=System.currentTimeMillis();
        if (x!=null)
        { 
          Tresult result=x.get();
          if (addResult)
          { addResult(result);
          }
        }
        super.work();
        long end=System.currentTimeMillis();
        
        cycles++;
        if (maxCycles>0 && cycles>=maxCycles)
        { break;
        }
        

        if (maxRuntimeMs>0 && end-start>=maxRuntimeMs)
        { break;
        }
        
        long sleep=periodMs-(end-lastRun);
        if (periodMs>0 && sleep>0)
        { Thread.sleep(sleep);
        }

        end=System.currentTimeMillis();
        if (maxRuntimeMs>0 && end-start>=maxRuntimeMs)
        { break;
        }        
      }
    }
  }
  
  
  @Override
  protected Task task()
  { return new CycleTask();
  }
  
  
}
