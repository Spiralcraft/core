package spiralcraft.util;

import java.util.Iterator;
import java.util.function.Function;

public class FunctionIterator<X,Y>
  implements Iterator<Y>
{
  private final Function<X,Y> f;
  private final Iterator<X> in;

  public FunctionIterator(Iterator<X> in,Function<X,Y> f)
  { 
    this.in=in;
    this.f=f;
  }
  
  @Override
  public boolean hasNext()
  { return in.hasNext();
  }

  @Override
  public Y next()
  { return f.apply(in.next());
  }
}
