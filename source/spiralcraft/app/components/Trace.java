//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.components;

import spiralcraft.app.kit.AbstractComponent;
import spiralcraft.app.kit.TraceHandler;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

public class Trace
  extends AbstractComponent
{
  private Channel<?> subject;
  
  @Override
  protected void addHandlers()
  { addHandler(new TraceHandler());
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focus) 
    throws ContextualException
  {
    
    this.subject=focus.getSubject();
    log.fine("Subject is "+subject);
    return super.bindImports(focus);
  }

  @Override
  public Focus<?> bindExports(Focus<?> focus) 
    throws ContextualException
  { 
    focus=super.bindExports(focus).chain(subject);
    log.fine("Focus is "+focus);
    return focus;
  }

}

