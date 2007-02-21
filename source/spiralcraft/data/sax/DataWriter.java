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

import spiralcraft.stream.Resolver;
import spiralcraft.stream.Resource;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Field;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import spiralcraft.util.StringUtil;

import spiralcraft.sax.XmlWriter;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;

import java.util.Iterator;
import java.util.HashMap;

public class DataWriter
{ 
    
   
  public void writeToUri
    (URI resourceUri
    ,Tuple data
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
    ,Tuple tuple
    )
    throws IOException,DataException
  {
    OutputStream out=resource.getOutputStream();
    writeToOutputStream(out,tuple);
    if (out!=null)
    {
      out.flush();
      out.close();
    }
  }
  
  public void writeToOutputStream
    (OutputStream out
    ,Tuple tuple
    )
    throws IOException,DataException
  {
    try
    { new Context(out).write(tuple);
    }
    catch (SAXException x)
    { throw new DataException("Error writing data "+x,x);
    }
    
  }
    
  
  
}

class Context
{
  private static final AttributesImpl NULL_ATTRIBUTES
    =new AttributesImpl();

  private static final URI STANDARD_NAMESPACE_URI
    =URI.create("java:/spiralcraft/data/types/standard/");

  private final XmlWriter writer;
  private Frame currentFrame; 
  
  public Context(OutputStream out)
  { writer=new XmlWriter(out);
  }
  
  public void write(Tuple tuple)
    throws IOException,SAXException,DataException
  {
    writer.startDocument();
    currentFrame=new TupleFrame(tuple);
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
    protected String qName;
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
  }

  abstract class TypeFrame
    extends Frame
  {
    protected final Type<?> type;
    protected final String typeName;
    protected final URI typeNamespace;
    protected final String qName;
    private final HashMap<URI,String> namespaceMap
      =new HashMap<URI,String>();
    private final HashMap<String,URI> reverseNamespaceMap
      =new HashMap<String,URI>();
    protected AttributesImpl attributes=NULL_ATTRIBUTES;
    
    public TypeFrame(Type<?> type)
    {
      this.type=type;
      URI typeUri=type.getUri();
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
      if (attributes==NULL_ATTRIBUTES)
      { attributes=new AttributesImpl();
      }
      attributes.addAttribute
        (null
        ,namespace
        ,"xmlns:"+namespace
        ,null
        ,uri.toString()
        );
      return namespace;   
    }

    public boolean isNamespaceUsed(String namespace)
    { 
      if (reverseNamespaceMap.get(namespace)!=null)
      { return true;
      }
      else
      { return super.isNamespaceUsed(namespace);
      }
    }
    
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
      writeString("\r\n");
      writeString(indentString);
      writer.startElement(null,null,qName,attributes);
    }
    
    protected final void endType(boolean addLine)
      throws SAXException
    {
      if (addLine)
      {
        writeString("\r\n");
        writeString(indentString);
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
    
    @SuppressWarnings("unchecked")
    public void next()
      throws SAXException
    {
      if (!singleContext)
      { startType();
      }
      writeString(((Type <? super Object>)type).toString(value));      
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
    
    public TupleFrame(Tuple tuple)
    { 
      super(tuple.getType());
      this.tuple=tuple;
    }
    
    
    public void next()
      throws SAXException
    {
      if (fieldIterator==null)
      { 
        startType();
        fieldIterator=tuple.getScheme().fieldIterable().iterator();
      }
      else if (fieldIterator.hasNext())
      {
        Field field=fieldIterator.next();
        if (field.getType().isAggregate())
        { currentFrame=new AggregateFieldFrame(tuple,field);
        }
        else
        { currentFrame=new SimpleFieldFrame(tuple,field);
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
      writeString("\r\n");
      writeString(indentString);
      writer.startElement(null,null,field.getName(),NULL_ATTRIBUTES);
    }
    
    protected final void closeField(boolean addLine)
      throws SAXException
    { 
      if (addLine)
      {
        writeString("\r\n");
        writeString(indentString);
      }
      writer.endElement(null,null,field.getName());
    }

    
  }

  class AggregateFieldFrame
    extends FieldFrame
  {
    
    private Iterator iterator;
    private Type componentType;
    private boolean hasOne;
    
    public AggregateFieldFrame(Tuple tuple,Field field)
    { 
      super(tuple,field);
      componentType=field.getType().getContentType();
    }
    
    
    public void next()
      throws SAXException,DataException
    {
      Aggregate value=(Aggregate) field.getValue(tuple);
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
        System.out.println("Aggregate Field: iterating "+item);
        if (item instanceof DataComposite)
        { currentFrame=new TupleFrame(((DataComposite) item).asTuple());
        }
        else
        { currentFrame=new PrimitiveFrame(componentType,item,false);
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
        { currentFrame=new TupleFrame((Tuple) value);
        }
        else
        { 
          primitive=true;
          currentFrame=new PrimitiveFrame(field.getType(),value,true);
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

