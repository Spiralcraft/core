//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.io.message;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>A Message is a sequence of bytes optionally associated with some 
 *   metadata.
 * </p>
 * 
 * <p>Once constructed, an individual Message object is immutable and will
 *   will always provide identical content and metadata. The InputStream
 *   provided by the Message may be read an unlimited number of times.
 * </p>
 * 
 * @author mike
 *
 */
public interface Message<MD extends Metadata>
{
  public MD getMetadata();
  
  public InputStream getInputStream()
    throws IOException;
  
  
}
