package spiralcraft.lang;

/**
 * A Focus where the subject is a java Bean
 */
public class BeanFocus
  extends DefaultFocus
{
  
  public BeanFocus()
  {
  }
  
  public BeanFocus(Object bean)
    throws BindException
  { setBean(bean);
  }
  
  public void setBean(Object bean)
    throws BindException
  { setSubject(OpticFactory.getInstance().box(bean));
  }
}
