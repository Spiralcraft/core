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
  public void execMain(String[] args)
    throws
      IOException
      ,ClassNotFoundException
      ,NoSuchMethodException
      ,IllegalAccessException
      ,InvocationTargetException
  {
    _classLoader.resolveLibrariesForClass(args[0]);
    Class clazz=_classLoader.loadClass(args[0]);
    Method mainMethod=clazz.getMethod("main",new Class[] {String[].class});
    String[] newArgs=new String[args.length-1];
    System.arraycopy(args,1,newArgs,0,newArgs.length);
    mainMethod.invoke(null,new Object[] {newArgs});
  }

  
  

}
