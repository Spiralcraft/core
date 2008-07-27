//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.io;

import java.io.OutputStream;

/**
 * An OutputStream that discards its output
 */
public class NullOutputStream
  extends OutputStream
{
  @Override
  public void write(int val)
  { }
  
  @Override
  public void write(byte[] bytes)
  { }
  
  @Override
  public void write(byte[] bytes,int start,int len)
  { }
}
