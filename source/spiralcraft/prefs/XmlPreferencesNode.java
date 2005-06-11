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
package spiralcraft.prefs;

import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import spiralcraft.sax.ParseTree;
import spiralcraft.sax.ParseTreeFactory;
import spiralcraft.sax.Element;
import spiralcraft.sax.Attribute;

import spiralcraft.registry.RegistryPathObject;
import spiralcraft.registry.RegistryNode;

import java.net.URI;

import java.io.IOException;

import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

public class XmlPreferencesNode
  extends AbstractPreferences
  implements RegistryPathObject
{
  private ParseTree _parseTree;
  private final URI _resourceUri;
  private final Element _nodeElement;
  private Element _mapElement;
  private HashMap _childMap;
  private HashMap _entryMap;

  public XmlPreferencesNode()
    throws BackingStoreException
  { 
    super(null,"");
    _resourceUri=null;
    _parseTree=ParseTree.createTree(new Element("node",new Attribute[0]));
    _nodeElement=_parseTree.getDocument().getRootElement();
    resolveChildren();    
  }
  
  XmlPreferencesNode(URI resourceUri)
    throws BackingStoreException
  { 
    super(null,"");
    _resourceUri=resourceUri;
    try
    { _parseTree=ParseTreeFactory.fromURI(resourceUri);
    }
    catch (SAXException x)
    { 
      x.printStackTrace();
      throw new BackingStoreFormatException(x.toString(),_resourceUri);
    }
    catch (IOException x)
    { 
      // XXX Need a 'create' mode
      throw new BackingStoreException(x.toString()+"(reading "+_resourceUri+")");
    }
    
    if (_parseTree==null)
    { _parseTree=ParseTree.createTree(new Element("node",new Attribute[0]));
    }
    _nodeElement=_parseTree.getDocument().getRootElement();
    resolveChildren();
  }

  XmlPreferencesNode(XmlPreferencesNode parent,String name,Element nodeElement)
  { 
    super(parent,name);
    _parseTree=null;
    _resourceUri=null;
    _nodeElement=nodeElement;
  }

  public RegistryPathObject registryPathObject(RegistryNode registryNode)
  { return (XmlPreferencesNode) node(registryNode.getName());
  }

  public void flush()
    throws BackingStoreException
  { 
    if (parent()==null)
    { 
      if (_resourceUri!=null)
      {
        synchronized (lock)
        { 
          try
          { ParseTreeFactory.toURI(_parseTree,_resourceUri);
          }
          catch (IOException x)
          { 
            throw new BackingStoreException
              (x.toString()+" (writing "+_resourceUri+")");
          }
          catch (SAXException x)
          { 
            throw new BackingStoreFormatException
              (x.toString(),_resourceUri);
          }
        }
      }
    }
    else
    { parent().flush();
    }
  }

  public void sync()
    throws BackingStoreException
  { 
    // re-read the parse tree from the resource and update
    //   any unchanged data from the store
  }

  protected final void flushSpi()
  { }

  protected final void syncSpi()
  { }

  public final void resolveChildren()
    throws BackingStoreException
  { 
    _childMap=new HashMap();
    _entryMap=new HashMap();
    
    List children=_nodeElement.getChildren(Element.class);
    if (children!=null)
    {
      Iterator it=children.iterator();
      while (it.hasNext())
      {
        Element element=(Element) it.next();
        if (element.getLocalName().equals("map"))
        { 
          _mapElement=element;
          loadEntries();
        }
        else if (element.getLocalName().equals("node"))
        { loadChild(element);
        }
        else
        { 
          throw new BackingStoreFormatException
            ("Unknown element '"
            +element.getLocalName()
            +"'"
            ,_resourceUri
            );
        }
      }
    }
  }

  private final void loadEntries()
    throws BackingStoreException
  {
    List children=_mapElement.getChildren(Element.class);
    if (children!=null)
    {
      Iterator it=children.iterator();
      while (it.hasNext())
      {
        Element element=(Element) it.next();
        if (element.getLocalName().equals("entry"))
        { 
          Entry entry=new Entry(element);
          _entryMap.put(entry.getKey(),entry);
        }
        else
        { 
          throw new BackingStoreFormatException
            ("Unknown tag '"
            +element.getLocalName()
            +"' in map"
            ,_resourceUri
            );
        }
      }
    }

  }

  private final void loadChild(Element element)
    throws BackingStoreException
  {
    String name=null;
    Attribute[] attribs=element.getAttributes();
    for (int i=0;i<attribs.length;i++)
    { 
      if (attribs[i].getLocalName().equals("name"))
      { name=attribs[i].getValue();
      }
    }
    if (name==null)
    { 
      throw new BackingStoreFormatException
        ("Name not defined for node"
        ,_resourceUri
        );
    }
    XmlPreferencesNode child=new XmlPreferencesNode(this,name,element);
    _childMap.put(name,child);
    child.resolveChildren();
  }

  protected final String getSpi(String key)
  {
    Entry entry=(Entry) _entryMap.get(key);
    if (entry!=null)
    { return entry.getValue();
    }
    return null;
  }

  protected final void putSpi(String key,String value)
  {
    Entry entry=(Entry) _entryMap.get(key);
    if (entry==null)
    { 
      entry=new Entry(key,value);
      _entryMap.put(key,entry);
    }
    else
    { entry.setValue(value);
    }

  }

  protected final void removeSpi(String key)
  {
    Entry entry=(Entry) _entryMap.get(key);
    if (entry!=null)
    { entry.remove();
    }
    _entryMap.remove(key);
  }

  protected final void removeNodeSpi()
  { 
    
    _childMap=null;
    _entryMap=null;
    _nodeElement.remove();
    
  }

  protected final String[] keysSpi()
    throws BackingStoreException
  {
    String[] result=new String[_entryMap.size()];
    _entryMap.keySet().toArray(result);
    return result;
  }

  protected final String[] childrenNamesSpi()
    throws BackingStoreException
  {
    String[] result=new String[_childMap.size()];
    _childMap.keySet().toArray(result);
    return result;
  }

  protected final AbstractPreferences childSpi(String name)
  {
    AbstractPreferences child=(AbstractPreferences) _childMap.get(name);
    if (child==null)
    { 
      Element element
        =new Element
          ("node"
          ,new Attribute[]
            { new Attribute("name",name)
            }
          );
      _nodeElement.addChild(element);
      child=new XmlPreferencesNode(this,name,element);
      _childMap.put(name,child);
    }
    return child;
  }

  class Entry
  {
    private Attribute _key;
    private Attribute _value;
    private Element _element;

    /** 
     * Construct an entry from an existing element
     */
    public Entry(Element element)
      throws BackingStoreException
    {
      _element=element;
      Attribute[] attribs=element.getAttributes();
      for (int i=0;i<attribs.length;i++)
      {
        if (attribs[i].getLocalName().equals("key"))
        { _key=attribs[i];
        }
        else if (attribs[i].getLocalName().equals("value"))
        { _value=attribs[i];
        }
        else
        { 
          throw new BackingStoreFormatException
            ("Unknown attribute '"+attribs[i].getLocalName()+"' in entry"
            ,_resourceUri
            );
        }
      }
    }

    public Entry(String key,String value)
    { 
      if (_mapElement==null)
      { 
        _mapElement=new Element("map",null);
        _nodeElement.addChild(_mapElement);
      }
      _key=new Attribute("key",key);
      _value=new Attribute("value",value);
      _element=new Element("entry",new Attribute[] {_key,_value});
      _mapElement.addChild(_element);
    }

    public void remove()
    { _mapElement.removeChild(_element);
    }

    public String getKey()
    { return _key.getValue();
    }

    public String getValue()
    { return _value.getValue();
    }

    public void setValue(String val)
    { _value.setValue(val);
    }
  }
}
