package spiralcraft.tuple.spi;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.FieldList;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.net.URI;

/**
 * A basic, efficient implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  private FieldList _fields;
  private URI _uri;
  
  public SchemeImpl()
  { }
  
  
  /**
   * Copy constructor
   */
  public SchemeImpl(Scheme scheme)
  {
    _uri=scheme.getURI();
    setFields(new FieldListImpl(scheme.getFields()));
  }
  
  public URI getURI()
  { return _uri;
  }
  
  public void setURI(URI uri)
  { _uri=uri;
  }

  public FieldList getFields()
  { return _fields;
  }

  /**
   * Specify the FieldList for this Scheme.
   *
   * Fields in the list will be indexed and bound to this Scheme.
   */
  protected void setFields(FieldListImpl<FieldImpl> fields)
  { 
    int i=0;
    for (FieldImpl field: fields)
    { 
      field.setIndex(i++);
      field.setScheme(this);
    }
    _fields=fields;
  }
}
