package spiralcraft.exec;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

import java.net.URI;

public class SystemExecutionContext
  extends ExecutionContext
{
  public SystemExecutionContext()
  { ExecutionContext.setInstance(this);
  }
  
  public PrintStream out()
  { return System.out;
  }

  public InputStream in()
  { return System.in;
  }
  
  public PrintStream err()
  { return System.err;
  }
  
  public URI focusURI()
  { return new File(".").toURI();
  }
}
