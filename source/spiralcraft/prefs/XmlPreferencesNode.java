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
      throw new BackingStoreException("SAX exception reading "+resourceUri);
    }
    catch (IOException x)
    { System.err.println("Creating new preferences store "+resourceUri);
    }
    
    if (_parseTree==null)
    { _parseTree=ParseTree.createTree(new Element("node",new Attribute[0]));
    }
    _nodeElement=_parseTree.getDocument().getRootElement();
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
          throw new BackingStoreException
            (x.toString()+" (writing "+_resourceUri+")");
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

  private final void resolveChildren()
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
          try
          { loadEntries();
          }
          catch (BackingStoreException x)
          { System.err.println(x.toString());
          }
        }
        else if (element.getLocalName().equals("node"))
        { 
          try
          { loadChild(element);
          }
          catch (BackingStoreException x)
          { System.err.println(x.toString());
          }
        }
        else
        { System.err.println(new BackingStoreException("Unknown element '"+element.getLocalName()+"'"));
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
        { throw new BackingStoreException("Unknown tag '"+element.getLocalName()+"' in map");
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
    { throw new BackingStoreException("Name not defined for node");
    }
    XmlPreferencesNode child=new XmlPreferencesNode(this,name,element);
    _childMap.put(name,child);
  }

  protected final String getSpi(String key)
  {
    if (_entryMap==null)
    { resolveChildren();
    }
    Entry entry=(Entry) _entryMap.get(key);
    if (entry!=null)
    { return entry.getValue();
    }
    return null;
  }

  protected final void putSpi(String key,String value)
  {
    if (_entryMap==null)
    { resolveChildren();
    }
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
    if (_entryMap==null)
    { resolveChildren();
    }
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
    if (_entryMap==null)
    { resolveChildren();
    }
    String[] result=new String[_entryMap.size()];
    _entryMap.keySet().toArray(result);
    return result;
  }

  protected final String[] childrenNamesSpi()
    throws BackingStoreException
  {
    if (_childMap==null)
    { resolveChildren();
    }
    String[] result=new String[_childMap.size()];
    _childMap.keySet().toArray(result);
    return result;
  }

  protected final AbstractPreferences childSpi(String name)
  {
    if (_childMap==null)
    { resolveChildren();
    }
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
        { throw new BackingStoreException("Unknown attribute '"+attribs[i].getLocalName()+"' in entry");
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
