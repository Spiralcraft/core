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
import java.util.List;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.util.IteratorChain;


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
  private final RuleSet<Tcontext,Tvalue> baseSet;
  private List<RuleSet<Tcontext,Tvalue>> additionalSets;
  
  /**
   * <p>Create a new RuleSet for the specified Context
   * </p>
   * 
   * @param context
   */
  public RuleSet(Tcontext context)
  { 
    this.context=context;
    this.baseSet=null;
  }
  
  /**
   * <p>Create a new RuleSet for the specified Context, that includes all
   *   the rules in the baseSet, which is checked first
   * </p>
   * 
   * @param context
   */
  public RuleSet(Tcontext context,RuleSet<Tcontext,Tvalue> baseSet)
  { 
    this.context=context;
    this.baseSet=baseSet;
  }

  public Tcontext getContext()
  { return context;
  }
  
  private final ArrayList<Rule<Tcontext,Tvalue>> rules
    =new ArrayList<Rule<Tcontext,Tvalue>>();
  

  @SuppressWarnings({ "unchecked", "rawtypes" }) // Varargs parameter
  @Override
  public Iterator<Rule<Tcontext,Tvalue>> iterator()
  { 
    Iterator<Rule<Tcontext,Tvalue>> it=null;
    
    if (additionalSets!=null)
    {
      // Typically archetype RuleSets, these get evaluated in order of
      //   their appearance
      for (RuleSet<Tcontext,Tvalue> set:additionalSets)
      { 
        if (it!=null)
        { it=new IteratorChain<Rule<Tcontext,Tvalue>>(it,set.iterator());
        }
        else
        { it=set.iterator();
        }
      }
    }    
    
    if (it!=null)
    { 
      // Run local rules -after- archetype tules
      it=new IteratorChain(it,rules.iterator());
    }
    else
    { it=rules.iterator();
    }
    
    // Insert base rules -before- all other rules
    if (baseSet!=null)
    { it=new IteratorChain<Rule<Tcontext,Tvalue>>(baseSet.iterator(),it);
    }

    return it;
  }

  public Inspector<Tcontext,Tvalue> 
    bind(Reflector<Tvalue> subjectReflector,Focus<?> context)
    throws BindException
  {
    return new Inspector<Tcontext,Tvalue>(this,subjectReflector,context);
    
  }
  
  @SuppressWarnings("unchecked")
  public void addRules(Rule<Tcontext,Tvalue> ... rules)
  {
    for (Rule<Tcontext,Tvalue> rule : rules)
    { this.rules.add(rule);
    }
  }
  

  public void addRuleSet(RuleSet<Tcontext,Tvalue> additionalSet)
  {
    if (additionalSets==null)
    { additionalSets=new ArrayList<RuleSet<Tcontext,Tvalue>>();
    }
    additionalSets.add(additionalSet);
  }
  
}


