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


import spiralcraft.lang.AccessException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.log.ClassLog;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.lang.DataReflector;


public class BufferField
  extends FieldImpl<Buffer>
{
  protected final ClassLog log=ClassLog.getInstance(BufferField.class);
  
  
  @Override
  @SuppressWarnings("unchecked")
  public Channel<Buffer> bindChannel(Focus<Tuple> focus)
    throws BindException
  {
    // The Channel that provides the original field value
    Channel<DataComposite> originalChannel;
    
    if (getArchetypeField()!=null)
    { 
      // Get the correct field behavior from the archetype
      originalChannel=getArchetypeField().bindChannel(focus);
      if (debug)
      { log.fine("Creating BufferFieldChannel for field " +getURI());
      }
      		
      return new BufferFieldChannel(originalChannel,focus);
      
    }
    else
    { 
      // We're not backed by an archetype? So we got our fields how?
      // By extension?
//      originalChannel=new FieldChannel<Buffer>(focus);
      log.warning("Using Buffer with no archetype?!?");
      throw new BindException
        ("BufferField not associated with a source field");
//      return new BufferFieldChannel(originalChannel,focus);
    }

  }
  
  class BufferFieldChannel
    extends AbstractChannel<Buffer>
  {
    private Channel<? extends Buffer> bufferSource;
    private Channel<Tuple> parentChannel;
    
    public BufferFieldChannel
      (Channel<? extends DataComposite> originalChannel
      ,Focus<Tuple> parentFocus
      )
      throws BindException
    { 
      // Our Focus provides access to our containing buffer

      super(DataReflector.<Buffer>getInstance(getType()));

      metaChannel=this;
      
      parentChannel
        =parentFocus.getSubject();
          
      if (debug)
      {
        log.fine("BufferFieldChannel parentChannel="+parentChannel);
        log.fine("Creating BufferChannel of type "+getType());
      }
      this.bufferSource
        =new BufferChannel(getType(),originalChannel,parentFocus);
      
    }

    @Override
    public boolean isWritable()
    { return true;
    }
      
    @Override
    public boolean store(Buffer val)
    { 
      try
      { 
        BufferTuple parent=(BufferTuple) parentChannel.get();
        BufferField.this.setValue(parent,val);
        return true;
      }
      catch (DataException x)
      { throw new AccessException("Error storing buffer "+val,x);
      }
    }

    @Override
    public Buffer retrieve()
    { 
      BufferTuple parent=(BufferTuple) parentChannel.get();
      try
      {
        Object maybeBuffer=BufferField.this.getValue(parent);
        if (maybeBuffer!=null && !(maybeBuffer instanceof Buffer))
        { 
          log.warning("BufferField "+getURI()+" field value was a non-Buffer: "
              +"\r\n    parentChannel="+parentChannel
              +"\r\n      value="+maybeBuffer
              );
          maybeBuffer=null;
        }
        
        
        Buffer buffer=(Buffer) maybeBuffer;
        
        if (buffer!=null 
            && !buffer.isDirty() 
            && buffer.getOriginal()!=null
            )
        { 
          // Don't re-reference non-dirty buffers
          buffer=null;
          store(null);
        }
        
        if (buffer==null)
        { 
          buffer=bufferSource.get();
          
          if (buffer!=null)
          { BufferField.this.setValue((BufferTuple) parentChannel.get(),buffer);
          }
        }
        return buffer;
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving buffer",x);
      }
      
    }

  }
  

}
