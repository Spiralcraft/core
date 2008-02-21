//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.session;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.FocusProvider;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.data.Type;
import spiralcraft.data.Field;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Tuple;

import spiralcraft.data.lang.DataBinding;

import spiralcraft.builder.Lifecycle;
import spiralcraft.builder.LifecycleException;

import java.util.ArrayList;

public class DataSession
  implements FocusProvider<DataSession.State>,Lifecycle
{
  
  private CompoundFocus<State> focus;
  private Type<SessionData> sessionDataType;
  private Channel<State> sessionDataSource;
  private ArrayList<View<?>> views;
  
  
  @Override
  public Focus<State> createFocus(Focus<?> parent)
    throws BindException
  {
    if (sessionDataSource==null)
    { 
      sessionDataSource
        =new SimpleChannel<State>(State.class,null,false);
    }
    
 
    this.focus=new CompoundFocus<State>(parent,sessionDataSource);
    this.focus.setLayerName("spiralcraft.data");

    DataBinding<EditableTuple> dataBinding
      =new DataBinding<EditableTuple>
        (sessionDataType
        ,sessionDataSource.resolve(this.focus,"sessionDataTuple",null)
        ,false
        );
    
    this.focus.bindFocus
      ("spiralcraft.data",new SimpleFocus<EditableTuple>(this.focus,dataBinding));

    return this.focus;
    

  }

  /**
   * Specify an alternate session data source, such as when session data
   *   is being externally persisted or state-managed.
   * @param channel
   */
  public void setSessionDataSource(Channel<State> channel)
  { this.sessionDataSource=channel;
  }
  
  @Override
  public void start()
    throws LifecycleException
  {
    for (Field field: sessionDataType.getScheme().fieldIterable())
    { 
      if (field instanceof SessionField)
      { 
        View<?> view=((SessionField) field).getView();
        if (view!=null)
        { views.add(view);
        }
      }
      
      
    }
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stop()
    throws LifecycleException
  {
    // TODO Auto-generated method stub
    
  }


  public class State
  {
    private EditableTuple sessionDataTuple;
    private final View<?>.State[] viewStates=new View<?>.State[views.size()];
    
    public State()
    { 
      int ctr=0;
      for (View<?> view : views)
      { viewStates[ctr++]=view.newState();
      }
    }
    
    public DataSession getDataSession()
    { return DataSession.this;
    }
    
    public EditableTuple getSessionDataTuple()
    { return sessionDataTuple;
    }
    
    
    
  }
}


