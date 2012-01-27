//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.vfs;

import java.io.IOException;


/**
 * <p>Utility functions to handle resources
 * </p>
 * 
 * @author mike
 *
 */
public class ResourceUtil
{

  /**
   * <p>Copy a Resource into a container, generating a unique name from the
   *   supplied baseName if required
   * </p>
   * 
   * @param input
   * @param container
   * @param name
   * @return
   */
  public static Resource addCopy
    (Container container,Resource input,String baseName)
    throws IOException
  {          
    Resource targetResource=container.getChild(baseName);
    while (targetResource.exists())
    {
      baseName=nextUniqueName(baseName);
      targetResource=container.getChild(baseName);
    }
    targetResource.copyFrom(input);
    return targetResource;
  }
   
  /**
   * <p>Generate a unique filename using an algorithm that inserts or increments
   *   a parenthesized integer at the end of the filename but before the
   *   last dotted suffix.
   * </p>
   * 
   * <p>Example: foo.txt, foo(1).txt, foo(2).txt, foo.baz.txt, foo.baz(1).txt
   * </p>
   * 
   * @param filename
   * @return
   */
  public static String nextUniqueName(String filename)
  {
    int dotPos=filename.indexOf('.');
    String prefix=(dotPos>0)?filename.substring(0,dotPos):filename;
    String suffix=(dotPos>0)?filename.substring(dotPos):"";
    
    int num=2;
    if (prefix.endsWith(")"))
    {
      int parenPos=prefix.lastIndexOf("(");
      if (parenPos>-1)
      { 
        String numString=prefix.substring(parenPos+1,prefix.length()-1);
        try
        { 
          num=Integer.parseInt(numString)+1;
          prefix=prefix.substring(0,parenPos).trim();
        }
        catch (NumberFormatException x)
        { // Ignore, last paren does not contain a number
        }
      }
    }
    prefix+="("+num+")";
    return prefix+suffix;
  }
}

