package spiralcraft.beans;

import java.util.HashMap;

import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;

public class MappedBeanInfo
  extends BeanInfoProxy
{
  private HashMap _propertyMap;

  public MappedBeanInfo(BeanInfo binf)
  { 
    super(binf);
    mapProperties();
  }

  private void mapProperties()
  {
    PropertyDescriptor[] props
      =beanInfo.getPropertyDescriptors();

    _propertyMap=new HashMap(props.length);

    for (int i=0;i<props.length;i++)
    { _propertyMap.put(props[i].getName(),props[i]);
    }
  }

  public PropertyDescriptor findProperty(String name)
  { return (PropertyDescriptor) _propertyMap.get(name);
  }

}
