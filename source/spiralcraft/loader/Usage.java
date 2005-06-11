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
