package spiralcraft.exec;

import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.lang.NoSuchMethodException;

import spiralcraft.util.Arguments;
import spiralcraft.util.StringUtil;
import spiralcraft.util.ArrayUtil;


import spiralcraft.loader.LibraryClassLoader;

import spiralcraft.prefs.XmlPreferencesFactory;

import java.util.prefs.Preferences;

/**
 * Manages the execution of an Application by resolving the appropriate set of class libraries
 *   and loading the 'main' class in its own classloader.
 */
public class ApplicationEnvironment
{
  private LibraryClassLoader _classLoader;
  private ApplicationManager _applicationManager;
  private String _mainClass;
  private String[] _mainArguments=new String[0];
  private String[] _modules;

  public void setApplicationManager(ApplicationManager manager)
  { 
    _applicationManager=manager;
    _classLoader=new LibraryClassLoader(manager.getLibraryCatalog());
    
  }

  public void setCommandLine(String val)
  { _mainArguments=StringUtil.tokenizeCommandLine(val);
  }
  
  public void setMainClass(String val)
  { _mainClass=val;
  }
  
  public void setMainArguments(String[] val)
  { _mainArguments=val;
  }
  
  public void setModules(String[] val)
  { _modules=val;
  }
  
  
  /**
   * Parse relevent information from the arguments and execute the main method of the specified 
   *   target class passing along the additional arguments.
   */
  public void exec(String[] args)
    throws ExecutionTargetException
  { 
    processArguments(args);

    if (_mainClass==null)
    { 
      System.err.println("Nothing to execute. Please specify a class.");
      return;
    }

    try
    {
      if (_modules!=null)
      {
        for (int i=0;i<_modules.length;i++)
        { _classLoader.addModule(_modules[i]);
        }
      }
      else
      { _classLoader.resolveLibrariesForClass(_mainClass);
      }
  
      Class clazz=_classLoader.loadClass(_mainClass);
      Method mainMethod=clazz.getMethod("main",new Class[] {String[].class});
      
      ClassLoader oldLoader=Thread.currentThread().getContextClassLoader();
      try
      {
        Thread.currentThread().setContextClassLoader(_classLoader);
        mainMethod.invoke(null,new Object[] {_mainArguments});
      }
      finally
      { Thread.currentThread().setContextClassLoader(oldLoader);
      }
    }
    catch (InvocationTargetException x)
    { 
      while (x.getTargetException() instanceof InvocationTargetException)
      { x=(InvocationTargetException) x.getTargetException();
      }
      throw new ExecutionTargetException(x.getTargetException());
    }
    catch (Exception x)
    { throw new ExecutionTargetException(x);
    }
      
  }

  private void processArguments(String[] args)
  {
    new Arguments()
    {
      public boolean processArgument(String argument)
      { 
        if (_mainClass==null)
        { _mainClass=argument;
        }
        else
        { _mainArguments=(String[]) ArrayUtil.append(_mainArguments,argument);
        }
        return true;
      }

      public boolean processOption(String option)
      { 
        if (_mainClass==null)
        {
          if (option=="module")
          { _modules=(String[]) ArrayUtil.append(_modules,nextArgument());
          }
          else
          { return false;
          }
        }
        else
        { _mainArguments=(String[]) ArrayUtil.append(_mainArguments,"-"+option);
        }
        return true;
      }

    }.process(args,'-');
  }    
  
}
