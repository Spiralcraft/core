package spiralcraft.prefs;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.NodeChangeListener;

import java.util.HashMap;
import java.util.Iterator;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.StringUtil;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A node in a hierarchical collection of preferences data.
 *
 * Extends the java.util.prefs API to provide applications
 *   with more control control over preferences storage,
 *   defaults, and composition with respect to application
 *   structure.
 *
 * This implementation of Preferences is a wrapper around
 *   a java.util.prefs.Preferences object.
 */
public class ProxyPreferencesNode
  extends Preferences
{
  private final Preferences _delegate;
  private final Preferences _default;
  private final ProxyPreferencesNode _parent;
  private final ProxyPreferencesNode _root;
  private final Object _lock=new Object();
  private final String _name;
  private final String _absolutePath;
  private final HashMap _childCache=new HashMap();
  private boolean _removed;

  /**
   * Construct a root ProxyPreferencesNode backed by a delegate, with an optional
   *   Preferences instance to use for default values
   */
  public ProxyPreferencesNode
    (Preferences delegate
    ,Preferences defalt
    )
  {
    _parent=null;
    _name="/";
    _root=this;
    _absolutePath="/";
    _delegate=delegate;
    _default=defalt;
  }

  /**
   * Construct a child ProxyPreferencesNode backed by a delegate, with an optional
   *   Preferences instance to use for default values
   */
  ProxyPreferencesNode
    (ProxyPreferencesNode parent
    ,String name
    ,Preferences delegate
    ,Preferences defalt
    )
  { 
    _parent=parent;
    _name=name;
    _root=_parent.getRoot();
    _absolutePath=_parent.absolutePath()+"/"+_name;
    _delegate=delegate;
    _default=defalt;
  }
   
  public ProxyPreferencesNode getRoot()
  { return _root;
  }
  
  public void put(String key, String value)
  { _delegate.put(key,value);
  }

  public String get(String key, String def)
  {
    if (_default!=null)
    { return _delegate.get(key,_default.get(key,def));
    }
    else
    { return _delegate.get(key,def);
    }
  }

  public void remove(String key)
  { _delegate.remove(key);
  }

  public void clear() throws BackingStoreException
  { _delegate.clear();
  }

  public void putInt(String key, int value)
  { _delegate.putInt(key,value);
  }

  public int getInt(String key, int def)
  {
    if (_default!=null)
    { return _delegate.getInt(key,_default.getInt(key,def));
    }
    else
    { return _delegate.getInt(key,def);
    }
  }

  public void putLong(String key, long value)
  { _delegate.putLong(key,value);
  }

  public long getLong(String key, long def)
  {
    if (_default!=null)
    { return _delegate.getLong(key,_default.getLong(key,def));
    }
    else
    { return _delegate.getLong(key,def);
    }
  }

  public void putBoolean(String key, boolean value)
  { _delegate.putBoolean(key,value);
  }

  public boolean getBoolean(String key, boolean def)
  {
    if (_default!=null)
    { return _delegate.getBoolean(key,_default.getBoolean(key,def));
    }
    else
    { return _delegate.getBoolean(key,def);
    }
  }

  public void putFloat(String key, float value)
  { _delegate.putFloat(key,value);
  }

  public float getFloat(String key, float def)
  {
    if (_default!=null)
    { return _delegate.getFloat(key,_default.getFloat(key,def));
    }
    else
    { return _delegate.getFloat(key,def);
    }
  }

  public void putDouble(String key, double value)
  { _delegate.putDouble(key,value);
  }

  public double getDouble(String key, double def)
  {
    if (_default!=null)
    { return _delegate.getDouble(key,_default.getDouble(key,def));
    }
    else
    { return _delegate.getDouble(key,def);
    }
  }

  public void putByteArray(String key, byte[] value)
  { _delegate.putByteArray(key,value);
  }

  public byte[] getByteArray(String key, byte[] def)
  {
    if (_default!=null)
    { return _delegate.getByteArray(key,_default.getByteArray(key,def));
    }
    else
    { return _delegate.getByteArray(key,def);
    }
  }

  public String[] keys() throws BackingStoreException
  {
    if (_default!=null)
    { return (String[]) ArrayUtil.mergeArrays(_delegate.keys(),_default.keys());
    }
    else
    { return _delegate.keys();
    }
  }

  public String[] childrenNames() throws BackingStoreException
  {
    if (_default!=null)
    { return (String[]) ArrayUtil.mergeArrays(_delegate.childrenNames(),_default.childrenNames());
    }
    else
    { return _delegate.childrenNames();
    }
  }

  public Preferences parent()
  { return _parent;
  }

  public Preferences node(String pathName)
  {
    synchronized(_lock) 
    {
      if (_removed)
      { throw new IllegalStateException("Node has been removed.");
      }

      if (pathName.equals(""))
      { return this;
      }
      
      if (pathName.equals("/"))
      { return _root;
      }

      if (pathName.charAt(0) != '/')
      { return node(StringUtil.tokenize(pathName,"/"),0);
      }
    }

    return _root.node(StringUtil.tokenize(pathName.substring(1), "/"),0);
  }

  ProxyPreferencesNode node(String[] path,int index)
  {
    synchronized (_lock)
    {
      String name=path[index];
      if (name==null || name.equals(""))
      { throw new IllegalArgumentException("Consecutive slashes in path");
      }
      ProxyPreferencesNode child=
        (ProxyPreferencesNode) _childCache.get(name);
      if (child==null)
      {
        if (name.length() > MAX_NAME_LENGTH)
        {
          throw new IllegalArgumentException
            ("Node name " + name + " too long");
        }
        child=childSpi(name);
        _childCache.put(name,child);
      }

      index++;
      if (index==path.length)
      { return child;
      }
      else
      { return child.node(path,index);
      }
    }
  }

  private ProxyPreferencesNode childSpi(String name)
  {
    if (_default!=null)
    { return new ProxyPreferencesNode(this,name,_delegate.node(name),_default.node(name));
    }
    else
    { return new ProxyPreferencesNode(this,name,_delegate.node(name),null);
    }
  }

  private ProxyPreferencesNode getChild(String name)
    throws BackingStoreException
  {
    synchronized (_lock)
    {
      String childName=(String) ArrayUtil.find(childrenNames(),name);
      if (childName!=null)
      { return childSpi(childName);
      }
    }
    return null;
  }
      
  public boolean nodeExists(String pathName)
    throws BackingStoreException
  {
    synchronized(_lock)
    {
      if (pathName.equals(""))
      { return !_removed;
      }

      if (_removed)
      { throw new IllegalStateException("Node has been removed.");
      }
      
      if (pathName.equals("/"))
      { return true;
      }

      if (pathName.charAt(0) != '/')
      { return nodeExists(StringUtil.tokenize(pathName,"/"),0);
      }
    }

    return _root.nodeExists(StringUtil.tokenize(pathName.substring(1), "/"),0);
  }

  boolean nodeExists(String[] path,int index)
    throws BackingStoreException
  {
    synchronized (_lock)
    {
      String name=path[index];
      if (name==null || name.equals(""))
      { throw new IllegalArgumentException("Consecutive slashes in path");
      }
      ProxyPreferencesNode child=
        (ProxyPreferencesNode) _childCache.get(name);
      if (child==null)
      { child = getChild(name);
      }
      if (child==null)
      { return false;
      }
      index++;
      if (index==path.length)
      { return true;
      }
      return child.nodeExists(path,index);
    }
  }

  Object getLock()
  { return _lock;
  }

  void removeNodeFromCache(String name)
  { _childCache.remove(name);
  }

  private void cacheAllChildren()
    throws BackingStoreException
  {
    String[] childNames=childrenNames();
    for (int i=0;i<childNames.length;i++)
    { 
      if (!_childCache.containsKey(childNames[i]))
      { _childCache.put(childNames[i],childSpi(childNames[i]));
      }
    }
  }

  public void removeNode() throws BackingStoreException
  { 
    if (this==_root)
    { throw new UnsupportedOperationException("Can't remove the root node");
    }

    synchronized(_parent.getLock())
    { 
      removeNodeRecursive();
      _parent.removeNodeFromCache(_name);
    }
  }

  void removeNodeRecursive()
    throws BackingStoreException
  {
    synchronized(_lock)
    {
      if (_removed)
      { throw new IllegalStateException("Node already removed.");
      }

      cacheAllChildren();
      Iterator i=_childCache.values().iterator();
      while (i.hasNext())
      { ((ProxyPreferencesNode) i.next()).removeNodeRecursive();
      }
      _childCache.clear();

      _delegate.removeNode();
      _removed=true;
    }
  }

  public String name()
  { return _name;
  }

  public String absolutePath()
  { return _absolutePath;
  }

  public boolean isUserNode()
  { return _delegate.isUserNode();
  }

  public String toString()
  { return _delegate.toString();
  }

  public void flush() throws BackingStoreException
  { _delegate.flush();
  }

  public void sync() throws BackingStoreException
  { _delegate.sync();
  }

  public void addPreferenceChangeListener(PreferenceChangeListener pcl)
  { 
    _delegate.addPreferenceChangeListener(pcl);
    if (_default!=null)
    { _default.addPreferenceChangeListener(pcl);
    }
  }

  public void removePreferenceChangeListener(PreferenceChangeListener pcl)
  { 
    _delegate.removePreferenceChangeListener(pcl);
    if (_default!=null)
    { _default.removePreferenceChangeListener(pcl);
    }
  }

  public void addNodeChangeListener(NodeChangeListener ncl)
  { 
    _delegate.addNodeChangeListener(ncl);
    if (_default!=null)
    { _default.addNodeChangeListener(ncl);
    }
  }

  public void removeNodeChangeListener(NodeChangeListener ncl)
  {
    _delegate.removeNodeChangeListener(ncl);
    if (_default!=null)
    { _default.removeNodeChangeListener(ncl);
    }
  }

  public void exportNode(OutputStream os)
    throws IOException, BackingStoreException
  { _delegate.exportNode(os);
  }

  public void exportSubtree(OutputStream os)
    throws IOException, BackingStoreException
  { _delegate.exportSubtree(os);
  }
  

}
