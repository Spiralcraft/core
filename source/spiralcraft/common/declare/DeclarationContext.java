//
// Copyright (c) 2008,2009 Michael Toth
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
package spiralcraft.common.declare;

import java.net.URI;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>Provides a mechanism for storing information about the context of a 
 *   declaration in the thread local context.
 * </p>
 * 
 * <p>Code that handles declared constructs push and pop the current declaration
 *   info into the stack when calling other components. 
 * </p>
 * 
 * <p>Components use the declaration context info to provide declaration source
 *   code information back to the user
 * </p>
 * 
 * @author mike
 */
public class DeclarationContext
{
  private static final ThreadLocalStack<URI> stack
    =new ThreadLocalStack<URI>();
  
  /**
   * @return The currently active set of prefix mappings
   */
  public static Object getDeclarationInfo()
  { return stack.get();
  }
  
  /**
   * Push declaration info onto the stack
   * 
   * @param resolver
   */
  public static void push(URI declarationInfo)
  { stack.push(declarationInfo);
  }
  
  /**
   * Remove the active declaration info from the stack and
   *   restore the previous declaration info
   */
  public static void pop()
  { stack.pop();
  }
  
}
