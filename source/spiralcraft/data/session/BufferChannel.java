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
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.lang.DataReflector;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLog;

/**
 * Returns a Buffer of a source
 * @author mike
 *
 */
public class BufferChannel<Tbuffer extends Buffer>
  extends AbstractChannel<Tbuffer>
{
  private static final ClassLog log=ClassLog.getInstance(BufferChannel.class);
  
  private Channel<? extends DataComposite> originalChannel;
  private Channel<DataSession> sessionChannel;

/*
  private Channel<Buffer> parentChannel;
*/

  @SuppressWarnings("unchecked")
  public static final BufferChannel<? extends Buffer>
    create(Type<Buffer> bufferType
    ,Channel<? extends DataComposite> originalChannel
    ,Focus<?> focus
    )
    throws BindException
  {
    return new BufferChannel
      (bufferType,originalChannel,focus);
  }
  
      
//  /**
//   * Construct a BufferChannel
//   * 
//   * @param focus A Focus on the DataComposite being buffered
//   * 
//   */
//  @SuppressWarnings("unchecked")
//  public BufferChannel(Focus<? extends DataComposite> focus)
//    throws BindException
//  { 
//    super
//      (DataReflector.<Tbuffer>getInstance
//        (Type.<DataComposite>getBufferType
//         ( ((DataReflector) focus.getSubject().getReflector())
//             .getType()
//         )
//        )
//      );
//    
//    log.fine("BufferChannel "+getReflector());
//    setupSession(focus);
//    this.originalChannel=focus.getSubject();
//    
//  }

  /**
   * Construct a BufferChannel
   * 
   * @param focus A focus, not necessarily on the originalChannel
   * 
   */
  @SuppressWarnings("unchecked")
  public BufferChannel
    (Focus<?> focus
    ,Channel<? extends DataComposite> original
    )
    throws BindException
  { 
    super
      (DataReflector.<Tbuffer>getInstance
        (Type.<DataComposite>getBufferType
         ( ((DataReflector) original.getReflector())
             .getType()
         )
        )
      );
    if (debug)
    { log.fine("BufferChannel "+getReflector());
    }
    setupSession(focus);
    this.originalChannel=original;
    
    
  }
  
  /**
   * Construct a BufferChannel
   * 
   * @param focus A focus, not necessarily on the originalChannel
   * 
   */
  public BufferChannel
    (Type<Buffer> bufferType
    ,Channel<? extends DataComposite> originalChannel
    ,Focus<?> focus
    )
    throws BindException
  { 
    // Our Focus provides access to our containing scope,
    //   not the originalChannel

    super(DataReflector.<Tbuffer>getInstance(bufferType));
    if (debug)
    { log.fine("BufferChannel "+getReflector());
    }
    this.originalChannel=originalChannel;
    setupSession(focus);

  }

  private void setupSession(Focus<?> focus)
    throws BindException
  {
    metaChannel=this;
    sessionChannel=DataSession.findChannel(focus);
    if (sessionChannel==null)
    { throw new BindException("Can't find a DataSession in Focus chain");
    }
    
    
  }
  

  @Override
  public boolean isWritable()
  { return false;
  }
  
  @Override
  public boolean store(Buffer val)
  { 
    // TODO This will be useful, to place whole buffers
    if (debug)
    { log.fine("Not storing Buffer "+val);
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Tbuffer retrieve()
  { 

    DataComposite original=originalChannel.get();
    
    if (original==null)
    { return null;
    }
    
    if (original instanceof Buffer)
    { return (Tbuffer) original;
    }
    
    try
    { 
      Tbuffer buffer=(Tbuffer) sessionChannel.get().buffer(original);
      return buffer;
    }
    catch (DataException x)
    { throw new AccessException("Error buffering "+original,x);
    }
//    Buffer parentBuffer=parentChannel!=null?parentChannel.get():null;
//    if (parentBuffer!=null)
//    { 
//      // We're at the top
//      parentBuffer.addChild(buffer);
//    }

    

  }

}