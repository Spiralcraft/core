package spiralcraft.loader;

import java.io.StringWriter;
import java.io.PrintWriter;

public class Usage
{

  public String toString()
  {
    StringWriter sw=new StringWriter();
    PrintWriter out=new PrintWriter(sw);

    out.println("Usage:");
    out.println("  ... [debug-options] environment-name [args]");
    out.println("");
    out.println("debug-options include:");
    out.println("    -debug");
    out.println("       Output debugging information about the loading process");
    out.println("    -core.source");
    out.println("       The location of the source tree for the spiralcraft-core module");
    out.println("    -core.jar");
    out.println("       The location of the spiralcraft-core jar file");
    out.println(" ");
    out.flush();

    return sw.toString();
  }

}
