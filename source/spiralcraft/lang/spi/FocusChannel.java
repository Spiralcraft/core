package spiralcraft.lang.spi;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

public class FocusChannel<T>
  extends ProxyChannel<T>
{
  private final Focus<?> focus;
  
  public FocusChannel(Channel<T> source,Focus<?> focus)
  {
    super(source);
    this.focus=focus;
  }
  
  public Focus<?> getFocus()
  { return focus;
  }

}
