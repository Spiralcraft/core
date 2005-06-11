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
package spiralcraft.util;

/**
 * Generic 'command line' argument handling. Command line arguments
 *   fall into 2 categories, standard arguments and option flags. Option
 *   flags begin with an option indicator.
 */
public abstract class Arguments
{
  private int _pos=0;
  private String[] _args;

  /**
   * Process the specified arguments
   */
  public void process(String[] args,char optionIndicator)
  {
    _args=args;

    while (hasMoreArguments())
    { 
      String arg=_args[_pos++];
      if (arg.charAt(0)==optionIndicator)
      { 
        if (!processOption(arg.substring(1).intern()))
        { 
          throw new IllegalArgumentException
            ("Unknown option "+arg);
        }
      }
      else
      { 
        if (!processArgument(arg.intern()))
        {
          throw new IllegalArgumentException
            ("Unknown argument "+arg);
        }
      }
    }
        
  }

  public boolean hasMoreArguments()
  { return _pos<_args.length;
  }

  public String nextArgument()
  { return _args[_pos++];
  }

  /**
   * Subclass should process an option, and return true
   *   if the option was recognized. String will be 'interned'
   *   for convenience using == as a comparator.
   */
  protected boolean processOption(String option)
  { return false;
  }
  
  /**
   * Subclass should process a stand-alone argument, and return
   *   true if the argument was recognized.
   */
  protected boolean processArgument(String argument)
  { return false;
  }

}
