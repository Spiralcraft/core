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

import spiralcraft.data.DataException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;

/**
 * <p>A frame which provides direct access to the XML element
 * </p>
 * 
 * 
 * @author mike
 *
 */
public class XmlFrame<T>
  extends AbstractFrameHandler
{

  private Binding<T> peer;
  private Binding<?> afterOpen;
  private Binding<?> preClose;
  private FrameChannel<T> channel;
  
    
  public void setPeer(Binding<T> peer)
  { this.peer=peer;
  }
  
  public void setAfterOpen(Binding<?> afterOpen)
  { this.afterOpen=afterOpen;
  }
  
  public void setPreClose(Binding<?> preClose)
  { this.preClose=preClose;
  }
  
  @Override
  public void bind()
    throws BindException
  {
    Focus<?> parentFocus=getFocus();


    if (peer!=null)
    {
      peer.bind(parentFocus);
      channel=new FrameChannel<T>(peer.getReflector());
      setFocus(parentFocus.chain(channel));
    }
    if (afterOpen!=null)
    { afterOpen.bind(getFocus());
    }
    if (preClose!=null)
    { preClose.bind(getFocus());
    }
    super.bind();
  }
  
  @Override
  protected void openData()
    throws DataException
  {
    if (peer!=null)
    { channel.set(peer.get());
    }
    if (afterOpen!=null)
    { afterOpen.get();
    }

  }
  
  @Override
  protected void closeData()
    throws DataException
  {     

    Setter.applyArrayIfNull(defaultSetters);

    if (preClose!=null)
    { preClose.get();
    }
    if (debug)
    { 
      Object val=channel.get();
      log.fine("elementURI="+getElementURI()+": "+(val!=null?val:""));
    }

  }
  
  
  
}
