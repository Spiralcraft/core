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
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import spiralcraft.common.namespace.NamespaceContext;
import spiralcraft.common.namespace.PrefixResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.DataFactory;
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
  private boolean allowMixedContentDefault;
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
    { throw newSAXException("Error starting root frame", x);
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
    { throw newSAXException("Error finalizing document", x);
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
    { throw new DataSAXException(x.toString()+formatPosition(),x);
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
      throw new DataSAXException(x.getMessage()+formatPosition(),x);
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
    
//    log.fine("CHARACTERS ("+start+","+length+") ["
//            +new String(ch,start,length)+"]"
//            );

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
  
  protected SAXException newSAXException(String message)
  { return new DataSAXException(message+": "+formatPosition());
  }
  
  protected SAXException newSAXException(String message,Exception cause)
  { return new DataSAXException(message+": "+formatPosition(),cause);
  }

  /**
   * Interprets a portion of an XML document, intended to delegate to other
   *   frames.
   */
  abstract class Frame
    implements PrefixResolver
  { 
    protected String qName;
    protected final StringBuilder chars
      =new StringBuilder();

    private Frame parentFrame;
    private boolean hasElements;
    private boolean preserveWhitespace=false;
    private HashMap<String,String> prefixMappings;
    
    protected boolean contextAwareFrame
      =currentFrame!=null?currentFrame.contextAwareFrame:contextAware;
    
    protected boolean allowMixedContent
      =currentFrame!=null?currentFrame.allowMixedContent:allowMixedContentDefault;

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
      if (allowMixedContent || getCharacters().length()==0)
      { 
        hasElements=true;
        newChild(uri,localName,qName,attributes).start(qName);
      }
      else 
      { 
        throw new DataSAXException
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
      { throw new DataSAXException("Expected </"+qName+">"+formatPosition());
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
      { chars.append(ch,start,length);
      }
      else
      { 
        if (!allowMixedContent && preserveWhitespace)
        {
          throw new DataSAXException
          ("Element already contains other elements."
          +" It cannot contain preserved whitespace."
          +formatPosition()
          );
        }
        
        if (!allowMixedContent && new String(ch,start,length).trim().length()>0)
        {
          throw new DataSAXException
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

    protected boolean handleStandardAttribute(Attributes attributes,int index)
    {
      if (attributes.getQName(index).equals("contextAware"))
      { 
        contextAwareFrame=Boolean.parseBoolean(attributes.getValue(index));
        return true;
      }
      return false;
    }
    
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
      throws SAXException;
    
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
      if (contextAwareFrame)
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
    public URI resolvePrefix(
      String prefix)
    {
      String ret=null;
      if (prefixMappings!=null)
      { ret=prefixMappings.get(prefix);
      }
      if (ret==null && parentFrame!=null)
      { return parentFrame.resolvePrefix(prefix);
      }
      return ret!=null?URI.create(ret):null;
    }
    
    @Override
    public Map<String,URI> computeMappings()
    { 
      Map<String,URI> computedMappings=new HashMap<String,URI>();

      Map<String,URI> parentMappings
        =parentFrame!=null?parentFrame.computeMappings():null;
      if (parentMappings!=null)
      { computedMappings.putAll(parentMappings);
      }
      
      if (prefixMappings!=null)
      { 
        for (String key:prefixMappings.keySet())
        { computedMappings.put(key,URI.create(prefixMappings.get(key)));
        }
      }
      return computedMappings;
    }    
    
    /**
     * Resolve a URI reference that may include an optional namespace
     *   prefix.
     * 
     * @param ref
     * @return
     */
    public URI resolveRef(String ref)
    {
      if (ref.startsWith(":"))
      { return URI.create(ref.substring(1));
      }
      else
      {
        int colonPos=ref.indexOf(':');
        if (colonPos>0)
        { 
          URI base=resolvePrefix(ref.substring(0,colonPos));
          if (base==null)
          {
            log.warning
              ("Namespace prefix '"+ref.substring(0,colonPos)+"' not found "
              +"at "+formatPosition()+". Falling back to resolving absolute "
              +" URI "+ref+". If an absolute URI is what you mean, please"
              +" use the ':' prefix- ie. ref=\":"+ref+"\""
              );
              	
            return URI.create(ref);
          }
          else
          { 
            String baseStr=base.toString();
            if (!baseStr.endsWith("/"))
            { 
              baseStr=baseStr+"/";
              base=URI.create(baseStr);
            }
            return base.resolve(URI.create(ref.substring(colonPos+1)));
          }
        }
        else
        { return URI.create(ref);
        }
      }
    }
    
    
    public Object fromString(Type<?> type,String text)
      throws DataException,SAXException
    {
      type.link();
      NamespaceContext.push(this);
      try
      { 
        
        return type.fromString(text);
      }
      catch (RuntimeException x)
      { 
        throw newSAXException
          ("Error translating text content",x);
      }
      finally
      { NamespaceContext.pop();
      }
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
      throws SAXException
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
    { 
      throw new DataSAXException
        ("Element '"+qName+"' not permitted here"+formatPosition());
    }


  }

}
