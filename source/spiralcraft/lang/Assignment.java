//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.lang;

import spiralcraft.lang.parser.AssignmentNode;

/**
 * <p>Represents the assignment of the value obtained from the source Expression
 *   to the target Expression. 
 * </p>
 * 
 * @author mike
 *
 */
public class Assignment<T>
{
  
  public static final Setter<?>[] 
    bindArray(Assignment<?>[] assignments,Focus<?> focus)
    throws BindException
  {
    if (assignments!=null)
    {
      Setter<?>[] setters=new Setter<?>[assignments.length];
      int i=0;
      for (Assignment<?> assignment: assignments)
      { setters[i++]=assignment.bind(focus);
      }
      return setters;
    }
    return null;
  }
  
  private Expression<? extends T> source;
  private Expression<T> target;
  private boolean debug;
  
  public Assignment()
  { }
  
  public Assignment(String assignmentExpression)
    throws ParseException
  { source=Expression.<T>parse(assignmentExpression);
  }
  
  public Assignment(Expression<T> target,Expression<? extends T> source)
  { 
    this.source=source;
    this.target=target;
  
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void setSource(Expression<? extends T> source)
  { this.source=source;
  }

  public void setTarget(Expression<T> target)
  { this.target=target;
  }
  
  public Expression<? extends T> getSource()
  { return source;
  }

  public Expression<T> getTarget()
  { return target;
  }
  
  @SuppressWarnings("unchecked")
  public Setter<T> bind(Focus<?> focus)
    throws BindException
  {
    Channel<? extends T> sourceChannel=focus.bind(source);
    Channel<T> targetChannel=null;
    if (target!=null)
    { targetChannel=focus.bind(target);
    }
    else if (sourceChannel instanceof AssignmentNode.AssignmentChannel)
    { 
      targetChannel
        =((AssignmentNode.AssignmentChannel) sourceChannel).targetChannel;
      sourceChannel
        =((AssignmentNode.AssignmentChannel) sourceChannel).sourceChannel;
    }
    Setter setter=new Setter<T>(sourceChannel,targetChannel);
    if (debug)
    { setter.setDebug(true);
    }
    return setter;
  }
}
