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
package spiralcraft.data.types.standard;

import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.rules.AbstractRule;
import spiralcraft.rules.RuleChannel;
import spiralcraft.rules.Violation;

import java.net.URI;

public class StringType
  extends PrimitiveTypeImpl<String>
{
  private int maxLength=-1;
  
  public StringType(TypeResolver resolver,URI uri)
  { 
    super(resolver,uri,String.class); 
    
  }
  
  @SuppressWarnings("unchecked") // Generic array in addRules
  @Override
  public void createRules()
  { 
    if (maxLength>0)
    {
      addRules
        (new StringLengthRule()
        );
          
    }
  }
  
  @Override
  public String fromString(String val)
  { return val;
  }

  public void setMaxLength(int val)
  { maxLength=val;
  }
  
  public int getMaxLength()
  { return maxLength;
  }

    
  class StringLengthRule
    extends AbstractRule<Type<String>,String>
  {

    @Override
    public Channel<Violation<String>> bindChannel(
      Focus<String> focus)
      throws BindException
    { return new StringLengthRuleChannel(focus);
    }   
    
    class StringLengthRuleChannel
      extends RuleChannel<String>
    {

      private final Channel<String> source;
    
      public StringLengthRuleChannel(Focus<String> focus)
      { source=focus.getSubject();
      }      
          
      @Override
      protected Violation<String> retrieve()
      {
        String value=source.get();
        if (value==null)
        { return null;
        }
        if (value.length()>maxLength)
        { 
          return new Violation<String>
            (StringLengthRule.this,"Must be under "+maxLength+" characters");
        }
        return null;
      }
    }
 
  }
}
