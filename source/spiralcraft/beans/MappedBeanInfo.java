package spiralcraft.beans;

import java.util.HashMap;

import java.beans.PropertyDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;

import java.lang.reflect.Field;

public class MappedBeanInfo
  extends BeanInfoProxy
{
  private HashMap _propertyMap;
  private HashMap _fieldMap;
  private Field[] _fields;
  private EventSetDescriptor _propertyChangeEventSetDescriptor=null;

  public MappedBeanInfo(BeanInfo binf)
  { 
    super(binf);
    mapProperties();
    mapFields();

    EventSetDescriptor[] events
      =beanInfo.getEventSetDescriptors();
    
    for (int i=0;i<events.length;i++)
    { 
      if (events[i].getListenerType()==PropertyChangeListener.class)
      { 
        _propertyChangeEventSetDescriptor=events[i];
        break;
      }
    }
  }
  
  public EventSetDescriptor getPropertyChangeEventSetDescriptor()
  { return _propertyChangeEventSetDescriptor;
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

  private void mapFields()
  {
    _fields=getBeanDescriptor().getBeanClass().getFields();
    _fieldMap=new HashMap(_fields.length);

    for (int i=0;i<_fields.length;i++)
    { _fieldMap.put(_fields[i].getName(),_fields[i]);
    }
  }

  public PropertyDescriptor findProperty(String name)
  { return (PropertyDescriptor) _propertyMap.get(name);
  }

  public Field findField(String name)
  { return (Field) _fieldMap.get(name);
  }
}
