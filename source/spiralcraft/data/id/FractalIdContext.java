//
package spiralcraft.data.id;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * Provides fractal ids to the code that runs within it: a timebase, a
 *   minter and the {@link FractalSequence} that mints under them.
 *
 * <p>The context is provided by the environment the code runs in, so the
 *   code does not need to know what kind of minter it is. It is a
 *   thread-local instance, pushed at a context boundary in a server and
 *   reached through {@link #instance()}; a thread started inside a push
 *   sees the pushed context. When nothing is pushed, the instance is the
 *   system context, with the timebase {@link #DEFAULT_TIMEBASE} and the
 *   development minter {@link #DEFAULT_MINTER}.
 * </p>
 *
 * <code>
 *   FractalIdContext.pushInstance(context);
 *   try
 *   { doWork();
 *   }
 *   finally
 *   { FractalIdContext.popInstance();
 *   }
 * </code>
 */
public class FractalIdContext
{
  /** The shipped timebase: base 10, ticks of 1 to 32 ms, origin 2026-01-01 */
  public static final String DEFAULT_TIMEBASE="A76A79869XCFV2";

  /** The minter of an unconfigured process: development */
  public static final String DEFAULT_MINTER="D0";

  private static final FractalIdContext SYSTEM_CONTEXT
    =new FractalIdContext(DEFAULT_TIMEBASE,DEFAULT_MINTER);

  private static final ThreadLocalStack<FractalIdContext> _INSTANCE
    =new ThreadLocalStack<FractalIdContext>(true)
    {
      @Override
      public FractalIdContext defaultValue()
      { return SYSTEM_CONTEXT;
      }
    };

  /** The context of the current thread */
  public static final FractalIdContext instance()
  { return _INSTANCE.get();
  }

  /**
   * Make a context the instance for the current thread, until
   *   {@link #popInstance()}, which must be called in a finally block.
   */
  public static final void pushInstance(FractalIdContext context)
  { _INSTANCE.push(context);
  }

  /** Restore the instance before the last push */
  public static final void popInstance()
  { _INSTANCE.pop();
  }

  private final FractalSequence sequence;

  /**
   * @param timebaseSpec A timebase spec, see {@link FractalTimebase}
   * @param minter The minter id, a rubric then an expanding-sequence value
   */
  public FractalIdContext(String timebaseSpec,String minter)
  { this(FractalTimebase.parse(timebaseSpec),minter);
  }

  public FractalIdContext(FractalTimebase timebase,String minter)
  { this.sequence=new FractalSequence(timebase,minter);
  }

  public FractalSequence getSequence()
  { return sequence;
  }

  public FractalTimebase getTimebase()
  { return sequence.getTimebase();
  }

  public String getMinter()
  { return sequence.getMinter();
  }

  /** Mint an id */
  public String nextId()
  { return sequence.nextId();
  }

  /** Mint ids, in order */
  public String[] nextIds(int count)
  { return sequence.nextIds(count);
  }

  /** Issue a prefix for another party to extend */
  public String nextPrefix()
  { return sequence.nextPrefix();
  }

  /** A block of ids under a new prefix */
  public String[] nextBlock(int count)
  { return sequence.nextBlock(count);
  }

  /** Decompose an id minted under this context's timebase */
  public FractalId parse(String id)
  { return FractalId.parse(id,sequence.getTimebase());
  }

  /** Explain this context's timebase */
  public FractalTimebaseParts explainTimebase()
  { return sequence.getTimebase().explain();
  }
}
