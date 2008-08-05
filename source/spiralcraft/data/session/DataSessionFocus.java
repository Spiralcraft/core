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
import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLogger;

/**
 * <p>A Focus which provides access to a DataSession object and its associated
 *   data Tuple of arbitrary Type.
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

  private Channel<Space> spaceChannel;
  private Type<DataComposite> dataType;
  private Focus<DataComposite> dataFocus;
  private ArrayList<Setter<?>> newSetters;

  @SuppressWarnings("unchecked")
  public DataSessionFocus
    (Focus<?> parentFocus
    ,Channel<DataSession> source
    ,Type<DataComposite> dataType
    )
    throws BindException
  { 
    super(parentFocus
        ,source!=null
          ?source
          :new SimpleChannel<DataSession>
            (BeanReflector.<DataSession>getInstance(DataSession.class)
            ,null
            ,false
            )
         );
    
    this.dataType=dataType;
    if (parentFocus!=null)
    {
      Focus<Space> spaceFocus
        =(Focus<Space>) parentFocus.findFocus(Space.SPACE_URI);
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
  
  public DataSession newDataSession()
  {
    DataSession dataSession=new DataSession();
    dataSession.setType(dataType);
    if (spaceChannel!=null)
    { dataSession.setSpace(spaceChannel.get());
    }
    dataSession.setFocus(this);
    
    return dataSession;
    
  }
  
  public void initializeDataSession()
  {
    if (newSetters!=null)
    { 
      for (Setter<?> setter: newSetters)
      { 
        log.fine("Setting "+setter.toString()+" on "+dataFocus.getSubject().get());
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
