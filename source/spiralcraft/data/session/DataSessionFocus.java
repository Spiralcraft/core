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

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataChannel;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CompoundFocus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLogger;

/**
 * A Focus which provides access to a DataSession object and its associated
 *   data buffer
 *   
 * 
 * @author mike
 *
 */
public class DataSessionFocus
  extends CompoundFocus<DataSession>
{
  @SuppressWarnings("unused")
  private static final ClassLogger log
    =new ClassLogger(DataSessionFocus.class);
  
  private static final Expression<DataComposite> DATA_EXPRESSION
    =Expression.<DataComposite>create("data");

  
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
    
    try
    {
      if (dataType!=null)
      {
        SimpleFocus<Buffer> dataBufferFocus
          =new SimpleFocus<Buffer>
            (this
            ,new DataChannel<DataComposite>
              (dataType
              ,this.bind(DATA_EXPRESSION)
              ,false
              ).buffer(this)
            );
      
        bindFocus("spiralcraft.data.buffer",dataBufferFocus);

        SimpleFocus<DataComposite> dataFocus
          =new SimpleFocus<DataComposite>
            (this
            ,new DataChannel<DataComposite>
              (dataType
              ,this.bind(DATA_EXPRESSION)
              ,false
              )
            );
    
      bindFocus("spiralcraft.data",dataFocus);
      // log.fine(dataFocus.toString());

      }
    }
    catch (DataException x)
    { throw new BindException("Error creating DataSessionFocus",x);
    }
    
    
    
  }
  
  public boolean isFocus(URI uri)
  { 
    boolean ret=super.isFocus(uri);
    // log.fine("isFocus="+ret+": "+uri);
    return ret;
  }
  
}
