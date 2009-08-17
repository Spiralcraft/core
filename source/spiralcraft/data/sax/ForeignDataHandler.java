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

import spiralcraft.data.DataException;

import spiralcraft.log.ClassLog;


import org.xml.sax.SAXException;
import org.xml.sax.Attributes;



import java.net.URI;

/**
 * <p>Reads SAX events into a Data graph from a resource in an arbitrary
 *   XML dialect, defined by a tree of FrameHandlers.
 * </p>
 */
public class ForeignDataHandler
  extends DataHandlerBase
{
  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(ForeignDataHandler.class);
  
  
  /**
   * Construct a new DataReader which translates the XML based on the
   *   the specified FrameHandler tree.
   */
  public ForeignDataHandler
    (FrameHandler rootHandler
    ,URI resourceURI
    )
  { 
    initialFrame=new HandledFrame(rootHandler,null);
    this.resourceURI=resourceURI;
  }
  

  class HandledFrame
    extends Frame
  {

    protected final FrameHandler frameHandler;
    protected final Attributes attributes;
    protected Object object;
    
    public HandledFrame(FrameHandler frameHandler,Attributes attributes)
    { 
      this.frameHandler=frameHandler;
      this.attributes=attributes;
      this.allowMixedContent
        =this.allowMixedContent || frameHandler.getAllowMixedContent();
    }
    
    public Attributes getAttributes()
    { return attributes;
    }
    
    public ForeignDataHandler getDataHandler()
    { return ForeignDataHandler.this;
    }
    
    @Override
    protected void endChild(
      Frame child)
      throws SAXException
    {
      
    }

    public void setObject(Object object)
    { this.object=object;
    }
    
    @Override
    public Object getObject()
    {
      // TODO Auto-generated method stub
      return object;
    }

    @Override
    protected Frame newChild(
      String uri,
      String localName,
      String name,
      Attributes attributes)
      throws SAXException,
      DataException
    {
      String fullName=AbstractFrameHandler.combineName(uri,localName);
      
      FrameHandler childFrame
        =frameHandler.getChildMap().get(fullName);
      
      
      if (childFrame!=null)
      { return new HandledFrame(childFrame,attributes);
      }
      else
      { 
        if (frameHandler.isStrictMapping())
        { 
          throwSAXException
            ("Unmapped element '"+fullName+"': uri="+uri
            +", localName="+localName
            +", qname="+qName
            );
          
          return null; // Unreachable, but required
        }
        else
        { return new IgnoredFrame();
        }
      }

    }
    
    /**
     * <p>Can be overridden to do something after the frame is associated with
     *   the document structure and made the current frame, but before this
     *   frame starts processing sub-frames
     * </p>
     */
    @Override
    protected void openFrame()
      throws DataException
    { 
      if (frameHandler!=null)
      { frameHandler.openFrame(this);
      }
    }
    

    
    /**
     * <p>Can be overridden to do something once this frame has received all
     *   its data but before control is returned to the parent frame.
     * </p>
     */
    @Override
    protected void closeFrame()
      throws SAXException
    {
      try
      {
        if (frameHandler!=null)
        { frameHandler.closeFrame(this);
        }
      }
      catch (DataException x)
      { throwSAXException(x.toString(),x);
      }      
      catch (RuntimeException x)
      { throwSAXException(x.toString(),x);
      }
    }
        
  }
  
  class IgnoredFrame
    extends Frame
  {

    { allowMixedContent=true;
    }
    
    @Override
    protected void endChild(
      Frame child)
      throws SAXException
    {
      
    }

    @Override
    public Object getObject()
    { return null;
    }

    @Override
    protected Frame newChild(
      String uri,
      String localName,
      String name,
      Attributes attributes)
      throws SAXException,
      DataException
    { return new IgnoredFrame();
    }
  }

  
  
  
}