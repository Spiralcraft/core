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

  protected boolean hasMoreArguments()
  { return _pos<_args.length;
  }

  protected String nextArgument()
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
