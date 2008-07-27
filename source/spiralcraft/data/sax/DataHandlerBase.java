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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.access.DataFactory;
import spiralcraft.text.ParsePosition;

/**
 * <p>Implements a SAX handler using a stack of Frames. A Frame is
 *   responsible for interpreting a portion of an XML document that
 *   contains text or one or more sibling elements.
 * </p>
 * 
 * @author mike
 *
 */
public abstract class DataHandlerBase
  extends DefaultHandler
{
  
  protected Frame initialFrame;
  private Frame currentFrame;
  private ParsePosition position;
  private Locator locator;
  protected URI resourceURI;
  protected DataConsumer<? super Tuple> dataConsumer;
  protected DataFactory<? super DataComposite> dataFactory;

  
  public void setDocumentLocator(Locator locator)
  { 
    this.locator=locator;
    position=new ParsePosition();
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
    position.setContextURI(resourceURI);
  }
  
  public String formatPosition()
  {
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
    return " ("+position+")";
  }
  

  /**
   * Specify the DataConsumer that will receive all Tuples contained within
   *   an outermost aggregate type. 
   */
  public void setDataConsumer(DataConsumer<? super Tuple> consumer)
  { this.dataConsumer=consumer;
  }
  
  public void setDataFactory(DataFactory<? super DataComposite> dataFactory)
  { this.dataFactory=dataFactory;
  }
  
  public DataFactory<? super DataComposite> getDataFactory()
  { return this.dataFactory;
  }
  
  public Object getCurrentObject()
  { return initialFrame.getObject();
  }
  
  /**
   * Optionally start the document
   */
  public void startDocument()
    throws SAXException
  { 
    try
    { initialFrame.start(null);
    } 
    catch (DataException x)
    { this.throwSAXException("Error starting root frame", x);
    }
  }

  /**
   * End the document, only if startDocument has been called
   */
  public void endDocument()
    throws SAXException
  { 
    try
    { initialFrame.finish();
    } 
    catch (DataException x)
    { this.throwSAXException("Error finalizing document", x);
    }
  }
   


  public void startElement
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  { 
    try
    { currentFrame.startElement(uri,localName,qName,attributes);
    }
    catch (DataException x)
    { 
      SAXException sx=new SAXException(x.toString()+formatPosition());
      sx.initCause(x);
      throw sx;
      
    }
    
  }

  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    try
    { currentFrame.finish(); 
    }
    catch (DataException x)
    { 
      SAXException sx=new SAXException(x.toString()+formatPosition());
      sx.initCause(x);
      throw sx;
    }
  }
  
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { currentFrame.characters(ch,start,length);
  }

  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {
  }  
  
  protected void throwSAXException(String message)
    throws SAXException
  { throw new SAXException(message+": "+formatPosition());
  }
  
  protected void throwSAXException(String message,Throwable cause)
    throws SAXException
  { 
    SAXException x=new SAXException(message+": "+formatPosition());
    x.initCause(cause);
    throw x;
  }

  /**
   * Interprets a portion of an XML document, intended to delegate to other
   *   frames.
   */
  abstract class Frame
  { 
    protected String qName;
    protected final StringBuilder chars
      =new StringBuilder();

    private Frame parentFrame;
    private boolean hasElements;
    private boolean preserveWhitespace=false;

    // Begin SAX API
    
    public final void startElement
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    { 
      if (getCharacters().length()==0)
      { 
        hasElements=true;
        newChild(uri,localName,qName,attributes).start(qName);
      }
      else
      { 
        throw new SAXException
          ("Element already contains text '"+getCharacters()+"', it cannot "
          +"also contain another Element <"+qName+">"+formatPosition()
          );
      }
      
    }
    

    public final void endElement
      (String uri
      ,String localName
      ,String qName
      )
      throws SAXException,DataException
    { 
      if (this.qName.equals(qName))
      { finish(); 
      }
      else
      { throw new SAXException("Expected </"+qName+">"+formatPosition());
      }
    }
  
    public final void characters
      (char[] ch
      ,int start
      ,int length
      )
      throws SAXException
    { 
      if (!hasElements)
      { chars.append(new String(ch,start,length));
      }
      else
      { 
        if (preserveWhitespace)
        {
          throw new SAXException
          ("Element already contains other elements."
          +" It cannot contain preserved whitespace."
          +formatPosition()
          );
        }
        
        if (new String(ch,start,length).trim().length()>0)
        {
          throw new SAXException
            ("Element '"+qName+"' already contains other elements."
            +" It cannot contain text '"+new String(ch,start,length).trim()+"'"
            +formatPosition()+" (frame="+toString()
            );
        }
      }
    }
    
    // End SAX Api
    

    // Begin override API

    /**
     * Return whatever object is represented in this frame
     */
    public abstract Object getObject();

    /**
     * <p>Must be overridden to create the new child node.
     * </p>
     * 
     * <p>The Frame returned from newChild will be pushed into the stack
     *   as the current Frame, then openFrame() will be called to perform
     *   any initialization.
     * </p>
     */
    protected abstract Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException;

    /**
     * <p>Most be overridden to handle data when a child is done
     *   processing.
     * </p>
     * 
     * <p>This is called every time a child Element is closed, after control
     *   is returned to this Frame. It allows this Frame to read data from
     *   a child Frame before the child Frame is dereferenced.
     * </p>
     */
    protected abstract void endChild(Frame child)
      throws SAXException,DataException;
    
    /**
     * <p>Can be overridden to do something after the frame is associated with
     *   the document structure and made the current frame, but before this
     *   frame starts processing sub-frames
     * </p>
     */
    protected void openFrame()
      throws DataException
    {
    }
    
    /**
     * <p>Can be overridden to do something once this frame has received all
     *   its data but before control is returned to the parent frame.
     * </p>
     */
    protected void closeFrame()
      throws SAXException,DataException
    {
    }
    
    // End override API    
    
    private final void start(String qName)
      throws DataException
    {
      parentFrame=currentFrame;
      currentFrame=this;
      this.qName=qName;
      openFrame();
    }

    private final void finish()
      throws SAXException,DataException
    { 
      closeFrame();
      currentFrame=parentFrame;
      if (parentFrame!=null)
      { parentFrame.endChild(this);
      }
    }
    
    protected String getCharacters()
    { 
      if (preserveWhitespace)
      { return chars.toString();
      }
      else
      { return chars.toString().trim();
      }
    }
  }
  
  /**
   * A frame which can contain nothing
   */
  class EmptyFrame
    extends Frame
  {

    protected void endChild(Frame child)
      throws SAXException, DataException
    { }

    public Object getObject()
    { return null;
    }

    protected Frame newChild
      (String uri
      , String localName
      , String qName
      , Attributes attributes
      ) 
      throws SAXException, DataException
    { throw new SAXException("Element '"+qName+"' not permitted here");
    }
    
  }

}
