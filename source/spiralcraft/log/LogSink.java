//
// Copyright (c) 1998,2019 Michael Toth
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
package spiralcraft.log;

import java.io.IOException;

import spiralcraft.app.kit.AbstractComponent;

/**
 * A destination for abstract log messages managed by LogService
 * 
 * @author mike
 *
 */
public abstract class LogSink
  extends AbstractComponent
{
  String name;

  public void setName(String name)
  { this.name=name;
  }
  
  public abstract void write(String formattedMessage)
    throws IOException;
  
}