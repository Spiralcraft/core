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
package spiralcraft.data.flatfile;

import java.io.IOException;

import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.lang.Contextual;

/**
 * Turns flatfile records in arbitrary formats into data
 * 
 * @author mike
 *
 * @param <T>
 */
public interface RecordFormat
  extends Contextual
{


  public Type<?> getType();
  
  public byte[] formatHeader()
    throws IOException;
  
  public byte[] format(Tuple data)
    throws IOException;
  
  public void parse(byte[] record,Tuple target)
    throws ParseException,IOException;
  
}
