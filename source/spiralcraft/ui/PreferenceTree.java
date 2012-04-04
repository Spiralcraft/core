//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import spiralcraft.sax.Attribute;
import spiralcraft.sax.Characters;
import spiralcraft.sax.Document;
import spiralcraft.sax.Element;
import spiralcraft.sax.ParseTree;
import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.XmlWriter;
import spiralcraft.util.ListMap;
import spiralcraft.util.Path;
import spiralcraft.util.thread.ThreadLocalStack;
import spiralcraft.util.tree.PathTree;

/**
 * <p>A PathTree where each node stores a dictionary that maps a string key to
 *   a set of values. 
 * </p>
 * 
 * <p>Components pull data from the PreferenceTree by property/sub-node name
 *   and explicitly create all nodes and properties in order to maximize
 *   forward compatibility of the data in the face of changes to the
 *   component graph. Minimizing the coupling of the preferences data model
 *   to the application architecture is a primary goal of this function.
 * </p>
 * 
 * <p>A PreferenceTree has the intrinsic ability to convert itself to and from
 *   a SAX Document tree where it can be read from or written to XML.
 * </p>
 * 
 * <p>The PreferenceTree class manages a thread-contextual stack of instances
 *   in order to facilitate the overlay of a hierarchical component
 *   model.
 * </p>
 * 
 * @author mike
 *
 */
public class PreferenceTree
  extends PathTree<ListMap<String,String>>
{
  
  private static final ThreadLocalStack<PreferenceTree> contextStack
    =new ThreadLocalStack<PreferenceTree>();

  public static final PreferenceTree contextInstance()
  { return contextStack.get();
  }
 
  public static final void push(PreferenceTree contextInstance)
  { contextStack.push(contextInstance);
  }
  
  public static final void pop()
  { contextStack.pop();
  }
  
  public PreferenceTree(String name)
  { 
    super(name);
    set(new ListMap<String,String>());
  }

  public void set(String name,String value)
  { get().set(name,value);
  }

  public void add(String name,String value)
  { get().add(name,value);
  }
  
  public List<String> get(String name)
  { return get().get(name);
  }
  
  public String getFirst(String name)
  { return get().getFirst(name);
  }

  public String getFirst(String name,String defaultVal)
  { 
    String val=getFirst(name);
    return val!=null?val:defaultVal;
  }

  public String getLast(String name)
  { return get().getLast(name);
  }

  public String getLast(String name,String defaultVal)
  { 
    String val=getLast(name);
    return val!=null?val:defaultVal;
  }
  
  public List<String> remove(String name)
  { return get().remove(name);
  }

  public void remove(String name,String value)
  { get().remove(name,value);
  }
  
  public PreferenceTree ensurePath(Path path)
  {
    if (path.isAbsolute() && getParent()!=null)
    { return ((PreferenceTree) getRoot()).ensurePath(path);
    }
    else if (path.size()==0)
    { return this;
    }
    else
    {
      String childName=path.firstElement();
      Path childPath=path.subPath(1);
      PreferenceTree child=(PreferenceTree ) getChild(childName);
      if (child==null)
      { 
        child=new PreferenceTree(childName);
        addChild(child);
      }
      return child.ensurePath(childPath);
    }
    
  }
  

  public void storeToXml(OutputStream out)
    throws SAXException
  {
    Element element=toSAXElement();
    Document document=new Document(element);
    XmlWriter writer=new XmlWriter(out,Charset.forName("UTF-8"));
    writer.setFormat(true);
    document.playEvents(writer);
  }

  public void loadFromXml(InputStream in) 
    throws SAXException, IOException
  { 
    ParseTree parseTree=ParseTreeFactory.fromInputStream(in);
    Document document=parseTree.getDocument();
    Element root=document.getRootElement();
    
    fromSAXElement(root);
  }
  
  
  
  public Element toSAXElement()
  {
    Element prefsNode
      =new Element
        ("Node"
        ,new Attribute[]
            {new Attribute("name",getName())
            }
        );
    
    for (Map.Entry<String,List<String>> entry: get().entrySet())
    {
      Element entryNode
        =new Element
          ("Entry"
          ,new Attribute[]
              { new Attribute("key",entry.getKey())
              }
          );
      prefsNode.addChild(entryNode);
      for (String str:entry.getValue())
      {
        Element valueNode
          =new Element("Value",null);
        valueNode.addChild(new Characters(str));
        entryNode.addChild(valueNode);
      }
    }
    
    for (PathTree<?> child: getChildren())
    { prefsNode.addChild( ((PreferenceTree) child).toSAXElement());
    }
    return prefsNode;
    
  }
  
  public void fromSAXElement(Element element)
    throws SAXException
  {
    if (!element.getLocalName().equals("Node"))
    { 
      throw new SAXException
        ("Expected an element named \"Node\", not \""+element.getQName()+"\"");
    }
    
    for (Element entry:element.getChildrenByQName("Entry"))
    {
      Attribute keyAttr=entry.getAttributeByQName("key");
      String key=(keyAttr==null)?"":keyAttr.getValue();
      
      for (Element value:entry.getChildrenByQName("Value"))
      { add(key,value.getCharacters().trim());
      }
    }
    
    for (Element child:element.getChildrenByQName("Node"))
    {
      Attribute nameAttr=child.getAttributeByQName("name");
      if (nameAttr==null)
      { throw new SAXException("Node name cannot be null");
      }
      PreferenceTree childNode=new PreferenceTree(nameAttr.getValue());
      childNode.fromSAXElement(child);
      addChild(childNode);
      
    }
    
  }
}
