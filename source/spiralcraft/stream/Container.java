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
package spiralcraft.stream;

import java.io.IOException;

/**
 * Provides access to the compositional/relational aspects of
 *   a Resource- ie. the extent to which a Resource contains or
 *   links to other resources.
 */
public interface Container
{
  /**
   * List all resources associated with this ResourceContainer
   */ 
  Resource[] listContents()
    throws IOException;

  /**
   * List all Resources that are immediate 'descendants' of this
   *   Container in the canonical 'tree' mapping (acyclic directed graph)
   *   of the underlying structure of this Resource scheme.
   *
   * Provides a means to easily perform depth-first traversals without
   *   encountering cycles.
   */
  Resource[] listChildren()
    throws IOException;

  /**
   * List all 'link' (non-child) Resources contained in this Container. 
   */
  Resource[] listLinks()
    throws IOException;

  /**
   * Obtain a child resource with the specified name
   *  (the actual backing resource may not exist until it is written to).
   */
  Resource getChild(String name)
    throws UnresolvableURIException;

  /**
   * Create a new Resource in this container which is a link to the specified
   *  Resource. The functionality is optional, and may not be supported in a
   *  given implementation.
   */
  Resource createLink(String name,Resource resource)
    throws UnresolvableURIException;
  
}
