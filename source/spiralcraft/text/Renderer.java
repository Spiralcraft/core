//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.text;

import java.io.IOException;
import java.io.Writer;



/**
 * <p>A means to render streaming text in a concurrent context
 * </p>
 * 
 * @author mike
 *
 */
public interface Renderer
{

  /**
   * <p>Render markup to a Writer
   * </p>
   * 
   * @param writer
   * @throws IOException
   */
  public void render(Writer writer)
    throws IOException;

}
