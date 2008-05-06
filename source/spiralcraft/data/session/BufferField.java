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
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Key;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.core.KeyField;
import spiralcraft.data.lang.DataReflector;


public class BufferField
  extends FieldImpl
{
  protected final ClassLogger log=ClassLogger.getInstance(BufferField.class);
  
  @SuppressWarnings("unchecked")
  public Type<Buffer> getType()
  { return (Type<Buffer>) super.getType();
  }
  
  
  @SuppressWarnings("unchecked")
  public Channel<?> bind(Focus<? extends Tuple> focus)
    throws BindException
  {
    // The Channel that provides the original field value
    Channel<DataComposite> originalChannel;
    
    if (getArchetypeField()!=null)
    { 
      // Get the correct field behavior from the archetype
      originalChannel=(Channel<DataComposite>) getArchetypeField().bind(focus);
      if (debug)
      { log.fine("Creating BufferFieldChannel for field " +getURI());
      }
      		
      return new BufferFieldChannel(originalChannel,(Focus<BufferTuple>) focus);
      
    }
    else
    { 
      // We're not backed by an archetype? So we got our fields how?
      // By extension?
      originalChannel=new FieldChannel(focus.getSubject());
      log.warning("Using Buffer with no archetype?!?");
      return new BufferFieldChannel(originalChannel,(Focus<BufferTuple>) focus);
    }

  }
  
  class BufferFieldChannel
    extends AbstractChannel<Buffer>
  {
    private Channel<? extends Buffer> bufferSource;
    private Channel<BufferTuple> parentChannel;
    
    @SuppressWarnings("unchecked")
    public BufferFieldChannel
      (Channel<? extends DataComposite> originalChannel
      ,Focus<BufferTuple> parentFocus
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
      
    public boolean store(Buffer val)
    { 
      try
      { 
        if (val!=null && !(val instanceof Buffer))
        { 
          throw new AccessException
            ("BufferField "+getURI()+" cannot be assigned a non-Buffer: "
              +"\r\n      value="+val
              );
             
        }
        BufferTuple parent=parentChannel.get();
        BufferField.this.setValue(parent,val);
        return true;
      }
      catch (DataException x)
      { throw new AccessException("Error storing buffer "+val,x);
      }
    }

    public Buffer retrieve()
    { 
      BufferTuple parent=parentChannel.get();
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
          
          if (buffer!=null && !(buffer instanceof Buffer))
          {
            throw new AccessException
              ("BufferField "+getURI()+" bufferSource returned a non-Buffer: "
              +"\r\n    bufferSource="+bufferSource
              +"\r\n      value="+maybeBuffer
              );
          }
          
          if (buffer!=null)
          { BufferField.this.setValue(parentChannel.get(),buffer);
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
