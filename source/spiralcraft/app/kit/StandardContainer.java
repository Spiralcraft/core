//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.app.kit;



import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.Parent;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.Focus;
import spiralcraft.log.Level;

public class StandardContainer
  implements Container
{


  
  
  protected Component[] children;
  protected Level logLevel=Level.INFO;
  protected final Parent parent;

  public StandardContainer(Parent parent)
  { this.parent=parent;
  }
  
  public StandardContainer(Parent parent,Component[] children)
  { 
    this.parent=parent;
    this.children=children;
  }
  
  @Override
  public void bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    resolveChildren(focusChain);
    bindChildren(focusChain);
  }
  
  /**
   * Override to generate/decorate children based on the exported data
   *   type
   * 
   * @param focusChain
   */
  protected final void resolveChildren(Focus<?> focusChain)
  {
  }
  
  protected final void bindChildren(Focus<?> focusChain)
    throws ContextualException
  { 
    if (children!=null)
    {
      for (Component child:children)
      { bindChild(focusChain,child);
      }
    }
  }
  
  protected Focus<?> bindChild(Focus<?> context,Component child) 
    throws ContextualException
  { 
    child.setParent(parent);
    return child.bind(context);
  }

  @Override
  public Component getChild(
    int childNum)
  { 
    if (children!=null)
    { return children[childNum];
    }
    else
    { throw new IndexOutOfBoundsException(childNum+" >= 0");
    }
  }


  @Override
  public int getChildCount()
  { 
    if (children!=null)
    { return children.length;
    }
    else
    { return 0;
    }
  }


  @Override
  public Component[] getChildren()
  { return children;
  }
  



  @Override
  public void start()
    throws LifecycleException
  { Lifecycler.start(children);
  }


  @Override
  public void stop()
    throws LifecycleException
  { Lifecycler.stop(children);
  }

 
  /**
   * <p>Relay a message to the appropriate children
   *   as indicated by the message path.
   * </p>
   * 
   * <p>This method is called by a Component once the pre-order stage of
   *   its local message processing has been completed. When this method
   *   returns, the Component can complete the post-order stage of
   *   its local message processing.
   * </p>
   * 
   * @param dispatcher The dispatcher
   * @param message The message to relay
   *
   */
  @Override
  public void relayMessage(Dispatcher dispatcher,Message message)
  { 
    if (children!=null)
    {
      Integer index=dispatcher.getNextRoute();
      if (index!=null)
      { dispatcher.relayMessage(children[index],index,message);
      }
      else if (message.isMulticast())
      { 
        for (int i=0;i<children.length;i++)
        { dispatcher.relayMessage(children[i],i,message);
        }
      }
    }
  }
  


}
