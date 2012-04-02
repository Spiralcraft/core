//
// Copyright (c) 1998,2012 Michael Toth
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
import spiralcraft.app.Dispatcher;
import spiralcraft.app.Pipe;
import spiralcraft.app.Message;
import spiralcraft.app.State;
import spiralcraft.app.StateFrame;
//import spiralcraft.log.ClassLog;
import spiralcraft.util.Sequence;

/**
 * A generic and common implementation of an Endpoint
 * 
 * @author mike
 *
 */
public class StandardPipe
  implements Pipe
{
//  private static final ClassLog log
//    =ClassLog.getInstance(StandardPipe.class);
  
  private final Sequence<Integer> path;
  private final Component root;
  private final State rootState;

  public StandardPipe
    (Component root,State rootState,Sequence<Integer> path)
  { 
    this.path=path;
    this.root=root;
    this.rootState=rootState;
  }
    
  @Override
  public void message(Message message)
  { newDispatcher().dispatch(message,root,rootState,path);    
  }

  protected Dispatcher newDispatcher()
  { return new StandardDispatcher(true,currentFrame());
  }
  
  protected StateFrame currentFrame()
  { return new StateFrame();
  }
}
