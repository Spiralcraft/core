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
import java.util.Iterator;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;


/**
 * <p>A collection of rules scoped to some application component.
 * </p>
 * @author mike
 *
 */
public class RuleSet<Tcontext,Tvalue>
  implements Iterable<Rule<Tcontext,Tvalue>>
{
  
  private final Tcontext context;
  
  public RuleSet(Tcontext context)
  { this.context=context;
  }
  
  public Tcontext getContext()
  { return context;
  }
  
  private final ArrayList<Rule<Tcontext,Tvalue>> rules
    =new ArrayList<Rule<Tcontext,Tvalue>>();
  

  @Override
  public Iterator<Rule<Tcontext,Tvalue>> iterator()
  { return rules.iterator();
  }

  public Inspector<Tcontext,Tvalue> 
    bind(Reflector<Tvalue> subjectReflector,Focus<?> context)
    throws BindException
  {
    return new Inspector<Tcontext,Tvalue>(this,subjectReflector,context);
    
  }
  
  public void addRules(Rule<Tcontext,Tvalue> ... rules)
  {
    for (Rule<Tcontext,Tvalue> rule : rules)
    { this.rules.add(rule);
    }
  }
  

  public int getRuleCount()
  { return rules.size();
  }
  
}


