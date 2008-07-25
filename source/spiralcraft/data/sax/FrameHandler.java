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
package spiralcraft.data.sax;

import java.util.LinkedHashMap;
import java.util.Stack;


import org.xml.sax.Attributes;

import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLogger;

/**
 * <P>Implements a mapping from a foreign XML data element to part of a
 *   Tuple set.
 * </P>
 * 
 * @author mike
 *
 */
public class FrameHandler
{
  protected static final ClassLogger log
    =ClassLogger.getInstance(FrameHandler.class);

  private String elementURI;
  private LinkedHashMap<String,FrameHandler> childMap
    =new LinkedHashMap<String,FrameHandler>();
  
  private boolean strictMapping;
  
  private Stack<ForeignDataHandler.HandledFrame> stack
    =new Stack<ForeignDataHandler.HandledFrame>();
  
  protected boolean debug;
  
  
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public String getElementURI()
  { return elementURI;
  }
  
  public void setElementURI(String elementURI)
  { this.elementURI = elementURI;
  }
  
  /**
   * <p>Recursively bind queries and expressions to the application context.
   * </p>
   * 
   * @param focus
   */
  public void bind(Focus<?> focus)
  {
  }
  
  /**
   * <p>When set to true, the strictMapping property indicates that an
   *   encounter of an unmapped
   *   element or attribute in the incoming data stream will cause an exception
   *   to be thrown, terminating processing. 
   * </p>
   * 
   * <p>When set to false (the default value), unmapped elements and attributes
   *   will simply be ignored
   * </p>
   * 
   * @return strictMapping 
   */
  public boolean isStrictMapping()
  { return strictMapping;
  }
  
  public void setStrictMapping(boolean strictMapping)
  { this.strictMapping=strictMapping;
  }
  
  
  public void setChildren(FrameHandler[] children)
  { 
    childMap.clear();
    for (FrameHandler map:children)
    { childMap.put(map.getElementURI(), map);
    }
  }

  public LinkedHashMap<String,FrameHandler> getChildMap()
  { return childMap;
  }
  
  public void openFrame(ForeignDataHandler.HandledFrame frame)
  { 
    stack.push(frame);
    if (debug)
    { log.fine("URI="+elementURI);
    }
  }

  public void closeFrame(ForeignDataHandler.HandledFrame frame)
  { 
    if (debug)
    { log.fine("URI="+elementURI);
    }
    if (stack.pop()!=frame)
    { 
      throw new IllegalStateException
        ("Internal Error: DataHandler and FrameHandler stack out of sync");
    }
  }

}
