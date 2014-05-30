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
import spiralcraft.text.MessageFormat;

public class Trace
  extends AbstractComponent
{
  private Channel<?> subject;
  private MessageFormat tagMessage;
  
  /**
   * The message to display before trace log entries.
   * 
   * @param tagMessage
   */
  public void setTagMessage(MessageFormat tagMessage)
  { 
    this.removeParentContextual(this.tagMessage);
    this.tagMessage=tagMessage;
    this.addParentContextual(this.tagMessage);
  }
  
  @Override
  protected void addHandlers()
  { addHandler(new TraceHandler());
  }
  
  @Override
  public Focus<?> bindImports(Focus<?> focus) 
    throws ContextualException
  {
    String prefix=tagMessage!=null?tagMessage.render():Integer.toString(System.identityHashCode(this));
    this.subject=focus.getSubject();
    log.fine(prefix+": "+getDeclarationInfo()+": Import subject is "+subject);
    return super.bindImports(focus);
  }

  @Override
  public Focus<?> bindExports(Focus<?> focus) 
    throws ContextualException
  { 
    String prefix=tagMessage!=null?tagMessage.render():Integer.toString(System.identityHashCode(this));
    focus=super.bindExports(focus).chain(subject);
    log.fine(prefix+": "+getDeclarationInfo()+": Export focus is "+focus);
    return focus;
  }

}

