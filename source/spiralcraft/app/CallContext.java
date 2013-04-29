//
//Copyright (c) 2012 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app;

import spiralcraft.common.ContextualException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.kit.AbstractChainableContext;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.util.Path;
import spiralcraft.util.thread.ThreadLocalStack;

/**
 * Maintains the thread scoped state of Calls.
 * 
 * @author mike
 *
 */
public class CallContext
  extends AbstractChainableContext
{
  
  private ThreadLocalStack<CallState> stack
    =new ThreadLocalStack<CallState>();


  @Override
  public Focus<?> bindImports(Focus<?> chain)
    throws ContextualException
  { return super.bindImports(chain.chain(LangUtil.constantChannel(this)));
  }
  /**
   * Return the next segment of the path
   * 
   * @return
   */
  public String getNextSegment()
  { 
    CallState state=stack.get();
    if (state!=null)
    { 
      if (state.nextIndex==state.path.size())
      { return null;
      }
      else
      { return state.path.getElement(state.nextIndex);
      }
    }
    else
    { return null;
    }
  }
  
  public void pushCall(Path path)
  { stack.push(new CallState(path));
  }
  
  public void popCall()
  { stack.pop();
  }
  
  public void descend()
  {
    CallState call=stack.get();
    if (call!=null)
    { 
      if (call.nextIndex==call.path.size())
      { throw new IllegalStateException("Call already at max depth");
      }
      call.nextIndex++;
    }
    else
    { throw new IllegalStateException("No call in progress");
    }
  }
  
  public void ascend()
  {
    CallState call=stack.get();
    if (call!=null)
    { 
      if (call.nextIndex==0)
      { throw new IllegalStateException("Call stack is already unwound");
      }
      call.nextIndex--;
    }
    else
    { throw new IllegalStateException("No call in progress");
    }
  }
}

class CallState
{
  final Path path;
  int nextIndex;

  public CallState(Path path)
  { this.path=path;
  }

}
