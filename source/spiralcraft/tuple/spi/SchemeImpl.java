package spiralcraft.tuple.spi;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A basic implementation of a Scheme for the manual construction of Schemes 
 *  (progammatically or via an Assembly)
 */
public class SchemeImpl
  implements Scheme
{
  private List _fields;
  
  public SchemeImpl()
  { }
  
  /**
   * Copy constructor
   */
  public SchemeImpl(Scheme scheme)
  {
    List fields=scheme.getFields();
    List newFields=new ArrayList(fields.size());

    Iterator it=fields.iterator();
    while (it.hasNext())
    { 
      Field field=(Field) it.next();
      newFields.add(new FieldImpl(field)); 
    }
    setFields(newFields);
  }
  
  public List getFields()
  { return _fields;
  }
  
  public void setFields(List fields)
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
