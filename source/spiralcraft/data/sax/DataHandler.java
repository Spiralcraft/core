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

import spiralcraft.data.TypeResolver;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.FieldNotFoundException;


import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.EditableArrayListAggregate;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import java.net.URI;
import java.io.IOException;

/**
 * Reads SAX events into a Data graph.
 */
public class DataHandler
  extends DefaultHandler
{
  private static final String STANDARD_PATH
    ="java:/spiralcraft/data/types/standard/";
  
  // private final TypeResolver resolver=TypeResolver.getTypeResolver();
  private Frame currentFrame;
  private URI resourceURI;
  private DataConsumer<? super Tuple> dataConsumer;
  
  /**
   * Construct a new DataReader which expects to read the specified
   *   formal Type.
   */
  public DataHandler(Type formalType,URI resourceURI)
  { 
    currentFrame=new InitialFrame(formalType); 
    this.resourceURI=resourceURI;
  }

  /**
   * Specify the DataConsumer that will receive all Tuples contained within
   *   an outermost aggregate type. 
   */
  public void setDataConsumer(DataConsumer<? super Tuple> consumer)
  { this.dataConsumer=consumer;
  }
  
  public Object getCurrentObject()
  { return currentFrame.getObject();
  }
  
  /**
   * Optionally start the document
   */
  public void startDocument()
    throws SAXException
  { 
  }

  /**
   * End the document, only if startDocument has been called
   */
  public void endDocument()
    throws SAXException
  { 
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
    { throw new SAXException(x.toString(),x);
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
    { throw new SAXException(x.toString(),x);
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

  protected void pushResource(URI uri)
  { 
  }

  /**
   * Holder for an element of the data tree
   */
  abstract class Frame
  { 
    protected String qName;
    protected final StringBuilder chars
      =new StringBuilder();

    protected Frame parentFrame;
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
          +"also contain another Element <"+qName+">"
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
      { throw new SAXException("Expected </"+qName+">");
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
          );
        }
        
        if (new String(ch,start,length).trim().length()>0)
        {
          throw new SAXException
            ("Element already contains other elements."
            +" It cannot contain text '"+chars.toString()+"'"
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
     * Must be overridden to create the new child node
     */
    protected abstract Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException;

    /**
     * Most be overridden to handle data when a child is done
     *   processing.
     */
    protected abstract void endChild(Frame child)
      throws SAXException,DataException;
    
    /**
     * Can be overridden to do something before this frame starts processing
     *   sub-frames
     */
    protected void openFrame()
    {
    }
    
    /**
     * Can be overridden to do something once this frame has finished processing
     *   but before control is given to the parent frame
     */
    protected void closeFrame()
      throws SAXException,DataException
    {
    }
    
    // End override API    
    
    private final void start(String qName)
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
      parentFrame.endChild(this);
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
  
  
  /**
   * Expects an Object in the form of a literal or one or more 
   *   Objects in the form of Type elements. The literal or Type elements
   *   must be compatible with the formalType.
   */
  abstract class ContainerFrame
    extends Frame
  { 
    protected final Type formalType;

    protected ContainerFrame(Type formalType)
    { this.formalType=formalType;
    }
    
    public Type getFormalType()
    { return formalType;
    }
    
    protected void assertCompatibleType(Type formalType,Type actualType)
      throws TypeMismatchException
    {
      if (formalType!=null && !formalType.isAssignableFrom(actualType))
      { 
        throw new TypeMismatchException
          ("Error reading data",formalType,actualType);
      }
    
    }
    
    protected Type resolveType(String uri,String localName)
      throws TypeNotFoundException
    {
      URI typeUri;
     
      if ( uri==null || uri.length()==0)
      { typeUri=URI.create(STANDARD_PATH.concat(localName));
      }
      else
      { 
        if (uri.endsWith("/"))
        { typeUri=URI.create(uri.concat(localName));
        }
        else
        { typeUri=URI.create(uri.concat("/").concat(localName));
        }
      }
//      System.err.println("DataHandler resolving: "+typeUri.toString());
      return TypeResolver.getTypeResolver().resolve(typeUri);
    }

    /**
     * Create a new Frame to read the specified Type
     */
    @SuppressWarnings("unchecked") // Heterogeneous collection
    protected Frame createFrame(Type type,URI ref)
      throws DataException,SAXException
    {
      Object value=null;
      if (ref!=null)
      { 
        if (!ref.isAbsolute())
        { ref=resourceURI.resolve(ref);
        }
        try
        { value=new DataReader().readFromURI(ref,type);
        }
        catch (IOException x)
        { throw new DataException("Error reading "+ref+": "+x,x);
        }
      }

      if (type.isAggregate())
      { return new AggregateFrame(type,(Aggregate) value);
      }
      else
      { return new DetailFrame(type,(Tuple) value);
      }
    }
    
    /**
     * Deal with adding an object
     */
    protected abstract void addObject(Object objet)
      throws SAXException;
      
    protected void closeFrame()
      throws SAXException,DataException
    { 
      String text=getCharacters();
      if (text.length()!=0)
      { 
//        System.err.println("DataHandler-ContainerFrame.closeFrame: "+text);
        // Instantiate from a literal string, if present
        addObject(formalType.fromString(text));
        
      }
      
    }
    
  }
  
  /**
   * Expects Object details in the form of Field elements, or a literal
   *   expressing the full value of an object in String form.
   */
  class DetailFrame
    extends Frame
  {
    private final Type type;
    private final Scheme scheme;
    private final EditableTuple tuple;
    
    private Field currentField;
    private Object prependData;

    
    private Object object;
    
    /**
     * Construct a detail frame to read a specific Type of data
     */
    public DetailFrame(Type type,Tuple initialValue)
      throws DataException
    { 
      if (initialValue!=null)
      { this.type=initialValue.getType();
      }
      else
      { this.type=type;
      }
      
      scheme=this.type.getScheme();
      if (scheme!=null)
      { 
        tuple=new EditableArrayTuple(scheme);
        if (initialValue!=null)
        { tuple.copyFrom(initialValue);
        }
      }
      else
      { tuple=null;
      }
    }
    
    public Object getObject()
    { return object;
    }
    
    protected void closeFrame()
      throws DataException
    {
      String text=getCharacters();
      if (text.length()>0)
      { 
        if (type.isStringEncodable())
        { object=type.fromString(text);
        }
        else
        { 
          throw new DataException
            ("Data of type "
            +type.getURI()
            +" is not String encodable ["+text+"]"
            );
        }
      }
      else
      { 
        if (type.isPrimitive())
        { object=type.fromData(tuple,null);
        }
        else
        { object=tuple;
        }
      }
    }
    
    /**
     * Process a Field element
     */
    protected Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    {
      URI ref=null;
      
      for (int i=0;i<attributes.getLength();i++)
      {
        if (attributes.getLocalName(i).equals("ref"))
        { ref=URI.create(attributes.getValue(i));
        }
        else
        { 
          throw new SAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"'");
        }
      }
        
      if (scheme==null)
      { 
        throw new DataException
          ("Primitive Type "+type.getURI()+" does not accept field data");
      }
      
      currentField=scheme.getFieldByName(localName);
      if (currentField==null)
      { throw new FieldNotFoundException(type,localName);
      }
      
      Type fieldType=currentField.getType();
      if (fieldType==null)
      { System.err.println("Field type is null "+currentField.toString());
      }

      if (ref!=null)
      { 
        if (!ref.isAbsolute())
        { ref=resourceURI.resolve(ref);
        }
        try
        { 
          currentField.setValue
            (tuple
            ,new DataReader().readFromURI(ref,fieldType)
            );
          
        }
        catch (IOException x)
        { throw new DataException("Error reading "+ref+": "+x,x);
        
        }
        return new EmptyFrame(); //XXX Until merge works
      }
      
      if (fieldType.isAggregate())
      { return new AggregateFrame(fieldType,null);
      }
      else
      { return new ObjectFrame(fieldType);
      }
    }
    
    /**
     * Collect the field value
     */
    protected void endChild(Frame frame)
      throws DataException
    { 
      if (!(frame instanceof EmptyFrame))
      { 
        Object childObject=frame.getObject();
        if (prependData!=null)
        {
          // Figure out how to merge the two
        }
        currentField.setValue(tuple,childObject);
      }
    }
  }
  
  /**
   * Expects a single value in the form of a literal or Type element
   */
  class ObjectFrame
    extends ContainerFrame
  { 
    
    private Object object;
    
    protected ObjectFrame(Type formalType)
    { super(formalType);
    }
    
    public Object getObject()
    { return object;
    }
    
    
    protected void addObject(Object object)
      throws SAXException
    { 
      if (this.object!=null)
      { throw new SAXException("Cannot contain more than one Object");
      }
      else
      { 
        this.object=object;
//        System.err.println("DataHandler-ObjectFrame.addObject: "+object);
      }
    }
    
    
    // We've encountered an Object
    public Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    {
      if (object!=null)
      { throw new SAXException("Cannot contain more than one Object");
      }
      
      URI ref=null;
      
      for (int i=0;i<attributes.getLength();i++)
      {
        if (attributes.getLocalName(i).equals("ref"))
        { ref=URI.create(attributes.getValue(i));
        }
        else
        { 
          throw new SAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"'");
        }
      }
      
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType,type);
      
      return createFrame(type,ref);

    }
    
    public void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
    }
  }
  
  /**
   * Expects one or more Objects in the form of Type elements, which are fed to
   *   a DataConsumer in sequence.
   * 
   * The literal or Type elements must be compatible with the formalType, which must
   *   be an Aggregate type.
   */
  class ConsumerFrame
    extends ContainerFrame
  {
    private final DataConsumer<? super Tuple> consumer;
    
    protected ConsumerFrame(Type formalType,DataConsumer<? super Tuple> consumer)
      throws DataException
    {
      super(formalType);
      this.consumer=consumer;
      consumer.dataInitialize(formalType.getContentType().getScheme());
    }
    
    /**
     * No Data object is retained by the ConsumerFrame
     */
    public Object getObject()
    { return null;
    }
    
    protected void addObject(Object object)
      throws SAXException
    {
      try
      { consumer.dataAvailable((Tuple) object);
      }
      catch (DataException x)
      { throw new SAXException("Error handling Tuple "+x,x);
      }
      
      
    }
    
    // We've encountered an Object
    public Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attribs
      )
      throws SAXException,DataException
    {
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType.getContentType(),type);
      return createFrame(type,null);
    }    
    
    public void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
    }    
    
    protected void closeFrame()
      throws SAXException,DataException
    { 
      super.closeFrame();
      consumer.dataFinalize();
    }
  }
  
  /**
   * Expects an Object in the form of a literal or one or more 
   *   Objects in the form of Type elements. The literal or Type elements
   *   must be compatible with the formalType.
   */
  class AggregateFrame
    extends ContainerFrame
  { 
    private EditableArrayListAggregate<Object> aggregate;
    
    protected AggregateFrame
      (Type formalType
      ,Aggregate<Object> initialValue
      )
    { 
      super(formalType);
      
      aggregate=new EditableArrayListAggregate<Object>(formalType);
      if (initialValue!=null)
      { aggregate.addAll(initialValue);
      }
    }
    
    public Object getObject()
    { return aggregate;
    }
    
    protected void addObject(Object object)
      throws SAXException
    { aggregate.add(object);
    }
    
    // We've encountered an Object
    public Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    {
      URI ref=null;
      
      for (int i=0;i<attributes.getLength();i++)
      {
        if (attributes.getLocalName(i).equals("ref"))
        { ref=URI.create(attributes.getValue(i));
        }
        else
        { 
          throw new SAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"'");
        }
      }
      
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType.getContentType(),type);
      return createFrame(type,ref);
    }
    
    public void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
    }
    
  }
  
  /**
   * Expects a single value in the form of a literal or Type element
   */
  class InitialFrame
    extends ObjectFrame
  {
    
    public InitialFrame(Type formalType)
    { super(formalType);
    }
    
    // We've encountered an Object
    public Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attribs
      )
      throws SAXException,DataException
    {
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType,type);
      
      if (dataConsumer!=null)
      { 
        if (type.isAggregate())
        { return new ConsumerFrame(type,dataConsumer);
        }
        else
        { 
          throw new DataException
            ("Outermost element must be an aggregate Type to use a" +
            " DataConsumer"
            );
                
        }
      }
      else
      { return createFrame(type,null);
      }

    }

  }
  
}