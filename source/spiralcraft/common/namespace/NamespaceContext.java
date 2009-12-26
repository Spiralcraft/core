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
package spiralcraft.common.namespace;

import spiralcraft.util.thread.ThreadLocalStack;

/**
 * <p>Provides a mechanism for storing the set of visible
 *   prefix mappings in a thread local context.
 * </p>
 * 
 * <p>Components that define mappings push and pop their PrefixResolver into
 *   the stack when calling out of their framework. 
 * </p>
 * 
 * <p>Components that use contextual prefix mappings call getPrefixResolver()
 *   to retrieve the currently active prefix mappings.
 * 
 * @author mike
 */
public class NamespaceContext
{
  private static final ThreadLocalStack<PrefixResolver> stack
    =new ThreadLocalStack<PrefixResolver>();
  
  /**
   * @return The currently active set of prefix mappings
   */
  public static PrefixResolver getPrefixResolver()
  { return stack.get();
  }
  
  /**
   * Push a PrefixResolver holding the currently active set of prefix mappings
   *   onto the stack
   * 
   * @param resolver
   */
  public static void push(PrefixResolver resolver)
  { stack.push(resolver);
  }
  
  /**
   * Remove the active PrefixResolver from the stack and
   *   restore the next recent PrefixResolver.
   */
  public static void pop()
  { stack.pop();
  }
  
}
