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
package spiralcraft.data.sax;


import spiralcraft.command.Command;
import spiralcraft.command.CommandAdapter;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.sax.RestClient;
import spiralcraft.data.session.Buffer;
import spiralcraft.data.session.DataSession;
import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BeanFocus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.FocusChainObject;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

/**
 * <p>Integrates the results of a rest query into the application data model
 * </p>
 * 
 * 
 * @author mike
 */
public class RestService
  implements FocusChainObject
{
  
  private static final ClassLogger log
    =ClassLogger.getInstance(RestService.class);
  
  private Expression<DataComposite> modelExpression;
  
  private Channel<DataSession> sessionChannel;
  private Channel<DataComposite> modelSourceChannel;
  
  private ThreadLocalChannel<Tuple> localQueryChannel;
  
  private ThreadLocalChannel<Buffer> localModelChannel;
  
  private Assignment<?>[] postAssignments;
  private Setter<?>[] postSetters;
  
  private Focus<?> parentFocus;
  private CompoundFocus<Tuple> focus;
  
  private RestClient restClient;
  
  private Expression<Boolean> errorExpression;
  private Channel<Boolean> errorChannel;
  private int errorRetries;
  private int errorRetryDelayMS;
  
  private boolean debug;
  
  public void setRestClient(RestClient client)
  { restClient=client;
  }
  
  /**
   * <p>Specify an expression that resolves to the application data model
   *   object which will be buffered for update
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
  public void bind(Focus<?> parentFocus) 
    throws BindException
  {
    this.parentFocus=parentFocus;
    
    // Note: We can't expose any of the ThreadLocalChannels, because they
    //   are only populated under this objects stack, and the data won't be
    //   available  to outside users.
    
    sessionChannel
      =parentFocus.<DataSession>bind
        (Expression.<DataSession>create("[:"+DataSession.FOCUS_URI+"]"));
    
    if (modelExpression!=null)
    { 
      modelSourceChannel=parentFocus.bind(modelExpression);
      if (!(modelSourceChannel.getReflector() instanceof DataReflector))
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
    
    
    localQueryChannel
      =new ThreadLocalChannel<Tuple>
        (DataReflector.<Tuple>getInstance(restClient.getQueryDataType()));

    CompoundFocus<Tuple> restFocus
      =new CompoundFocus<Tuple>(parentFocus,localQueryChannel);
    if (localModelChannel!=null)
    { 
      restFocus.bindFocus
        ("buffer"
        ,new SimpleFocus<Buffer>(restFocus,localModelChannel)
        );
    }
    restFocus.bindFocus
      (getClass().getName()
      ,new BeanFocus<RestService>(this)
      );

    this.focus=restFocus;

    if (errorExpression!=null)
    { errorChannel=this.focus.bind(errorExpression);
    }
   
    bindAssignments();
    restClient.bind(focus);
  }

  protected void bindAssignments()
    throws BindException
  { 
//    if (preAssignments!=null)
//    { preSetters=Assignment.bindArray(preAssignments, focus);
//    }
    
    if (postAssignments!=null)
    { postSetters=Assignment.bindArray(postAssignments, focus);
    }
  }  
  
  @Override
  public Focus<?> getFocus()
  { return parentFocus;
  }
  
  public Command<?,?> queryCommand()
  {
    return new CommandAdapter<RestService,Tuple>()
    {

      { setTarget(RestService.this);
      }
      
      @Override
      protected void run()
      { 
        try
        { 
          setResult(getTarget().query());
          
        }
        catch (DataException x)
        { this.setException(x);
        }
      }
    };
  }
  
  public Tuple query()
    throws DataException
  {
    if (debug)
    { log.fine("Starting query");
    }
    
    int tries=0;
    boolean errorCondition=false;
    do
    {
      localQueryChannel.push
        (new EditableArrayTuple(restClient.getQueryDataType()));
      try
      { 
        DataSession session=sessionChannel.get();
        Buffer buffer;
        if (modelSourceChannel!=null)
        { buffer=session.buffer(modelSourceChannel.get());
        }
        else
        { buffer=session.buffer(session.getData());
        }
        
        if (debug)
        { log.fine("Buffered "+buffer);
        }
        
        if (localModelChannel!=null)
        { localModelChannel.push(buffer);
        }
        try
        {
        
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
            buffer.save();
            if (debug)
            { log.fine("Saved buffer "+buffer);
            }
            return localQueryChannel.get();
          }
          else
          { errorCondition=true;
          }
        }
        finally
        { 
          if (localModelChannel!=null)
          { localModelChannel.pop();
          }
        }
      }
      finally
      { localQueryChannel.pop();
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

}
