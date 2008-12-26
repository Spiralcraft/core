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
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.access.DataFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.NamespaceResolver;
import spiralcraft.log.ClassLog;
import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;
import spiralcraft.util.ContextDictionary;

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
  

  private static final ClassLog log=ClassLog.getInstance(DataHandler.class);
  
  protected Frame initialFrame;
  private Frame currentFrame;
  private ParsePosition position;
  private Locator locator;
  protected URI resourceURI;
  protected DataConsumer<? super Tuple> dataConsumer;
  protected DataFactory<? super DataComposite> dataFactory;

  private ContentHandler traceHandler;
  private boolean contextAware;
  protected boolean debug;
  
  @Override
  public void setDocumentLocator(Locator locator)
  { 
    this.locator=locator;
    position=new ParsePosition();
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
    position.setContextURI(resourceURI);
  }
  
  public void setContextAware(boolean contextAware)
  { this.contextAware=contextAware;
  }
  
  public String formatPosition()
  {
    position.setLine(locator.getLineNumber());
    position.setColumn(locator.getColumnNumber());
    return " ("+position+")";
  }
  
  public void setTraceHandler(ContentHandler traceHandler)
  { this.traceHandler=traceHandler;
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
  @Override
  public void startDocument()
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.startDocument();
    }
    
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
  @Override
  public void endDocument()
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.endDocument();
    }

    try
    { initialFrame.finish();
    } 
    catch (DataException x)
    { this.throwSAXException("Error finalizing document", x);
    }
  }
   


  @Override
  public void startElement
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.startElement(uri,localName,qName,attributes);
    }

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

  @Override
  public void endElement
    (String uri
    ,String localName
    ,String qName
    )
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.endElement(uri,localName,qName);
    }

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
  
  @Override
  public void startPrefixMapping(String prefix,String uri)
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.startPrefixMapping(prefix,uri);
    }
    currentFrame.startPrefixMapping(prefix,uri);
  }

  @Override
  public void endPrefixMapping(String prefix)
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.endPrefixMapping(prefix);
    }
  }
  
  @Override
  public void characters
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  { 
    if (traceHandler!=null)
    { traceHandler.characters(ch,start,length);
    }
    
    currentFrame.characters(ch,start,length);
  }

  @Override
  public void ignorableWhitespace
    (char[] ch
    ,int start
    ,int length
    )
    throws SAXException
  {
    if (traceHandler!=null)
    { traceHandler.ignorableWhitespace(ch,start,length);
    }
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
    implements NamespaceResolver
  { 
    protected String qName;
    protected final StringBuilder chars
      =new StringBuilder();

    private Frame parentFrame;
    private boolean hasElements;
    private boolean preserveWhitespace=false;
    private HashMap<String,String> prefixMappings;

    public final void startPrefixMapping(String prefix,String uri)
    {
      if (prefixMappings==null)
      { prefixMappings=new HashMap<String,String>();
      }
      prefixMappings.put(prefix,uri);
    }
    
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
    

    /**
     * @param uri
     * @param localName
     */
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
     * 
     * @throws DataException 
     */
    protected void openFrame()
      throws DataException
    {
    }
    
    /**
     * <p>Can be overridden to do something once this frame has received all
     *   its data but before control is returned to the parent frame.
     * </p>
     * @throws SAXException 
     * @throws DataException 
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
      throws DataException
    { 
      String ret;
      if (preserveWhitespace)
      { ret=chars.toString();
      }
      else
      { ret=chars.toString().trim();
      }
      if (contextAware)
      { 
        try
        { ret=ContextDictionary.substitute(ret);
        }
        catch (ParseException x)
        { throw new DataException("Error substituting properties in "+ret,x);
        }
      }
      return ret;
    
    }
    
    @Override
    public URI getDefaultNamespaceURI()
    { 
      String ret=null;
      if (prefixMappings!=null)
      { ret=prefixMappings.get("default");
      }
      if (ret==null && parentFrame!=null)
      { return parentFrame.getDefaultNamespaceURI();
      }
      return ret!=null?URI.create(ret):null;
    }

    @Override
    public URI resolveNamespace(
      String prefix)
    {
      String ret=null;
      if (prefixMappings!=null)
      { ret=prefixMappings.get(prefix);
      }
      if (ret==null && parentFrame!=null)
      { return parentFrame.resolveNamespace(prefix);
      }
      return ret!=null?URI.create(ret):null;
    }
    
    public Object fromString(Type<?> type,String text)
      throws DataException
    {
      Object nativeObject=type.fromString(text);
    
      if (nativeObject.getClass()==Expression.class)
      { 
        if (debug)
        { log.fine("Resolving "+nativeObject.toString());
        }

        nativeObject=((Expression<?>) nativeObject).resolveNamespaces(this);
        
        if (debug)
        { log.fine("Resolved "+nativeObject.toString());
        }
      }
      return nativeObject;
    }        
  }
  
  /**
   * A frame which can contain nothing
   */
  class EmptyFrame
    extends Frame
  {

    @Override
    protected void endChild(Frame child)
      throws SAXException, DataException
    { }

    @Override
    public Object getObject()
    { return null;
    }

    @Override
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
