package spiralcraft.common;

public class DisposableReference<T>
  implements Disposable
{
  private T referent;
  
  public DisposableReference(T referent)
  { 
    this.referent=referent;
    DisposableContext.register(this);
  }
  
  public void dispose()
  { referent=null;
  }
  
  public T get()
  { return referent;
  }

}
