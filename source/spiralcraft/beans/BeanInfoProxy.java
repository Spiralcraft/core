package spiralcraft.beans;

import java.beans.BeanInfo;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;

import java.awt.Image;

/**
 * Base class for decorating BeanInfo objects with additional functionality
 */
public class BeanInfoProxy
  implements BeanInfo
{
  protected final BeanInfo beanInfo;

  public BeanInfoProxy(BeanInfo beanInfo)
  { this.beanInfo=beanInfo;
  }

  public BeanInfo[] getAdditionalBeanInfo()
  { return beanInfo.getAdditionalBeanInfo();
  }

  public BeanDescriptor getBeanDescriptor()
  { return beanInfo.getBeanDescriptor();
  }

  public int getDefaultEventIndex()
  { return beanInfo.getDefaultEventIndex();
  }
  
  public int getDefaultPropertyIndex()
  { return beanInfo.getDefaultPropertyIndex();
  }

  public EventSetDescriptor[] getEventSetDescriptors()
  { return beanInfo.getEventSetDescriptors();
  }

  public Image getIcon(int iconKind)
  { return beanInfo.getIcon(iconKind);
  }

  public MethodDescriptor[] getMethodDescriptors()
  { return beanInfo.getMethodDescriptors();
  }

  public PropertyDescriptor[] getPropertyDescriptors()
  { return beanInfo.getPropertyDescriptors();
  }
}
