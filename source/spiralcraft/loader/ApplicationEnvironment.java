package spiralcraft.loader;

import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.lang.NoSuchMethodException;

import spiralcraft.util.Arguments;
import spiralcraft.util.StringUtil;
import spiralcraft.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Manages the execution of an Application by resolving the appropriate set of class libraries
 *   and loading the 'main' class in its own classloader.
 */
public class ApplicationEnvironment
{
  private LibraryClassLoader _classLoader;
  private ApplicationManager _applicationManager;
  private String _mainClass;
  private ArrayList _mainArguments;
  private ArrayList _modules=new ArrayList();

  public ApplicationEnvironment(ApplicationManager manager)
  { 
    _applicationManager=manager;
    _classLoader=new LibraryClassLoader(manager.getLibraryCatalog());
    
  }

  /**
   * Parse relevent information from the arguments and execute the main method of the specified 
   *   target class passing along the additional arguments.
   */
  public void exec(String[] args)
    throws InvocationTargetException
          ,ClassNotFoundException
          ,IOException
          ,NoSuchMethodException
          ,IllegalAccessException
  { 
    args=expandAliases(args);

    new Arguments()
    {
      public boolean processArgument(String argument)
      { 
        if (_mainClass==null)
        { 
          _mainClass=argument;
          _mainArguments=new ArrayList();
        }
        else
        { _mainArguments.add(argument);
        }
        return true;
      }

      public boolean processOption(String option)
      { 
        if (_mainClass==null)
        {
          if (option=="module")
          { _modules.add(nextArgument());
          }
          else
          { return false;
          }
        }
        else
        { _mainArguments.add("-"+option);
        }
        return true;
      }

    }.process(args,'-');

    if (_mainClass==null)
    { 
      System.err.println("Nothing to execute. Please specify a class.");
      return;
    }

    if (_modules.size()>0)
    {
      Iterator it=_modules.iterator();
      while (it.hasNext())
      { _classLoader.addModule((String) it.next());
      }
    }
    else
    { _classLoader.resolveLibrariesForClass(_mainClass);
    }

    Class clazz=_classLoader.loadClass(_mainClass);
    Method mainMethod=clazz.getMethod("main",new Class[] {String[].class});
    String[] newArgs=new String[_mainArguments.size()];
    _mainArguments.toArray(newArgs);
    
    ClassLoader oldLoader=Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(_classLoader);
      mainMethod.invoke(null,new Object[] {newArgs});
    }
    finally
    { Thread.currentThread().setContextClassLoader(oldLoader);
    }
  }

  private String[] expandAliases(String[] args)
  { 
    Aliases aliases=_applicationManager.getAliases();
    if (aliases!=null)
    { return aliases.expand(args);
    }
    else
    { return args;
    }
  }
}
