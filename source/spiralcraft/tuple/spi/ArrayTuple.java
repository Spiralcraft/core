package spiralcraft.tuple.spi;

import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.BufferConflictException;

/**
 * A Tuple which stores its data in an Array
 */
public class ArrayTuple
  implements Tuple
{
  private final Scheme _scheme;
  private final Object[] _data;
  private Buffer _buffer;
  private boolean _deleted;
  private ArrayTuple _nextVersion;
  
  public ArrayTuple(Scheme scheme)
  { 
    _scheme=scheme;
    _data=new Object[scheme.getFields().size()];
    _buffer=new Buffer();
  }
  
  public ArrayTuple(ArrayTuple original)
  { 
    this(original.getScheme());
    _buffer.original=original;
  }
  
  public Scheme getScheme()
  { return _scheme;
  }
  
  public Object getID()
  { return this;
  }
  
  public Object get(Field field)
  { return _data[field.getIndex()];
  }
  
  public synchronized void set(Field field,Object value)
  { 
    assertBuffer();
    _data[field.getIndex()]=value;
  }

  public synchronized Tuple commitBuffer()
    throws BufferConflictException
  { 
    assertBuffer();
    if (_buffer.original!=null)
    { _buffer.original.newVersion(this);
    }
    _buffer=null;
    return this;
  }

  public synchronized Tuple createBuffer()
  { 
    if (_deleted)
    { throw new IllegalStateException("Tuple has been deleted");
    }
    if (_buffer!=null)
    { throw new IllegalStateException("Tuple is already a buffer");
    }
    return new ArrayTuple(this);
  }
  
  public boolean isBuffer()
  { return _buffer!=null;
  }
  
  public synchronized Tuple currentVersion()
  { 
    if (_deleted)
    { return null;
    }
    
    if (_nextVersion!=null)
    { return _nextVersion.currentVersion();
    }
    return this;
  }
  
  public Tuple nextVersion()
  { return _nextVersion;
  }
  
  public synchronized Tuple original()
  {
    if (_buffer!=null)
    { return _buffer.original;
    }
    return null;
  }
  
  public synchronized void delete()
    throws BufferConflictException
  { 
    assertBuffer();
    _deleted=true;
    commitBuffer();
  }
  
  public boolean isDeleted()
  { return _deleted;
  }

  synchronized void newVersion(ArrayTuple nextVersion)
    throws BufferConflictException
  {
    if (_nextVersion!=null)
    { throw new BufferConflictException(_nextVersion);
    }
    _nextVersion=nextVersion;
  }
  
  private void assertBuffer()
  { 
    if (_buffer==null)
    { throw new IllegalStateException("Tuple is not buffered");
    }
  }
  
}

class Buffer
{
  
  ArrayTuple original;
 
}
