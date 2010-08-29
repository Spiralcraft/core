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

import java.net.URI;
import java.util.Map;

/**
 * An interface which resolves namespace prefixes to namespace URIs. Used to
 *   share namespace mappings defined in resources (ie. XML, webui) with any
 *   consumer, and allow for programmatic control of available namespaces.
 * 
 * @author mike
 */
public interface PrefixResolver
{

  /**
   * Resolve a prefix to a namespace URI. By convention, the empty string
   *   represents the "default" namespace URI.
   * 
   * @param prefix
   * @return the URI mapped to the prefix, or null if no URI is mapped
   */
  URI resolvePrefix(String prefix);
  
  Map<String,URI> computeMappings();
}
