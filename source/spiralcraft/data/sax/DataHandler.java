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

import spiralcraft.data.DataConsumer;
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


import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.log.ClassLog;


import org.xml.sax.SAXException;
import org.xml.sax.Attributes;



import java.net.URI;
import java.io.IOException;

/**
 * Reads SAX events into a Data graph from a resource in the 
 *   spiralcraft.data XML dialect.
 */
@SuppressWarnings("unchecked") // Mostly runtime type resolution
public class DataHandler
  extends DataHandlerBase
{
  private static final ClassLog log=ClassLog.getInstance(DataHandler.class);
  private static final String STANDARD_PATH
    ="class:/spiralcraft/data/types/standard/";
  
  /**
   * Construct a new DataReader which expects to read the specified
   *   formal Type.
   */
  public DataHandler(Type<?> formalType,URI resourceURI)
  { 

    
    initialFrame=new InitialFrame(formalType); 
    
    this.resourceURI=resourceURI;
  }
  


  //protected void pushResource(URI uri)
  //{ 
  //}


  
  
  /**
   * Expects an Object in the form of a literal or one or more 
   *   Objects in the form of Type elements. The literal or Type elements
   *   must be compatible with the formalType.
   */
  abstract class ContainerFrame
    extends Frame
  { 
    protected final Type formalType;

    protected ContainerFrame(Type<?> formalType)
    { this.formalType=formalType;
    }
    
    public Type<?> getFormalType()
    { return formalType;
    }
    
    protected void assertCompatibleType(Type<?> formalType,Type<?> actualType)
      throws TypeMismatchException
    {
      if (formalType!=null && !formalType.isAssignableFrom(actualType))
      { 
        log.fine
          (formalType.toString()
          +"\r\n  not assignable from \r\n"
          +actualType.toString()
          );
        throw new TypeMismatchException
          ("Error reading data "+formatPosition(),formalType,actualType);
      }
    
    }
    
    protected Type<?> resolveType(String uri,String localName)
      throws DataException
    {
      URI typeUri;
     
      if ( uri==null || uri.length()==0)
      { 
        
        // Default to standard types path
        typeUri=URI.create(STANDARD_PATH.concat(localName));
        return Type.resolve(typeUri);
      }
      else if (uri.equals(".") && resourceURI!=null)
      { return Type.resolve(resourceURI.resolve(localName));
      }
      else
      { 
        if (uri.endsWith("/"))
        { typeUri=URI.create(uri.concat(localName));
        }
        else
        { typeUri=URI.create(uri.concat("/").concat(localName));
        }
        if (!typeUri.isAbsolute() && resourceURI!=null)
        { 
//        System.err.println
//          ("DataHandler resolving relative to : "
//          +resourceURI.toString()+" : "+typeUri.toString()
//          );
          
          typeUri=resourceURI.resolve(typeUri);
        }
//      System.err.println("DataHandler resolving: "+typeUri.toString());
        try
        { return Type.resolve(typeUri);
        }
        catch (TypeNotFoundException x)
        { 
          throw new DataException
            (x.getMessage()+formatPosition()
            ,x
            );
        }
        
      }
    }

    /**
     * Create a new Frame to read the specified Type
     */
//    @SuppressWarnings("unchecked") // Heterogeneous collection
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
        { 
          DataReader dataReader=new DataReader();
          dataReader.setDataFactory(dataFactory);
          value=dataReader.readFromURI(ref,type);
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
    protected abstract void addObject(Object object)
      throws SAXException;
      
    /**
     * Add the set of objects composed from the character content
     * 
     * @param object
     * @throws SAXException
     */
    protected abstract void addAggregateFromChars(Object object)
      throws SAXException;
    
    @Override
    protected void closeFrame()
      throws SAXException,DataException
    { 
      String text=getCharacters();
      if (text.length()!=0)
      { 
        if (formalType.isStringEncodable())
        {
//        System.err.println("DataHandler-ContainerFrame.closeFrame: "+text);
        // Instantiate from a literal string, if present
          if (formalType.isAggregate())
          {
            if (formalType.isPrimitive())
            { addAggregateFromChars(fromString(formalType,text));
            }
            else
            { addAggregateFromChars
                (formalType.toData(fromString(formalType,text)));
            }
          }
          else
          {
            if (formalType.isPrimitive())
            { addObject(fromString(formalType,text));
            }
            else
            { addObject(formalType.toData(fromString(formalType,text)));
            }
          }
        }
        else
        { throw new DataException("Type is not String encodable: "+formalType);
        }
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
    protected DetailFrame(Type type,Tuple initialValue)
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
        if (dataFactory==null)
        { tuple=new EditableArrayTuple(scheme);
        }
        else
        { tuple=(EditableTuple) dataFactory.create(this.type);
        }
        if (initialValue!=null)
        { tuple.copyFrom(initialValue);
        }
      }
      else
      { tuple=null;
      }
    }
    
    @Override
    public Object getObject()
    { return object;
    }
    

    
    @Override
    protected void closeFrame()
      throws DataException
    {
      String text=getCharacters();
      if (text.length()>0)
      { 
        if (type.isStringEncodable())
        { 
          // XXX Never return a native object for a non-primitive Type.
          Object nativeObject=fromString(type,text);
                    
          if (!type.isPrimitive())
          { object=type.toData(nativeObject);
          }
          else
          { object=nativeObject;
          }
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
    @Override
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
        if (handleStandardAttribute(attributes,i))
        {
        }
        else if (attributes.getLocalName(i).equals("ref"))
        { ref=resolveRef(attributes.getValue(i));
        }
        else
        { 
          throw new DataSAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"':"
            +formatPosition()
            );
        }
      }
        
      if (scheme==null)
      { 
        throw new DataSAXException
          ("Primitive Type "+type+" does not accept field data:"
           +formatPosition()
          );
      }
      
      currentField=type.getField(localName);
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
          DataReader dataReader=new DataReader();
          dataReader.setDataFactory(dataFactory);
          currentField.setValue
            (tuple
            ,dataReader.readFromURI(ref,fieldType)
            );
          
        }
        catch (IOException x)
        { throw new DataException("Error reading "+ref+": "+x,x);
        
        }
        return new EmptyFrame(); //XXX Until merge works
      }

      // 2009-07-29 mike Pass in current field value instead of null, so
      //   data is additive to an aggregate
      if (fieldType.isAggregate())
      { return new AggregateFrame
          (fieldType,(Aggregate) currentField.getValue(tuple));
      }
      else
      { return new ObjectFrame(fieldType);
      }
    }
    
    /**
     * Collect the field value
     */
    @Override
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
    
    @Override
    public Object getObject()
    { return object;
    }
    
    @Override
    protected void addAggregateFromChars(Object object)
      throws SAXException
    { addObject(object);
    }
    
    @Override
    protected void addObject(Object object)
      throws SAXException
    { 
      if (this.object!=null)
      { throwSAXException("Cannot contain more than one Object");
      }
      else
      { 
        this.object=object;
//        System.err.println("DataHandler-ObjectFrame.addObject: "+object);
      }
    }
    
    
    // We've encountered an Object
    @Override
    protected Frame newChild
      (String uri
      ,String localName
      ,String qName
      ,Attributes attributes
      )
      throws SAXException,DataException
    {
      if (object!=null)
      { 
        throwSAXException
          ("Type "+formalType.getURI()+" cannot contain more than one Object");
      }
      
      URI ref=null;
      
      for (int i=0;i<attributes.getLength();i++)
      {
        if (handleStandardAttribute(attributes,i))
        {
        }
        else if (attributes.getLocalName(i).equals("ref"))
        { ref=resolveRef(attributes.getValue(i));
        }
        else
        { 
          throw new DataSAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"': "
            +formatPosition()
            );
        }
      }
      
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType,type);
      
      return createFrame(type,ref);

    }
    
    @Override
    protected void endChild(Frame child)
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
    
    protected ConsumerFrame
      (Type formalType
      ,DataConsumer<? super Tuple> consumer
      )
      throws DataException
    {
      super(formalType);
      this.consumer=consumer;
      consumer.dataInitialize(formalType.getContentType().getScheme());
    }
    
    /**
     * No Data object is retained by the ConsumerFrame
     */
    @Override
    public Object getObject()
    { return null;
    }
    
    @Override
    protected void addAggregateFromChars(Object object)
      throws SAXException
    {
      for (Tuple tuple : (Aggregate<Tuple>) object)
      { addObject(tuple);
      }
    }
    
    @Override
    protected void addObject(Object object)
      throws SAXException
    {
      try
      { consumer.dataAvailable((Tuple) object);
      }
      catch (DataException x)
      { 
        throw new DataSAXException
          ("Error handling Tuple: "+formatPosition(),x);
      }
      
      
    }
    
    // We've encountered an Object
    @Override
    protected Frame newChild
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
    
    @Override
    protected void endChild(Frame child)
      throws SAXException
    { addObject(child.getObject());
    }    
    
    @Override
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
      throws DataException
    { 
      super(formalType);
      
      if (dataFactory!=null)
      { aggregate=(EditableArrayListAggregate) dataFactory.create(formalType);
      }
      else
      { aggregate=new EditableArrayListAggregate<Object>(formalType);
      }
      
      if (initialValue!=null)
      { aggregate.addAll(initialValue);
      }
    }
    
    
    @Override
    public Object getObject()
    { return aggregate;
    }

    @Override
    protected void addAggregateFromChars(Object object)
      throws SAXException
    {
      if (formalType.isPrimitive())
      { 
        throw new UnsupportedOperationException
          ("Primitive aggregates not supported");

      }
      else
      {
        for (Object val : (Aggregate) object)
        { addObject(val);
        }
      }
    }
    
    @Override
    protected void addObject(Object object)
      throws SAXException
    { aggregate.add(object);
    }
    
    // We've encountered an Object
    @Override
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
        if (handleStandardAttribute(attributes,i))
        {
        }
        else if (attributes.getLocalName(i).equals("ref"))
        { ref=resolveRef(attributes.getValue(i));
        }
        else
        { 
          throw new DataSAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"': "
            +formatPosition()
            );
        }
      }
      
      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType.getContentType(),type);
      return createFrame(type,ref);
    }
    
    @Override
    protected void endChild(Frame child)
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
    @Override
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
        if (handleStandardAttribute(attributes,i))
        {
        }
        else if (attributes.getLocalName(i).equals("ref"))
        { ref=resolveRef(attributes.getValue(i));
        }
        else
        { 
          throw new DataSAXException
            ("Unrecognized attribute '"+attributes.getQName(i)+"': "
            +formatPosition()
            );
        }
      }

      Type type=resolveType(uri,localName);
      assertCompatibleType(formalType,type);
      
      if (dataConsumer!=null)
      { 
        if (type.isAggregate())
        { 
          if (ref!=null)
          { 
            throw new DataException
              ("Not implemented: using ref with a DataConsumer");
          }
          return new ConsumerFrame(type,dataConsumer);
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
      { return createFrame(type,ref);
      }

    }

  }
  
}