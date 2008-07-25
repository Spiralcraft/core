//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import spiralcraft.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents an Element in an XML document
 */
public class Element
  extends Node
{
  private String _uri;
  private String _localName;
  private String _qName;
  private Attribute[] _attributes;
  private HashMap<String,String> prefixMappings;

  
  /**
   * Constructor for no-namespace client use
   */
  public Element
    (String name
    ,Attribute[] attributes
    )
  { 
    _uri=name;
    _localName=name;
    _qName=name;
    _attributes=attributes;
  }

  /**
   * Constructor for SAX use
   */
  public Element
    (String uri
    ,String localName
    ,String qName
    ,Attributes attributes
    )
  { 
    _uri=uri;
    _localName=localName;
    _qName=qName;
    
    int numAttributes=attributes.getLength();
    if (numAttributes>0)
    { 
      _attributes=new Attribute[numAttributes];
      for (int i=0;i<numAttributes;i++)
      { 
        _attributes[i]=
          new Attribute
            (attributes.getLocalName(i)
            ,attributes.getQName(i)
            ,attributes.getType(i)
            ,attributes.getURI(i)
            ,attributes.getValue(i)
            )
            ;
        
      }
    }
  }

  public int pruneElements(Set<String> uriList)
  { 
    int count=0;
    Iterator<Node> it=getChildren().iterator();
    while (it.hasNext())
    {
      Node node=it.next();
      if (node instanceof Element)
      {
        Element element=(Element) node;
        if (uriList.contains(element.getResolvedName()))
        { 
          it.remove();
          count++;
        }
        else
        { count+=element.pruneElements(uriList);
        }
      }
    }
    return count;
  }
  
  public int pruneAttributes(String elementURI,Set<String> attributeURIList)
  {
    int count=0;
    if (elementURI==null || elementURI.equals(getResolvedName()))
    {
      if (_attributes!=null)
      {
        ArrayList<Attribute> list=new ArrayList<Attribute>(_attributes.length);
        for (int i=0;i<_attributes.length;i++)
        { 
          if (!attributeURIList.contains(_attributes[i].getResolvedName()))
          { list.add(_attributes[i]);
          }
          else
          { count++;
          }
        }
        if (count>0)
        { _attributes=list.toArray(new Attribute[list.size()]);
        }
      }
    }

    if (getChildren()!=null)
    {
      Iterator<Node> it=getChildren().iterator();
      while (it.hasNext())
      {
        Node node=it.next();
        if (node instanceof Element)
        { count+=((Element) node).pruneAttributes(elementURI,attributeURIList);
        }
      }
    }
    return count;
    
  }
  
  /**
   * @return A String in the format of: { uri "#" } name
   */
  public String getResolvedName()
  { return _uri!=null && !_uri.isEmpty()?_uri+"#"+_localName:_localName;
  }
  
  /**
   * @return Any namespace prefix mappings declared in this Element 
   */
  public HashMap<String,String> getPrefixMappings()
  { return prefixMappings;
  }
  
  /**
   * Specify any namespace prefix mappings declared with this Element
   * 
   * @param mappings
   */
  public void setPrefixMappings(HashMap<String,String> mappings)
  { prefixMappings=mappings;
  }
  
   

  

  /**
   * A concatenation of the character data in this element
   */
  public String getCharacters()
  { 
    List<Characters> children
      =getChildren(Characters.class);

    StringBuilder buf=new StringBuilder();
    for (Characters c: children)
    { buf.append(c.getCharacters());
    }
    return buf.toString();
  }
  
  public void removeAttribute(Attribute attribute)
  { _attributes=(Attribute[]) ArrayUtil.remove(_attributes,attribute);
  }

  public void addAttribute(Attribute attribute)
  { _attributes=(Attribute[]) ArrayUtil.append(_attributes,attribute);
  }

  public Attribute[] getAttributes()
  { return _attributes;
  }

  /**
   * 
   * @return The namespace URI
   */
  public String getURI()
  { return _uri;
  }

  /**
   * 
   * @return The qualified tag name (namespace:name)
   */
  public String getQName()
  { return _qName;
  }

  /**
   * 
   * @return The local name (without a namespace prefix)
   */
  public String getLocalName()
  { return _localName;
  }

  public void playEvents(ContentHandler handler)
    throws SAXException
  { 
    handler.startElement
      (_uri
      ,_localName
      ,_qName
      ,new ArrayAttributes(_attributes)
      );
    playChildEvents(handler);
    handler.endElement
      (_uri
      ,_localName
      ,_qName
      );
  }

  public String toString()
  { 
    return super.toString()
      +"[uri="+_uri
      +",localName="+_localName
      +",qName="+_qName
      +",attributes="+_attributes
      +"]"
      ;
  }

}
