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


import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.log.ClassLogger;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Key;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;

import spiralcraft.data.core.FieldImpl;
import spiralcraft.data.core.KeyField;
import spiralcraft.data.lang.DataReflector;


public class BufferField
  extends FieldImpl
{
  protected final ClassLogger log=new ClassLogger(BufferField.class);
  
  @SuppressWarnings("unchecked")
  public Type<Buffer> getType()
  { return (Type<Buffer>) super.getType();
  }
  
  @Override
  public void subclassResolve()
  {
  }
  
  @SuppressWarnings("unchecked")
  public Channel<?> bind(Focus<? extends Tuple> focus)
    throws BindException
  {
    Channel<DataComposite> originalChannel;
    
    if (getArchetypeField()!=null)
    { 
      // Get the correct field behavior from the archetype
      originalChannel=(Channel<DataComposite>) getArchetypeField().bind(focus);
      
      return new BufferFieldChannel(originalChannel,focus);
      
    }
    else
    { 
      // We're not backed by an archetype? So we got our fields how?
      // By extension?
      originalChannel=new FieldChannel(focus.getSubject());
      log.fine("Using Buffer with no archetype");
      return new BufferFieldChannel(originalChannel,focus);
    }

  }
  
  class BufferFieldChannel
    extends AbstractChannel<Buffer>
  {
    private Channel<? extends Buffer> bufferSource;
    private Channel<DataSession> sessionChannel;
    private ThreadLocalChannel<Buffer> bufferPinned;
    private Focus<Buffer> pinnedFocus;
    
    private Setter<Tuple> inheritedValues;
    
    

    
  /*
    private Channel<Buffer> parentChannel;
  */
    
    @SuppressWarnings("unchecked")
    public BufferFieldChannel
      (Channel<? extends DataComposite> originalChannel
      ,Focus<? extends Tuple> focus
      )
      throws BindException
    { 
      // Our Focus provides access to our containing buffer

      super(DataReflector.<Buffer>getInstance(getType()));

      metaChannel=this;
      
      sessionChannel
        =(Channel<DataSession>) focus.findFocus(DataSession.FOCUS_URI)
          .getSubject();
      
//      Focus<Buffer> bufferFocus=(Focus<Buffer>) focus.findFocus(Buffer.FOCUS_URI);
//      if (bufferFocus!=null)
//      { 
//        parentChannel
//          =bufferFocus.getSubject();
//      }
          
      
      this.bufferSource=new BufferChannel(getType(),originalChannel,focus);
      this.bufferPinned
        =new ThreadLocalChannel(DataReflector.getInstance(getType()));

      this.pinnedFocus=new SimpleFocus(focus,bufferPinned);
      
      if (!getType().isAggregate())
      {

        if (getArchetypeField() instanceof KeyField)
        { 
          Focus pinnedTupleFocus=pinnedFocus;
          
          Key key=((KeyField) getArchetypeField()).getKey();

          inheritedValues
            =new Setter<Tuple>
              (key.bind(focus)
              ,key.getImportedKey().bind(pinnedTupleFocus)
              );
        }
      }

    }

    @Override
    public boolean isWritable()
    { return false;
    }
      
    public boolean store(Buffer val)
    { 
      // TODO This will be useful, to place whole buffers, especially
      //   when updating relationships by object references instead of keys
      return false;
    }

    public Buffer retrieve()
    { 
      
      Buffer buffer=bufferSource.get();
      bufferPinned.push(buffer);
      try
      { 
        // Do a bunch of stuff to buffer depending on state

        // XXX Check buffer state (don't do this every time)
        inheritedValues.set();
      }
      finally
      { bufferPinned.pop();
      }
      
      return buffer;

    }

  }
  

}
