package spiralcraft.loader;

import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.lang.NoSuchMethodException;

/**
 * Encapsulates an Application, its state and its resources
 */
public class ApplicationEnvironment
{
  private LibraryClassLoader _classLoader;
  private ApplicationManager _applicationManager;

  public ApplicationEnvironment(ApplicationManager manager)
  { 
    _applicationManager=manager;
    _classLoader=new LibraryClassLoader(manager.getLibraryCatalog());
    
  }

  /**
   * Execute the main method of the class specified in args[0],
   *   with the remaning parameters in args[].
   */
  public int execMain(String[] args)
  {
    ClassLoader oldLoader=Thread.currentThread().getContextClassLoader();
    try
    {
      _classLoader.resolveLibrariesForClass(args[0]);
      Class clazz=_classLoader.loadClass(args[0]);
      Method mainMethod=clazz.getMethod("main",new Class[] {String[].class});
      String[] newArgs=new String[args.length-1];
      System.arraycopy(args,1,newArgs,0,newArgs.length);
      Thread.currentThread().setContextClassLoader(_classLoader);
      mainMethod.invoke(null,new Object[] {newArgs});
      return 0;
    }
    catch (InvocationTargetException x)
    {
      if (x.getTargetException() instanceof RuntimeException)
      { throw (RuntimeException) x.getTargetException();
      }
      else
      { 
        x.printStackTrace();
        return 1;
      }
    }
    catch (Exception x)
    { 
      x.printStackTrace();
      return 1;
    }
    finally
    { Thread.currentThread().setContextClassLoader(oldLoader);
    }
  }

  
  

}
