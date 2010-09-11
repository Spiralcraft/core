package spiralcraft.lang.spi;

import java.util.concurrent.atomic.AtomicLong;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.Channel;
import spiralcraft.time.Clock;

public class CachedChannel<T>
  extends SourcedChannel<T,T>
{

  private T value;
  private final AtomicLong lastRefresh
    =new AtomicLong(0);
  
  
  public CachedChannel(Channel<T> source)
  { super(source.getReflector(), source);
  }

  protected boolean isStale()
  {
    
    if (lastRefresh.get()==0)
    { return true;
    }
    return false;
  }
  
  public void refresh()
  { 
    value=source.get();
    lastRefresh.set(Clock.instance().approxTimeMillis());
  }
  
  @Override
  protected T retrieve()
  { 
    if (isStale())
    { refresh();
    }
    return value;
  }

  @Override
  protected boolean store(
    Object val)
    throws AccessException
  { return false;
  }

}
