package spiralcraft.tuple.spi;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.FieldList;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A basic, efficient implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  private FieldList _fields;
  
  public SchemeImpl()
  { }
  
  /**
   * Copy constructor
   */
  public SchemeImpl(Scheme scheme)
  {
    FieldList fields=scheme.getFields();
    FieldList newFields=new FieldListImpl(fields.size());

    Iterator it=fields.iterator();
    while (it.hasNext())
    { 
      Field field=(Field) it.next();
      newFields.add(new FieldImpl(field)); 
    }
    setFields(newFields);
  }
  
  public FieldList getFields()
  { return _fields;
  }
  
  public void setFields(FieldList fields)
  { 
    int i=0;
    for (Iterator it=fields.iterator();it.hasNext();)
    { 
      FieldImpl field=(FieldImpl) it.next();
      field.setIndex(i++);
      field.setScheme(this);
    }
    _fields=fields;
  }
}
