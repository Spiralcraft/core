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
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.data.lang.TypeReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Setter;
import spiralcraft.lang.SimpleFocus;
import spiralcraft.lang.spi.SimpleChannel;

/**
 * <p>Corresponds to the outermost FrameHandler of the set used to
 *   translate a document, and provides an initial data context for the
 *   document.
 * </p>
 * 
 * <p>If a target is specified, it will be made available to child
 *   elements. If no target is specified, the subject of the supplied
 *   focus will be made available as this FrameHandler's target.
 * </p>
 * 
 * <p>If a Type target is specified, it will be made available to child
 *   elements. If no target is specified, the subject of the supplied
 *   focus will be made available as this FrameHandler's target.
 * </p>
 * 
 * @author mike
 *
 */
public class RootFrame<T>
  extends AbstractFrameHandler
{  
  
  private Type<T> type;
  private Expression<T> target;
  private Channel<T> targetChannel;
  private FrameChannel<T> channel;
  /**
   * <p>Obtain the targetChannel, after binding, in order to access the
   *   root data object.
   * </p>
   * 
   * @param initialValue
   */
  public Channel<T> getTargetChannel()
  { return targetChannel;
  }
  
  /**
   * <p>The spiralcraft.data.type which corresponds to the data scoped to
   *   this RootFrameHandler.
   * </p>
   *   
   * @param type
   */
  public void setType(Type<T> type)
  { this.type=type;
  }

  /**
   * <p>The spiralcraft.data.type which corresponds to the data scoped to
   *   this RootFrameHandler.
   * </p>
   *   
   * @param type
   */
  public Type<T> getType()
  { return type;
  }
  
  /**
   * <p>An Expression which resolves the target object for this frame,
   *   if any, which will be made available through the Focus chain.
   * </p>
   * 
   * <p>This is primarily used when the FrameHandler tree reads data into
   *   part of a larger existing data model.
   * </p>
   * 
   * @param target
   */
  public void setTarget(Expression<T> target)
  { this.target=target;
  }
  

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
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
    }
    else if (parentFocus!=null)
    { targetChannel=(Channel<T>) parentFocus.getSubject();
    }
    else if (type!=null)
    {
      targetChannel
        =new SimpleChannel<T>
          (DataReflector.<T>getInstance(type));
    }
    
    if (targetChannel!=null)
    {
      channel=new FrameChannel<T>(targetChannel.getReflector());
      setFocus(new SimpleFocus<T>(parentFocus,channel));
      
      if (type!=null)
      {
        if (!(targetChannel.getReflector() instanceof TypeReflector))
        { 
          throw new BindException
            ("Target is not an instance of "+type.getURI());
        }
        else
        {
          Type<?> targetType
            =((TypeReflector) targetChannel.getReflector()).getType();
        
          if (!type.isAssignableFrom(targetType))
          {
            throw new BindException
              (targetType.getURI()+" is not an instance of "+type.getURI());
          }
        }
        
      }
    }

    super.bind();
    
  }
  
  @Override
  protected void openData()
    throws DataException
  { 
    if (targetChannel!=null)
    { 
      T target=targetChannel.get();
      if (target==null)
      { log.info("Channel for enclosing data context returned null");
      }
      channel.set(target);
    }
  }
  
  @Override
  protected void closeData()
    throws DataException
  {     

    Setter.applyArrayIfNull(defaultSetters);

    
  }
  
  @Override
  public void capturedChildObject
    (Object childObject,ForeignDataHandler.HandledFrame myFrame)
  { myFrame.setObject(childObject);
  }   
  
}
