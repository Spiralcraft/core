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


import spiralcraft.data.DataComposite;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Field;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.EmptyIterator;
import spiralcraft.util.IteratorChain;
import spiralcraft.util.refpool.URIPool;
import spiralcraft.util.string.StringUtil;
import spiralcraft.vfs.Resolver;
import spiralcraft.vfs.Resource;

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.sax.XmlWriter;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import java.net.URI;

import java.util.Iterator;
import java.util.HashMap;

public class DataWriter
{ 
    
  protected static final ClassLog log
    =ClassLog.getInstance(DataWriter.class);
  protected static final Level debugLevel
    =ClassLog.getInitialDebugLevel(DataWriter.class, null);
  
  public void writeToURI
    (URI resourceUri
    ,DataComposite data
    )
    throws IOException,DataException
  {
    writeToResource
      (Resolver.getInstance().resolve(resourceUri)
      ,data
      );
  }

  public void writeToResource
    (Resource resource
    ,DataComposite data
    )
    throws IOException,DataException
  {
    OutputStream out=resource.getOutputStream();
    writeToOutputStream(out,data);
    if (out!=null)
    {
      out.flush();
      if (out instanceof FileOutputStream)
      { ((FileOutputStream) out).getFD().sync();
      }
      out.close();
    }
  }
  
  public void writeToWriter
    (Appendable out
    ,DataComposite data
    )
    throws DataException
  {
    try
    { new Context(this,new XmlWriter(out,null)).write(data);
    }
    catch (SAXException x)
    { throw new DataException("Error writing data "+x,x);
    }
  
  }

  public void writeToOutputStream
    (OutputStream out
    ,DataComposite data
    )
    throws DataException
  {
    try
    { new Context(this,new XmlWriter(out,null)).write(data);
    }
    catch (SAXException x)
    { throw new DataException("Error writing data "+x,x);
    }
    
  }
    
  public String writeToString(DataComposite data)
    throws DataException
  { 
    StringWriter writer=new StringWriter();
    writeToWriter(writer,data);
    return writer.toString();
  }
  
}

@SuppressWarnings({"unchecked","rawtypes"}) // Mostly runtime type resolution
class Context
{
  private static final AttributesImpl NULL_ATTRIBUTES
    =new AttributesImpl();

  private static final URI STANDARD_NAMESPACE_URI
    =URIPool.create("class:/spiralcraft/data/types/standard/");

  private final XmlWriter writer;
  private Frame currentFrame; 
  private ClassLog log;
  
  public Context(DataWriter dataWriter,XmlWriter writer)
  { 
    this.writer=writer;
    this.log=DataWriter.log;
  }
  
  public void write(DataComposite data)
    throws SAXException,DataException
  {
    if (data==null)
    { 
      throw new IllegalArgumentException
        ("Cannot write a null DataComposite object");
    }
    writer.startDocument();
    if (data.isTuple())
    { currentFrame=new TupleFrame(data.asTuple());
    }
    else
    { currentFrame=new AggregateFrame(data.asAggregate());
    }
    while (currentFrame!=null)
    { currentFrame.next();
    }
    writer.endDocument();
  }

  /**
   * Holder for an element of the data tree
   */
  abstract class Frame
  { 
//    protected String qName;
    protected final Frame parentFrame;
    protected final String indentString;

    public abstract void next()
      throws SAXException,DataException;
    
    public String getNamespace(URI uri)
    { 
      if (parentFrame!=null)
      { return parentFrame.getNamespace(uri);
      }
      else
      { return null;
      }
    }

    public boolean isNamespaceUsed(String namespace)
    { 
      if (parentFrame!=null)
      { return parentFrame.isNamespaceUsed(namespace);
      }
      else
      { return false;
      }
    }
      
    public Frame()
    { 
      parentFrame=currentFrame;
      if (parentFrame!=null)
      { indentString=parentFrame.getIndentString().concat("  ");
      }
      else
      { indentString="";
      }
    }
    
    public String getIndentString()
    { return indentString;
    }

    protected final void finish()
    { currentFrame=parentFrame;
    }
    
    protected void writeString(String str)
      throws SAXException
    { 
      if (str!=null)
      { writer.characters(str.toCharArray(),0,str.length());
      }
    }
    
    protected void writeWhitespace(String str)
      throws SAXException
    { 
      if (str!=null)
      { writer.ignorableWhitespace(str.toCharArray(),0,str.length());
      }
    }
    
    protected void pushCompositeFrame(DataComposite data)
    {
      if (data.isTuple())
      { currentFrame=new TupleFrame(data.asTuple());
      }
      else
      { currentFrame=new AggregateFrame(data.asAggregate());
      }
    }
    
  }

  abstract class TypeFrame
    extends Frame
  {
    protected final Type type;
    protected final String typeName;
    protected final URI typeNamespace;
    protected final String qName;
    private final HashMap<URI,String> namespaceMap
      =new HashMap<URI,String>();
    private final HashMap<String,URI> reverseNamespaceMap
      =new HashMap<String,URI>();
    private AttributesImpl attributes=NULL_ATTRIBUTES;
    
    public TypeFrame(Type type,String qname)
    {
      this.type=type;
      URI typeUri=type.getURI();
      String[] path=StringUtil.tokenize(typeUri.getPath(),"/");

      
      typeName=path[path.length-1];  
      typeNamespace=typeUri.resolve(".");
      this.qName=qname;
    }
    
    public TypeFrame(Type type)
    {
      this.type=type;
      URI typeUri=type.getURI();
      String[] path=StringUtil.tokenize(typeUri.getPath(),"/");

      
      typeName=path[path.length-1];  
      typeNamespace=typeUri.resolve(".");

      // (make namespaces work)
      String namespace=getNamespace(typeNamespace);
      if (namespace==null)
      { namespace=makeNamespace(typeNamespace);
      }
      if (namespace!=null)
      { qName=namespace+":"+typeName;
      }
      else
      { qName=typeName;
      }
    }

    protected void addAttribute
      (String uri,String localname,String qname,String type,String value)
    {
      if (attributes==NULL_ATTRIBUTES)
      { attributes=new AttributesImpl();
      }
      attributes.addAttribute(uri,localname,qname,type,value);
    }
    
    private String makeNamespace(URI uri)
    {
      String namespace=null;
      if (uri.equals(STANDARD_NAMESPACE_URI))
      { return null;
      }
      
      String[] path=StringUtil.tokenize(uri.getPath(),"/");
      if (path.length>0)
      { namespace=path[path.length-1];
      }
      else
      { namespace="local";
      }
      
      String usedNamespace=namespace;        
      int disambiguator=2;
      while (isNamespaceUsed(namespace))
      { namespace=usedNamespace.concat(Integer.toString(disambiguator++));
      }
      namespaceMap.put(uri,namespace);
      reverseNamespaceMap.put(namespace,uri);

      addAttribute
        (null
        ,namespace
        ,"xmlns:"+namespace
        ,null
        ,uri.toString()
        );
      return namespace;   
    }

    @Override
    public boolean isNamespaceUsed(String namespace)
    { 
      if (reverseNamespaceMap.get(namespace)!=null)
      { return true;
      }
      else
      { return super.isNamespaceUsed(namespace);
      }
    }
    
    @Override
    public String getNamespace(URI uri)
    {
      String namespace=namespaceMap.get(uri);
      if (namespace==null)
      { namespace=super.getNamespace(uri);
      }
      return namespace;    
        
    }
    
    protected final void startType()
      throws SAXException
    {
      writeWhitespace("\r\n");
      writeWhitespace(indentString);
      writer.startElement(null,null,qName,attributes);
    }
    
    protected final void endType(boolean addLine)
      throws SAXException
    {
      if (addLine)
      {
        writeWhitespace("\r\n");
        writeWhitespace(indentString);
      }
      writer.endElement(null,null,qName);
    }
    
    
  }

  class PrimitiveFrame
    extends TypeFrame
  {
    private final Object value;
    private final boolean singleContext;


    
    public PrimitiveFrame(Type<?> type,Object value,boolean singleContext)
    {
      super(type);
      this.value=value;
      this.singleContext=singleContext; 
    }

    public PrimitiveFrame(Type<?> type,Object value,String qname)
    {
      super(type,qname);
      this.value=value;
      this.singleContext=false; 
    }
    
    @Override
    public void next()
      throws SAXException
    {
      if (!singleContext)
      { startType();
      }
      
      try
      {  writeString(((Type <? super Object>)type).toString(value));
      }
      catch (IllegalArgumentException x)
      { 
        throw new DataSAXException
          ("Error writing value ["+value+"] for "+type.getURI(),x);
      }
      
      if (!singleContext)
      { endType(false);
      }
      finish();
      return;
    }
  }
  
  class TupleFrame
    extends TypeFrame
  {
    private final Tuple tuple;
    private Iterator<? extends Field> fieldIterator;
    private boolean empty=true;
    
    public TupleFrame(Tuple tuple)
    { 
      super(tuple.getType());
      this.tuple=tuple;
    }
    
    
    @Override
    public void next()
      throws SAXException,DataException
    {
      if (fieldIterator==null)
      { 
        
        if (tuple instanceof DeltaTuple)
        { 
          DeltaTuple dt=(DeltaTuple) tuple;
          if (DataWriter.debugLevel.canLog(Level.FINE))
          { DataWriter.log.fine("Writing DeltaTuple "+tuple.getType().getURI());
          }
          
          Field[] dirtyFields=dt.getDirtyFields();
          if (dirtyFields!=null)
          {
            fieldIterator
              =ArrayUtil.iterator(dirtyFields);
          }
          else 
          { fieldIterator=new EmptyIterator<Field>();
          }
          
          if (dt.isDelete())
          { 
            addAttribute(null,"delta","delta",null,"D");
            if (dt.getType().getPrimaryKey()!=null)
            {
              fieldIterator
                =ArrayUtil.iterator
                  (dt.getType().getPrimaryKey().getSourceFields());
            }
            else
            {
              fieldIterator
                =dt.getType().getFieldSet().fieldIterable().iterator();              
            }
          }
          else if (dt.getOriginal()!=null)
          { 
            addAttribute(null,"delta","delta",null,"U");
            if (dt.getType().getPrimaryKey()!=null)
            {
              fieldIterator
                =new IteratorChain<Field>
                  (ArrayUtil.iterator
                    (dt.getType().getPrimaryKey().getSourceFields())
                  ,fieldIterator
                  );

              if (DataWriter.debugLevel.isFine())
              {
                for (Field field:dt.getType().getPrimaryKey().getSourceFields())
                { 
                  log.fine("Primary Key Field: "+field+" type="+field.getFieldSet());
                  log.fine("Primary Key Value: "+field.getValue(dt)+" from "+dt);
                  log.fine("Base extent: "+dt.widen(field.getFieldSet().getType()));
                }
              }
              
            }
            else
            { 
              log.warning("No primary key writing update transaction for "+dt);
              fieldIterator
                =dt.getType().getFieldSet().fieldIterable().iterator();
            }
          }
          else
          { addAttribute(null,"delta","delta",null,"C");
          }
          
          
          
        }
        else
        { 
          if (tuple instanceof JournalTuple)
          { 
            JournalTuple jt=(JournalTuple) tuple;
            addAttribute
              (null,"tx","tx",null,Long.toString(jt.getTransactionId()));
            addAttribute
              (null,"v","v",null,Long.toString(jt.getVersion()));
          }
          
          if (tuple.getType()!=null)
          { 
            if (DataWriter.debugLevel.canLog(Level.FINE))
            { DataWriter.log.fine("Writing Tuple "+tuple.getType().getURI());
            }
            // Make sure we include base type Fields
            fieldIterator
              =tuple.getType().getFieldSet().fieldIterable().iterator();
          }
          else
          { 
            if (DataWriter.debugLevel.canLog(Level.FINE))
            { DataWriter.log.fine("Writing untyped Tuple "+tuple.getFieldSet());
            }
            DataWriter.log.fine("Writing untyped tuple "+tuple.getFieldSet());
            fieldIterator=tuple.getFieldSet().fieldIterable().iterator();
          }
        }
        startType();
        
      }
      else if (fieldIterator.hasNext())
      {
        
        Field field=fieldIterator.next();
        if (DataWriter.debugLevel.canLog(Level.FINE))
        { DataWriter.log.fine("Starting field "+field.getURI());
        }
        
        if (!field.isTransient() && !field.isStatic())
        {
          Object value=field.getValue(tuple);
          if (value!=null)
          {
            empty=false;
            if (field.getType().isAggregate())
            { currentFrame=new AggregateFieldFrame(tuple,field);
            }
            else
            { currentFrame=new SimpleFieldFrame(tuple,field);
            }
          }
        }
      }
      else
      {
        endType(!empty);
        finish();
        return;
      }
    }
    
  }
  
  class AggregateFrame
    extends TypeFrame
  {
    private final Aggregate<?> aggregate;
    private Iterator<?> aggregateIterator;
    private Type componentType;
  
    public AggregateFrame(Aggregate<?> aggregate)
    { 
      super(aggregate.getType());
      this.aggregate=aggregate;
      this.componentType=type.getContentType();
    }
  
  
    @Override
    public void next()
      throws SAXException,DataException
    {
      if (aggregateIterator==null)
      { 
        startType();
        aggregateIterator=aggregate.iterator();
      }
      else if (aggregateIterator.hasNext())
      {
        Object object=aggregateIterator.next();
        if (object==null)
        {
        }
        else if (object instanceof DataComposite)
        { pushCompositeFrame((DataComposite) object);
        }
        else
        { 
          Type objectType=componentType;
          
          if (objectType.getNativeClass()!=object.getClass())
          { objectType=ReflectionType.canonicalType(object.getClass());
          }
          
          if (objectType.isDataEncodable())
          { 
            DataComposite data
              =objectType.toData(object);
            pushCompositeFrame(data);
          }
          else
          { currentFrame=new PrimitiveFrame(objectType,object,false);
          }
        }
        
      }
      else
      {
        endType(true);
        finish();
        return;
      }
    }
  
  }

  

  abstract class FieldFrame
    extends Frame
  {
    protected final Field field;
    protected final Tuple tuple;
    
    protected boolean opened;

    public FieldFrame(Tuple tuple,Field field)
    {
      this.field=field;
      this.tuple=tuple;
    }
    
    protected final void openField()
      throws SAXException
    {
      writeWhitespace("\r\n");
      writeWhitespace(indentString);
      writer.startElement(null,null,field.getName(),NULL_ATTRIBUTES);
    }
    
    protected final void closeField(boolean addLine)
      throws SAXException
    { 
      if (addLine)
      {
        writeWhitespace("\r\n");
        writeWhitespace(indentString);
      }
      writer.endElement(null,null,field.getName());
    }

    
  }

  class AggregateFieldFrame
    extends FieldFrame
  {
    
    private Iterator<?> iterator;
    private Type componentType;
    private boolean hasOne;
    
    public AggregateFieldFrame(Tuple tuple,Field field)
    { 
      super(tuple,field);
      componentType=field.getType().getContentType();
    }
    
    
    @Override
    public void next()
      throws SAXException,DataException
    {
      Aggregate value=null;
      if (componentType.isPrimitive())
      { 
        Object fieldValue=field.getValue(tuple);
        if (fieldValue.getClass().isArray())
        { value=(Aggregate) field.getType().toData(fieldValue);
        }
        else
        { value=(Aggregate) fieldValue;
        }
      }
      else
      { value=(Aggregate) field.getValue(tuple);
      }
      if (value==null)
      { 
        finish();
        return;
      }
      
      if (!opened)
      {
        opened=true;
        openField();
        iterator=value.iterator();
      }
      else if (iterator.hasNext())
      {
        hasOne=true;
        Object item=iterator.next();
//        System.out.println("Aggregate Field: iterating "+item);
        if (item instanceof DataComposite)
        { currentFrame=new TupleFrame(((DataComposite) item).asTuple());
        }
        else
        { 
          Type objectType=componentType;
          
          if (objectType.getNativeClass()!=item.getClass())
          { objectType=ReflectionType.canonicalType(item.getClass());
          }
          
          if (objectType.isDataEncodable())
          { 
            DataComposite data
              =objectType.toData(item);
            pushCompositeFrame(data);
          }
          else
          { currentFrame=new PrimitiveFrame(objectType,item,false);
          }
          
        }
      }
      else
      {
        closeField(hasOne);
        finish();
        return;
      }
    }
  }
  
  class SimpleFieldFrame
    extends FieldFrame
  {
    
    private boolean primitive;
    
    public SimpleFieldFrame(Tuple tuple,Field field)
    { super(tuple,field);
    }
    
    @Override
    public void next()
      throws SAXException,DataException
    {
      Object value=field.getValue(tuple);
      if (value==null)
      { 
        finish();
        return;
      }
      
      if (!opened)
      {
        opened=true;
        openField();

        
        if (value instanceof DataComposite)
        { 
          if (((DataComposite) value).isTuple())
          { currentFrame=new TupleFrame((Tuple) value);
          }
          else 
          { currentFrame=new AggregateFrame((Aggregate) value);
          }
        }
        else
        { 
          if (field.getType().isDataEncodable())
          { 
            Type ftype=field.getType();
            DataComposite data
              =ftype.toData(value);
            pushCompositeFrame(data);
          }
          else
          { 
            primitive=true;
            currentFrame=new PrimitiveFrame(field.getType(),value,true);
          }
        }
      }
      else
      {
        closeField(!primitive);
        finish();
        return;
      }
    }
  }
  
}

