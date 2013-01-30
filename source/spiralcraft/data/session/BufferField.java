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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.log.ClassLog;
import spiralcraft.util.tree.LinkedTree;

import spiralcraft.common.ContextualException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Field;
import spiralcraft.data.Tuple;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.core.RelativeField;
import spiralcraft.data.lang.DataReflector;

public class BufferField
  extends FieldImpl<Buffer>
{
  protected final ClassLog log=ClassLog.getInstance(BufferField.class);
  protected boolean child;
  
  
  @Override
  @SuppressWarnings("unchecked")
  public Channel<Buffer> bindChannel
    (Channel<Tuple> source,Focus<?> focus,Expression<?>[] args)
    throws BindException
  {
    // The Channel that provides the original field value
    Channel<DataComposite> originalChannel;
    
    Field<DataComposite> afield=getArchetypeField();
    if (afield!=null)
    { 
      // Get the correct field behavior from the archetype
      originalChannel=afield.bindChannel(source,focus,args);
      if (debug)
      { log.fine("Creating BufferFieldChannel for field " +getURI());
      }
      		
      if (afield instanceof RelativeField)
      { child=((RelativeField<?>) afield).isChild();
      }
      return new BufferFieldChannel(originalChannel,source,focus);
      
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
    extends SourcedChannel<Tuple,Buffer>
  {
    private Channel<? extends Buffer> bufferSource;
    private Channel<? extends DataComposite> originalChannel;
    
    public BufferFieldChannel
      (Channel<? extends DataComposite> originalChannel
      ,Channel<Tuple> source
      ,Focus<?> parentFocus
      )
      throws BindException
    { 
      // Our Focus provides access to our containing buffer

      super(DataReflector.<Buffer>getInstance(getType()),source);
      this.originalChannel=originalChannel;

          
      if (debug)
      {
        log.fine("BufferFieldChannel parentChannel="+source);
        log.fine("Creating BufferChannel of type "+getType());
      }
      this.bufferSource
        =BufferChannel.create(getType(),originalChannel,parentFocus);
      
    }

    @Override
    public boolean isWritable()
    { return true;
    }
      
    @Override
    public boolean store(Buffer val)
    { 
      if (val!=null && !(val instanceof Buffer))
      { throw new AccessException("Not a buffer: "+val);
      }
      
      try
      { 
        BufferTuple parent=(BufferTuple) source.get();
        
        BufferField.this.setValue(parent,val);
        return true;
      }
      catch (DataException x)
      { throw new AccessException("Error storing buffer "+val,x);
      }
    }

    
    @Override
    public synchronized <X> Channel<X> resolveMeta(Focus<?> focus,URI typeURI)
      throws BindException
    {       
      if (debug)
      { log.debug("Checking for metadata type "+typeURI.toString());
      }
      try
      {
        Channel<X> meta=BufferField.this.resolveMeta(typeURI);
        if (meta==null)
        { meta=super.resolveMeta(focus,typeURI);
        }
        if (meta==null)
        { meta=originalChannel.resolveMeta(focus,typeURI);
        }
        return meta;
      }
      catch (ContextualException x)
      { throw new BindException("Error resolving metadata "+typeURI,x);
      }

    }
    
    @Override
    public Buffer retrieve()
    { 
      BufferTuple parent=(BufferTuple) source.get();
      try
      {
        
        Object maybeBuffer=null;
        if (parent!=null)
        {

          BufferTuple parentExtent=(BufferTuple) widenTuple(parent);
          maybeBuffer=parentExtent.getBuffer(getIndex());
        }
          
        if (maybeBuffer!=null && !(maybeBuffer instanceof Buffer))
        { 
          log.warning("BufferField "+getURI()+" field value was a non-Buffer: "
              +"\r\n    parentChannel="+source
              +"\r\n      value="+maybeBuffer
              );
          maybeBuffer=null;
        }
        
        
        Buffer buffer=(Buffer) maybeBuffer;
        
        if (buffer!=null)
        { 
          if (child)
          {
            if (parent!=buffer.getParent())
            {
              if (debug)
              { log.fine("Not reusing buffer b/c parent changed "+buffer);
              }
              buffer=null;
              store(null);
            }
          }
          else
          {
            if (!buffer.isDirty() 
               && buffer.getOriginal()!=null
               )
            {
              // Don't re-reference non-dirty buffers of existing data
              buffer=null;
              store(null);
            }
          }
        }
        
        if (buffer==null)
        { 
          buffer=bufferSource.get();
          if (debug)
          { log.fine("Created new buffer "+buffer);
          }
          
          if (buffer!=null && parent!=null)
          { 
            BufferField.this.setValue(parent,buffer);
            if (child)
            { buffer.setParent(parent);
            }
          }
        }
        return buffer;
      }
      catch (DataException x)
      { throw new AccessException("Error retrieving buffer",x);
      }
      
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
    { return new LinkedTree<Channel<?>>(this,originalChannel.trace(stop));
    }

  }
  

}
