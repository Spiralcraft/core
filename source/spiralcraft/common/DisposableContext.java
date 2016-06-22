package spiralcraft.common;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import spiralcraft.util.thread.ThreadLocalStack;


/**
 * Notify registered Disposables when they are no longer needed so they can
 *   release resources.
 * 
 * @author mike
 *
 */
public class DisposableContext
{
  
  private static ThreadLocalStack<DisposableContext> local
    =new ThreadLocalStack<>(true);
  
  public static void register(Disposable d)
  {
    if (local.size()>0)
    { 
      local.get().add(d);
      // System.err.println("Registered "+d);
    }
    else
    { // System.err.println("Not registering "+d);
    
    }
  }
  
  public  static void push()
  {
    local.push(new DisposableContext());
  }
  
  public static void pop()
  {
    local.get().dispose();
    local.pop();
  }
  
  private LinkedList<WeakReference<Disposable>> list=new LinkedList<>();

  private void add(Disposable d)
  { list.addFirst(new WeakReference<Disposable>(d));
  }
  
  public void dispose()
  {
    while (!list.isEmpty())
    {
      WeakReference<Disposable> dwr = list.removeFirst();
      Disposable d=dwr.get();
      if (d!=null)
      { 
        try
        { 
          System.err.println("Disposing "+d);
          d.dispose();
        }
        catch (Throwable t)
        { t.printStackTrace();
        }
        d=null;
      }
    }
  }
}