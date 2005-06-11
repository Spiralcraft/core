//
// Copyright (c) 1998,2005 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.exec;

import spiralcraft.util.ArrayUtil;
import spiralcraft.util.Arguments;

/**
 * Runs an executable class
 */
public class ClassExecutor
  extends Executor
{
  private String _class;
  
  public static void main(String[] args)
    throws ExecutionException
  { new ClassExecutor().execute(args);
  }
  
  /**
   * Locate and execute a class that implements the Executable interface.
   */
  public void execute(String[] args)
    throws ExecutionException
  {
    try
    {
      processArguments(args);
  
      if (_class==null)
      { throw new IllegalArgumentException("No class specified. Nothing to execute.");
      }
      
      Class clazz=Thread.currentThread().getContextClassLoader().loadClass(_class);
      if (Executable.class.isAssignableFrom(clazz))
      {
        Executable executable=(Executable) clazz.newInstance(); 
        try
        { executable.execute(_context,_arguments);
        }
        catch (Throwable x)
        { throw new ExecutionTargetException(x);
        }
      }
      else
      { 
        throw new IllegalArgumentException
          ("Class "+clazz.getName()+" is not an Executable");
      }
    }
    catch (ClassNotFoundException x)
    { throw new IllegalArgumentException("Class "+_class+" not found");
    }
    catch (InstantiationException x)
    { throw new ExecutionTargetException(x);
    }
    catch (IllegalAccessException x)
    { throw new ExecutionTargetException(x);
    }
    
  }
  
  /**
   * Process arguments. The first non-option argument is treated as the URI
   *   of the target to invoke, if the URI hasn't been preset programmatically.
   *   The remaining arguments are passed through the the Executable.
   */
  private void processArguments(String[] args)
  {
    new Arguments()
    {
      public boolean processArgument(String argument)
      { 
        if (_class==null)
        { _class=argument;
        }
        else
        { _arguments=(String[]) ArrayUtil.append(_arguments,argument);
        }
        return true;
      }

      public boolean processOption(String option)
      { 
        if (_class==null)
        { return false;
        }
        else
        { _arguments=(String[]) ArrayUtil.append(_arguments,"-"+option);
        }
        return true;
      }

    }.process(args,'-');
  }    

  
}
