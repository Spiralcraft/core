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

import java.net.URI;
import java.util.ArrayList;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Field;
import spiralcraft.data.Space;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataChannel;

import spiralcraft.lang.Assignment;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;

import spiralcraft.log.ClassLogger;

/**
 * <p>A Focus which provides access to a DataSession object and its
 *   optional associated data Tuple of arbitrary Type, which defines the
 *   "working set" associated with the DataSession.
 * </p>
 * 
 * <p>
 *   A source Channel provides access to the DataSession object.
 * </p>  
 * 
 * @author mike
 *
 */
public class DataSessionFocus
  extends CompoundFocus<DataSession>
{
  private static final ClassLogger log
    =ClassLogger.getInstance(DataSessionFocus.class);
  
  private static final Expression<DataComposite> DATA_EXPRESSION
    =Expression.<DataComposite>create("data");

  
  public static final DataSessionFocus 
    create(Focus<?> parentFocus,Type<? extends DataComposite> dataType)
    throws BindException
  {
    SimpleChannel<DataSession> source
      =new SimpleChannel<DataSession>
        (BeanReflector.<DataSession>getInstance(DataSession.class));
    
    return new DataSessionFocus(parentFocus,source,dataType);
  }
  
  
  private Channel<Space> spaceChannel;
  private Type<? extends DataComposite> dataType;
  private Focus<DataComposite> dataFocus;
  private ArrayList<Setter<?>> newSetters;
  private boolean debug;
 
  /**
   * <p>Create a DataSession focus which uses an externally managed
   *   dataSession
   * </p>
   * @param parentFocus The contextual Focus chain
   * @param source The channel which will hold the data session
   * @param dataType The data type of the Tuple associated with the DataSession
   * @throws BindException
   */
  @SuppressWarnings("unchecked")
  public DataSessionFocus
    (Focus<?> parentFocus
    ,Channel<DataSession> source
    ,Type<? extends DataComposite> dataType
    )
    throws BindException
  { 
    super(parentFocus,source);
    if (source==null)
    { 
      throw new BindException
        ("Source channel (Channel<DataSession>) cannot be null"
        );
    }
    
    this.dataType=dataType;
    if (parentFocus!=null)
    {
      Focus<Space> spaceFocus
        =parentFocus.<Space>findFocus(Space.SPACE_URI);
      if (spaceFocus!=null)
      { spaceChannel=spaceFocus.getSubject();
      }
    }
    
    if (dataType!=null)
    {

// To pre-buffer- no use case
//
//        SimpleFocus<Buffer> dataBufferFocus
//          =new SimpleFocus<Buffer>
//            (this
//            ,new DataChannel<DataComposite>
//              (dataType
//              ,this.bind(DATA_EXPRESSION)
//              ,false
//              ).buffer(this)
//            );
//      
//         bindFocus("spiralcraft.data.buffer",dataBufferFocus);

      // Adapt a data channel to the bean channel returned from
      //   the Data property
      dataFocus
        =new SimpleFocus<DataComposite>
          (this
          ,new DataChannel<DataComposite>
            (dataType
            ,this.bind(DATA_EXPRESSION)
            ,false
            )
          );
    
      bindFocus("spiralcraft.data",dataFocus);

      // Take care of initial field value
      for (Field field: dataType.getFieldSet().fieldIterable())
      {
      
      
        // Takes care of timestamps
        if (field.getNewExpression()!=null)
        {
          if (newSetters==null)
          { newSetters=new ArrayList<Setter<?>>();
          }
          newSetters.add
            (bindSetter(field,field.getNewExpression()));
        }
      }
    }
        
    
  }


  @SuppressWarnings("unchecked")
  private Setter<?> bindSetter(Field field,Expression expression)
    throws BindException
  {
    return new Assignment
      (Expression.create(field.getName())
      ,expression
      ).bind(dataFocus);
    
  }
  
  @Override
  public boolean isFocus(URI uri)
  { 
    boolean ret=super.isFocus(uri);
    // log.fine("isFocus="+ret+": "+uri);
    return ret;
  }
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void reset()
  {
    getSubject().set(newDataSession());
    initializeDataSession();
  }
  
  public DataSession newDataSession()
  {
    DataSession dataSession=new DataSession();
    dataSession.setType(dataType);
    if (spaceChannel!=null)
    { dataSession.setSpace(spaceChannel.get());
    }
    
    // Note: The Focus is only used to bind the Updaters for 
    //   this session
    dataSession.setFocus(this);
    
    return dataSession;
    
  }
  
  /**
   * Initializes the DataSession data object once the DataSession
   *   returned from newDataSession is made accessible through the
   *   supplied source Channel&lt;DataSession&gt;
   */
  public void initializeDataSession()
  {
    if (newSetters!=null)
    { 
      for (Setter<?> setter: newSetters)
      { 
        if (debug)
        { 
          log.fine
            ("Setting "+setter.toString()+" on "+dataFocus.getSubject().get());
        }
        if (!setter.set())
        { 
          log.fine("Assignment had no effect"
                  +"\r\n   source="+setter.getSource().get()
                  +"\r\n   target="+setter.getTarget().get()
                  );
          
        }
      }
    }
  }
  
  
}
