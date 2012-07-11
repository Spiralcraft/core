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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import spiralcraft.data.DataException;
import spiralcraft.data.sax.ForeignDataHandler.HandledFrame;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.ClosureFocus;
import spiralcraft.util.string.StringPool;

/**
 * <p>Creates an Aggregate and publishes it in the Focus chain so it can receive
 *   updates. The AttributeBinding expressions reference the Aggregate by default.
 * </p>
 * 
 * @author mike
 *
 */
public class RecursiveFrame
  implements FrameHandler
{

  private FrameHandler frameRef;
  private FrameHandler parent;
  private String frameId;
  private ClosureFocus<?>.RecursionContext recursionContext;
  private boolean captureChildObject;
  
  public void setFrameId(String frameId)
  { this.frameId=frameId;
  }

  
  @Override
  public void bind()
    throws BindException
  {

    if (frameId==null)
    { throw new BindException("frameId must be specified");
    }
    
    frameRef=findFrameHandler(frameId);
    if (frameRef==null)
    { throw new BindException("frameId '"+frameId+"' not found.");
    }
    
    recursionContext=frameRef.getRecursionContext(getFocus());
    
  }

  @Override
  public void openFrame(
    HandledFrame frame)
    throws DataException
  {
    // XXX Push the context to frameRef
    recursionContext.push();
    frameRef.openFrame(frame);
    
    
  }
  
  @Override
  public void closeFrame(
    HandledFrame frame)
    throws DataException
  {
    
    
    frameRef.closeFrame(frame);
    recursionContext.pop();
    
  }

  @Override
  public FrameHandler findFrameHandler(
    String id)
  { return parent.findFrameHandler(id);
  }

  @Override
  public LinkedHashMap<String, FrameHandler> getChildMap()
  { return frameRef.getChildMap();
  }

  @Override
  public String getElementURI()
  { return frameRef.getElementURI();
  }

  @Override
  public <X> Focus<X> getFocus()
  { return parent.getFocus();
  }

  @Override
  public boolean isStrictMapping()
  { return frameRef.isStrictMapping();
  }



  @Override
  public boolean getAllowMixedContent()
  { return frameRef.getAllowMixedContent();
  }
  
  @Override
  public void setParent(
    FrameHandler parent)
  { this.parent=parent;    
  }

  @Override
  public ClosureFocus<?>.RecursionContext getRecursionContext(
    Focus<?> focusChain)
  { throw new UnsupportedOperationException
      ("RecursiveFrame can't provide a RecursionContext");
  }
  
  @Override
  public URI resolvePrefix(String name)
  { return frameRef.resolvePrefix(name);
  }
  
  @Override
  public Map<String,URI> computeMappings()
  { return frameRef.computeMappings();
  }
  
  @Override
  public void closingChild(FrameHandler child)
  { // We don't have children, so won't be called
  }
  
  @Override
  public void setStringPool(StringPool stringPool)
  {
  }
  
  /**
   * <p>Whether this frame should capture the object created by its child 
   *   frame when the child frame closes.
   * </p>
   * 
   * @param captureChildObject
   */
  @Override
  public void setCaptureChildObject(boolean captureChildObject)
  { this.captureChildObject=captureChildObject;
  }

  @Override
  public boolean getCaptureChildObject()
  { return captureChildObject;
  }  
  
  @Override
  public void capturedChildObject
    (Object childObject,ForeignDataHandler.HandledFrame myFrame)
  { myFrame.setObject(childObject);
  }  
}
