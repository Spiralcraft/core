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

import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLogger;

/**
 * Returns a Buffer of a source
 * @author mike
 *
 */
public class BufferChannel
  extends AbstractChannel<Buffer>
{
  private static final ClassLogger log=new ClassLogger(BufferChannel.class);
  
  private Channel<? extends DataComposite> originalChannel;
  private Channel<DataSession> sessionChannel;

/*
  private Channel<Buffer> parentChannel;
*/

  /**
   * Construct a BufferChannel
   * 
   * @param focus A Focus on the DataComposite being buffered
   * 
   */
  @SuppressWarnings("unchecked")
  public BufferChannel(Focus<? extends DataComposite> focus)
    throws BindException
  { 
    super
      (DataReflector.<Buffer>getInstance
        (Type.<DataComposite>getBufferType
         ( ((DataReflector) focus.getSubject().getReflector())
             .getType()
         )
        )
      );
    
    log.fine("BufferChannel "+getReflector());
    setupSession(focus);
    this.originalChannel=focus.getSubject();
    
  }

  /**
   * Construct a BufferChannel
   * 
   * @param focus A focus, not necessarily on the originalChannel
   * 
   */
  @SuppressWarnings("unchecked")
  public BufferChannel
    (Focus<? extends DataComposite> focus
    ,Channel<DataComposite> original
    )
    throws BindException
  { 
    super
      (DataReflector.<Buffer>getInstance
        (Type.<DataComposite>getBufferType
         ( ((DataReflector) original.getReflector())
             .getType()
         )
        )
      );
    log.fine("BufferChannel "+getReflector());
    setupSession(focus);
    this.originalChannel=focus.getSubject();
    
  }
  
  /**
   * Construct a BufferChannel
   * 
   * @param focus A focus, not necessarily on the originalChannel
   * 
   */
  @SuppressWarnings("unchecked")
  public BufferChannel
    (Type<Buffer> bufferType
    ,Channel<? extends DataComposite> originalChannel
    ,Focus<?> focus
    )
    throws BindException
  { 
    // Our Focus provides access to our containing scope,
    //   not the originalChannel

    super(DataReflector.<Buffer>getInstance(bufferType));
    log.fine("BufferChannel "+getReflector());
    this.originalChannel=originalChannel;
    setupSession(focus);

  }

  @SuppressWarnings("unchecked")
  private void setupSession(Focus<?> focus)
    throws BindException
  {
    metaChannel=this;
    
    Focus<DataSession> sessionFocus
      =(Focus<DataSession>) focus.findFocus(DataSession.FOCUS_URI);
    if (sessionFocus!=null)
    {
      sessionChannel
        =sessionFocus.getSubject();
    }
    else
    { throw new BindException("Can't find a DataSession in Focus chain");
    }
    
    
//    Focus<Buffer> bufferFocus=(Focus<Buffer>) focus.findFocus(Buffer.FOCUS_URI);
//    if (bufferFocus!=null)
//    { 
//      parentChannel
//        =bufferFocus.getSubject();
//    }
        
    
    
  }
  
  
  public boolean store(Buffer val)
  { 
    // TODO This will be useful, to place whole buffers
    return false;
  }

  public Buffer retrieve()
  { 

    DataComposite original=originalChannel.get();

    Buffer buffer=sessionChannel.get().buffer(original);
//    Buffer parentBuffer=parentChannel!=null?parentChannel.get():null;
//    if (parentBuffer!=null)
//    { 
//      // We're at the top
//      parentBuffer.addChild(buffer);
//    }

    return buffer;

  }

}