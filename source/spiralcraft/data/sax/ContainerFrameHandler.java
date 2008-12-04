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
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;

/**
 * <p>Maps an XML element that serves as a container for another element
 *   but does not necessarily map directly to data.
 * </p>
 * 
 * <p>If a target is specified, it will be made available to child
 *   elements. If no target is specified, the subject of the parent
 *   FrameHandler's focus will be made available as this FrameHandler's
 *   target.
 * </p>
 * 
 * <p>A ContainerFrameHandler may be used as the root of the FrameHandler
 *   tree by supplying a focus via setFocus() before bind() is called.
 * </p>
 * 
 * @author mike
 *
 */
public class ContainerFrameHandler<T>
  extends FrameHandler
{  
  
  private Expression<T> target;
  private Channel<T> targetChannel;
  private FrameChannel<T> channel;
  

  /**
   * <p>An Expression which resolves the target object for this frame,
   *   if any, which will be made available through the Focus chain.
   * </p>
   * 
   * @param target
   */
  public void setTarget(Expression<T> target)
  { this.target=target;
  }
  
  @Override
  public void bind()
    throws BindException
  {
    Focus<?> parentFocus=getFocus();
    
    if (target!=null)
    {
      if (parentFocus==null)
      { 
        throw new BindException
          ("Focus is null, cannot bind target expression for '"
           +getElementURI()+"'"
          );
      }
      targetChannel=parentFocus.bind(target);
      channel=new FrameChannel<T>(targetChannel.getReflector());
      setFocus(new SimpleFocus<T>(parentFocus,channel));
    }
    
    super.bind();
  }
  
  @Override
  protected void openData()
    throws DataException
  { 
    if (targetChannel!=null)
    { channel.set(targetChannel.get());
    }
  }
  
  @Override
  protected void closeData()
    throws DataException
  {     

    if (getFrame().getCharacters().trim().length()>0 && !isHandlingText())
    { log.fine("Unprocessed character data: "+getFrame().getCharacters());
    }
    Setter.applyArrayIfNull(defaultSetters);
    
  }
  
  
  
}
