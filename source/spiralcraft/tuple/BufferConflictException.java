package spiralcraft.tuple;

public class BufferConflictException
  extends Exception
{
  private final Tuple _conflictedVersion;
  
  public BufferConflictException(Tuple conflictedVersion)
  { _conflictedVersion=conflictedVersion;
  }

  /**
   * Return the version of the Tuple that was committed before the
   *   Tuple generating the exception. 
   */
  public Tuple getConflictedVersion()
  { return _conflictedVersion;
  }
}
