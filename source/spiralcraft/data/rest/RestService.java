//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.rest;


import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.DataSession;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;

import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.ThreadedFocusChainObject;
import spiralcraft.lang.reflect.BeanFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;

import spiralcraft.log.ClassLog;

import spiralcraft.util.thread.ContextFrame;
import spiralcraft.util.thread.Delegate;
import spiralcraft.util.thread.DelegateException;

/**
 * <p>Integrates the results of a rest query into the application data model
 * </p>
 * 
 * 
 * @author mike
 */
public class RestService
  implements ThreadedFocusChainObject
{
  
  private static final ClassLog log
    =ClassLog.getInstance(RestService.class);
  
  private Expression<DataComposite> modelExpression;
  
  private Channel<DataSession> sessionChannel;
  private Channel<DataComposite> modelSourceChannel;
  
  private ThreadLocalChannel<Tuple> localQueryChannel;
  
  private ThreadLocalChannel<Buffer> localModelChannel;
  
  private Assignment<?>[] postAssignments;
  private Setter<?>[] postSetters;
  
  private RestClient restClient;
  
  private Expression<Boolean> errorExpression;
  private Channel<Boolean> errorChannel;
  private int errorRetries;
  private int errorRetryDelayMS;
  
  private boolean debug;
  private Boolean useDataSession;
  
  private Delegate<Tuple> delegate
    =new Delegate<Tuple>()
  { 
    public Tuple run()
      throws DelegateException
    {
      try
      { return queryImpl();
      }
      catch (Exception x)
      { throw new DelegateException(x);
      }
    }
  };
  
  public void setRestClient(RestClient client)
  { restClient=client;
  }

  /**
   * <p>Indicate whether a DataSession should be used to edit persistent
   *   model objects. Defaults to false, but automatically set to true
   *   if the model object is supplied.
   * </p>
   * @param useDataSession
   */
  public void setUseDataSession(boolean useDataSession)
  { this.useDataSession=useDataSession;
  }
  
  /**
   * <p>Specify an expression that resolves to the application data model
   *   object which will be buffered for update and available to
   *   the Assignments specified via the postAssignments property.
   *   
   *   XXX Buffering functionality should be integrated into TupleFrameHandler
   *     which can avail itself of the spiralcraft.data.editor package to do the
   *     buffer management.
   * </p>
   * 
   * @param modelExpression
   */
  public void setModelExpression(Expression<DataComposite> modelExpression)
  { this.modelExpression=modelExpression;
  }
  
  public void setErrorExpression(Expression<Boolean> errorExpression)
  { this.errorExpression=errorExpression;
  }
  
  public void setErrorRetries(int errorRetries)
  { this.errorRetries=errorRetries;
  }
  
  public void setErrorRetryDelayMS(int errorRetryDelayMS)
  { this.errorRetryDelayMS=errorRetryDelayMS;
  }
  
  /**
   * <p>Specify the set of assignments that will be applied after the query 
   *   completes
   * </p>
   * 
   * @param assignments
   */
  public void setPostAssignments(Assignment<?>[] postAssignments)
  { this.postAssignments=postAssignments;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public Focus<?> bind(Focus<?> parentFocus) 
    throws BindException
  {
    
    // Note: We can't expose any of the ThreadLocalChannels, because they
    //   are only populated under this objects stack, and the data won't be
    //   available  to outside users.
    
    sessionChannel=DataSession.findChannel(parentFocus);
    
    if (modelExpression!=null)
    { 
      useDataSession=true;
      modelSourceChannel=parentFocus.bind(modelExpression);
      if (!(modelSourceChannel.getReflector() instanceof DataReflector<?>))
      { 
        throw new BindException
          ("Property 'modelExpression' can only refer to "
          +" a DataComposite type, not a "+modelSourceChannel.getReflector()
            .getContentType().getName()
          );
      }
      DataReflector<DataComposite> reflector
        =(DataReflector<DataComposite>) modelSourceChannel.getReflector();
      
      Type<Buffer> bufferType=Type.getBufferType(reflector.getType());
      localModelChannel
        =new ThreadLocalChannel<Buffer>
          (DataReflector.<Buffer>getInstance(bufferType));
    }
    
    // If not specified already, don't use it
    if (useDataSession==null)
    { useDataSession=false;
    }
    
    
    localQueryChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(restClient.getQueryDataType()));

    // Set up the Focus that provides the input source and output target for
    //   the REST operation.
    SimpleFocus<Tuple> restFocus
      =new SimpleFocus<Tuple>(parentFocus,localQueryChannel);
    if (localModelChannel!=null)
    { 
      restFocus.addFacet
        (new SimpleFocus<Buffer>(restFocus,localModelChannel)
        );
    }
    restFocus.addFacet
      (new BeanFocus<RestService>(this)
      );

    Focus<?> focus=restFocus;

    if (errorExpression!=null)
    { errorChannel=focus.bind(errorExpression);
    }
   
    bindAssignments(focus);
    restClient.bind(focus);
    return parentFocus;
  }

  protected void bindAssignments(Focus<?> focus)
    throws BindException
  { 
//    if (preAssignments!=null)
//    { preSetters=Assignment.bindArray(preAssignments, focus);
//    }
    
    if (postAssignments!=null)
    { postSetters=Assignment.bindArray(postAssignments, focus);
    }
  }  
  
  /**
   * <p>A command to execute the query. Must be called from within
   *   the thread context of this FocusChainObject
   * </p>
   * 
   * @return The Command object
   */
  public Command<?,?,?> queryCommand()
  {
    return new CommandAdapter<RestService,Void,Tuple>()
    {

      { setTarget(RestService.this);
      }
      
      @Override
      protected void run()
      { 
        try
        { setResult(getTarget().queryImpl());
        }
        catch (DataException x)
        { this.setException(x);
        }
      }
    };
  }
  
  public Delegate<Tuple> queryDelegate()
  { return delegate;
  }
  
  public Tuple query()
    throws DataException
  { 
    try
    { return runInContext(delegate);
    }
    catch (DelegateException x)
    { 
      if (x.getCause() instanceof DataException)
      { throw (DataException) x.getCause();
      }
      else
      { throw new RuntimeException(x);
      }
    }
  }
  
  private Tuple queryImpl()
    throws DataException
  {
    if (debug)
    { log.fine("Starting query");
    }
    
    int tries=0;
    boolean errorCondition=false;
    do
    {
      localQueryChannel.set
        (new EditableArrayTuple(restClient.getQueryDataType()));

      Buffer buffer=null;
      if (useDataSession)
      {
        DataSession session=sessionChannel.get();
        if (modelSourceChannel!=null)
        { 
          DataComposite modelObject=modelSourceChannel.get();
          if (modelObject==null)
          { 
            throw new DataException
              ("Model object is null, nothing to update ("
              +modelSourceChannel.getReflector().getTypeURI()
              +")"
              );
          }
          buffer=session.buffer(modelSourceChannel.get());
        }
        else
        { buffer=session.buffer(session.getData());
        }
      }
        
      if (debug)
      { log.fine("Buffered "+buffer);
      }
        
      if (localModelChannel!=null)
      { localModelChannel.set(buffer);
      }
        
      restClient.query();
         
      if (debug)
      { log.fine("RestClient.query() finished");
      }
          
      Boolean errorResult=(errorChannel!=null?errorChannel.get():null);
      if (errorResult==null || !errorResult)
      {
        if (postSetters!=null)
        { Setter.applyArray(postSetters);
        }
        if (buffer!=null)
        {
          buffer.save();
          if (debug)
          { log.fine("Saved buffer "+buffer);
          }
        }
        return localQueryChannel.get();
      }
      else
      { errorCondition=true;
      }
        
      if (errorCondition)
      { 
        try
        { Thread.sleep(errorRetryDelayMS);
        }
        catch (InterruptedException x)
        { 
          // Drop out
          tries=errorRetries-1;
        }
      }
    }
    while(errorCondition && tries++<errorRetries);
    return null;

  }

  @Override
  public void setNext(
    ContextFrame next)
  { 
    restClient.setNext(next);
  }

  @Override
  public <T> T runInContext(
    Delegate<T> delegate)
    throws DelegateException
  {
    localQueryChannel.push(null);
    if (localModelChannel!=null)
    { localModelChannel.push(null);
    }
    try
    { return restClient.runInContext(delegate);
    }
    finally
    { 
      if (localModelChannel!=null)
      { localModelChannel.pop();
      }
      
      localQueryChannel.pop();
    }
  }

}
