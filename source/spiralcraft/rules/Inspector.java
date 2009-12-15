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
package spiralcraft.rules;

import java.util.ArrayList;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.ThreadLocalChannel;

/**
 * <p>Inspects an object for compliance against a RuleSet within an
 *   application Focus chain context and reports Rule Violations.
 * </p>
 * 
 * @author mike
 *
 */
public class Inspector<Tcontext,Tsubject>
{
  private RuleSet<Tcontext,Tsubject> ruleSet;
  private ThreadLocalChannel<Tsubject> subjectChannel;
  private Focus<Tsubject> localFocus;
  private final Channel<Violation<Tsubject>>[] ruleChannels;
  
  @SuppressWarnings("unchecked") // Generic array creation
  Inspector
    (RuleSet<Tcontext,Tsubject> ruleSet
    ,Reflector<Tsubject> subjectReflector
    ,Focus<?> contextFocus
    )
    throws BindException
  {
    this.ruleSet=ruleSet;
    subjectChannel=new ThreadLocalChannel<Tsubject>(subjectReflector);
    localFocus=contextFocus.telescope(subjectChannel);
    
    ArrayList<Channel<Violation<Tsubject>>> ruleChannels
      =new ArrayList<Channel<Violation<Tsubject>>>();

    for (Rule rule: this.ruleSet)
    { ruleChannels.add(rule.bindChannel(subjectChannel,localFocus,null));
    }
    this.ruleChannels
      =ruleChannels.toArray
        (new Channel[ruleChannels.size()]);
  }
  
  @SuppressWarnings("unchecked") // Generic array creation
  public Violation<Tsubject>[] inspect(Tsubject subject)
  {
    subjectChannel.push(subject);
    try
    { 
      ArrayList<Violation<Tsubject>> list=null;
      for (Channel<Violation<Tsubject>> channel: ruleChannels)
      {
        Violation<Tsubject> v=channel.get();
        if (v!=null)
        {
          v.setSubject(subject);
          if (list==null)
          { list=new ArrayList<Violation<Tsubject>>();
          }
          list.add(v);
        }
      }
      if (list!=null)
      { return list.toArray(new Violation[list.size()]);
      }
      else
      { return null;
      }        
    }
    finally
    { subjectChannel.pop();
    }
  }
  
  
}
