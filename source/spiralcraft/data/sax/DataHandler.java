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
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.FieldNotFoundException;

import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.EditableArrayListAggregate;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;

import java.net.URI;

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
  
  
  /**
   * Construct a new DataReader which expects to read the specified
   *   formal Type.
   */
  public DataHandler(Type formalType)
  { currentFrame=new InitialFrame(formalType); 
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

    // Begin SAX API
    
    public final void startElement
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    { 
      if (chars.toString().trim().length()==0)
      { 
        hasElements=true;
        newChild(uri,localName,qName,attributes).start(qName);
      }
      else
      { 
        throw new SAXException
          ("Element already contains text '"+chars.toString()+"', it cannot "
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
      { chars.append(new String(ch,start,length).trim());
      }
      else
      { 
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
      if (!formalType.isAssignableFrom(actualType))
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
     * Deal with adding an object
     */
    protected abstract void addObject(Object objet)
      throws SAXException;
      
    protected void closeFrame()
      throws SAXException,DataException
    { 
      if (chars.toString().trim().length()!=0)
      { 
        // Instantiate from a literal string, if present
        addObject(formalType.fromString(chars.toString()));
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
    
    private Object object;
    
    /**
     * Construct a detail frame to read a specific Type of data
     */
    public DetailFrame(Type type)
    { 
      this.type=type;
      scheme=type.getScheme();
      if (scheme!=null)
      { tuple=new EditableArrayTuple(scheme);
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
      if (chars.toString().trim().length()>0)
      { 
        if (type.isStringEncodable())
        { object=type.fromString(chars.toString());
        }
        else
        { 
          throw new DataException
            ("Data of type "
            +type.getURI()
            +" is not String encodable ["+chars.toString()+"]"
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
      if (fieldType.isAggregate())
      { return new AggregateFrame(fieldType);
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
    { currentField.setValue(tuple,frame.getObject());
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
      { this.object=object;
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
      if (object!=null)
      { throw new SAXException("Cannot contain more than one Object");
      }
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType,type);
      return new DetailFrame(type);
    }
    
    public void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
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
    
    private EditableArrayListAggregate<? super Object> aggregate;
    
    protected AggregateFrame(Type formalType)
    { 
      super(formalType);
      
      // XXX Must create new aggregation type
      aggregate=new EditableArrayListAggregate<Object>(formalType);
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
      ,Attributes attribs
      )
      throws SAXException,DataException
    {
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType.getContentType(),type);
      return new DetailFrame(type);
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
    
    public void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
    }
  }
  
}