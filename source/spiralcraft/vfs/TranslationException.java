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
package spiralcraft.vfs;

import java.net.URI;
import java.io.IOException;

/**
 * <P>Occurs when a Translator encounters an unexpected resource format 
 *   and cannot complete the Translation.
 */
public class TranslationException
  extends IOException
{

  private static final long serialVersionUID = 1L;

  public TranslationException(URI uri,String message)
  { super(message+": "+uri.toString());
  }
}
