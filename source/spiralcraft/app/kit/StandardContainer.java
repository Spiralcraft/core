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



import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spiralcraft.app.CallContext;
import spiralcraft.app.CallMessage;
import spiralcraft.app.Component;
import spiralcraft.app.Container;
import spiralcraft.app.DispatchFilter;
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Message;
import spiralcraft.app.Parent;
import spiralcraft.common.ContextualException;
import spiralcraft.common.LifecycleException;
import spiralcraft.common.Lifecycler;
import spiralcraft.lang.Focus;
import spiralcraft.lang.util.LangUtil;
import spiralcraft.log.Level;
import spiralcraft.log.Log;
import spiralcraft.util.ListMap;

public class StandardContainer
  implements Container
{


  
  protected Log log;
  protected Component[] children;
  protected Level logLevel=Level.INFO;
  protected final Parent parent;
  protected ListMap<Message.Type,Integer> childSubscriptions;
  protected HashSet<Message.Type> subscribedTypes;
  protected HashMap<String,Integer> idMap;
  protected CallContext callContext;
  protected DispatchFilter dispatchFilter;

  public StandardContainer(Parent parent)
  { this.parent=parent;
  }
  
  public StandardContainer(Parent parent,Component[] children)
  { 
    this.parent=parent;
    this.children=children;
  }
  
  public void setLog(Log log)
  { this.log=log;
  }

  public void setLogLevel(Level level)
  { this.logLevel=level;
  }
  
  @Override
  public void bind(
    Focus<?> focusChain)
    throws ContextualException
  { 
    callContext=LangUtil.findInstance(CallContext.class,focusChain);
    resolveChildren(focusChain);
    bindChildren(focusChain);
  }
  
  @Override
  public Set<Message.Type> getSubscribedTypes()
  { return subscribedTypes;
  }
  
  public void setDispatchFilter(DispatchFilter filter)
  { this.dispatchFilter=filter;
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
      int index=0;
      subscribedTypes=new HashSet<Message.Type>();
      for (Component child:children)
      { 
        bindChild(focusChain,child);
        String id=child.getId();
        if (id!=null)
        { 
          if (idMap==null)
          { idMap=new HashMap<String,Integer>();
          }
          idMap.put(id,index);
        }
        Message.Type[] types=child.getSubscribedTypes();
        if (types!=null)
        {
          if (childSubscriptions==null)
          { childSubscriptions=new ListMap<Message.Type,Integer>();
          }
          
          for (Message.Type type : types)
          { 
            childSubscriptions.add(type,index);
            subscribedTypes.add(type);
          }
        }
        
        index++;
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
   * Resolve the state index that corresponds to the next path element in the
   *   call path.
   * 
   * @param dispatcher
   * @return
   */
  protected Integer resolveCallIndex(Dispatcher dispatcher)
  { 
    if (callContext!=null)
    { 
      String key=callContext.getNextSegment();
      if (key!=null)
      { 
        Integer id=idMap.get(key);
        if (id==null)
        { 
          throw new RuntimeException
            (new ContextualException
              ("Call path segment '"+key+"' not found"
              ,parent.asComponent().getDeclarationInfo()
              )
            );
        }
        return id;
      }
      else
      { 
        if (logLevel.isFine())
        { log.log(Level.FINE,"Path segment for call is null");
        }
      }
    }
    else
    { 
      if (logLevel.isFine())
      { log.log(Level.FINE,"No callContext for call");
      }
    }
    return null;
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
      { 
        if (logLevel.isFine())
        { log.log(Level.FINE,"Relaying "+message+" to child #"+index);
        }
        
        dispatcher.relayMessage(children[index],index,message);
      }
      else if (message.getType()==CallMessage.TYPE 
               && (index=resolveCallIndex(dispatcher))!=null
              )
      { 
        if (logLevel.isFine())
        { log.log(Level.FINE,"Relaying Call "+message+" to child #"+index);
        }
        
        callContext.descend();
        try
        { dispatcher.relayMessage(children[index],index,message);
        }
        finally
        { callContext.ascend();
        }
        
      }
      else if (message.isMulticast())
      { 
        if (logLevel.isFine())
        { 
          log.log
            (Level.FINE
            ,"Multicasting "+message+" to "+children.length+" children"
            );
        }
        
        for (int i=0;i<children.length;i++)
        { 
          if (dispatchFilter==null 
              || dispatchFilter.applies
                (dispatcher, message, i , children[i])
             )
          { dispatcher.relayMessage(children[i],i,message);
          }
        }
      }
      else if (childSubscriptions!=null)
      {
        
        List<Integer> subscribers=childSubscriptions.get(message.getType());
        if (subscribers!=null)
        { 
          if (logLevel.isFine())
          { 
            log.log
              (Level.FINE,"Sending "+message+" to subscribers "+subscribers);
          }
          
          for (int i:subscribers)
          { 
            if (dispatchFilter==null 
                || dispatchFilter.applies
                  (dispatcher, message, i , children[i])
               )
            { dispatcher.relayMessage(children[i],i,message);
            }
          }
        }
        else
        {
          if (logLevel.isFine())
          { log.log(Level.FINE,"No subscribers for "+message);
          }
        }
        
      }
      else
      {
        
        if (logLevel.isFine())
        { log.log(Level.FINE,"Branch complete for "+message);
        }

      }
    }
    else
    { 
      if (logLevel.isFine())
      { log.log(Level.FINE,"No children to relay "+message);
      }
    }
  }
  


}
