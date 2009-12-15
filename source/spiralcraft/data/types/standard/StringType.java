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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.rules.AbstractRule;
import spiralcraft.rules.PatternRule;
import spiralcraft.rules.RuleChannel;
import spiralcraft.rules.Violation;

import java.net.URI;
import java.util.regex.Pattern;

public class StringType
  extends PrimitiveTypeImpl<String>
{
  private int maxLength=-1;
  private Pattern acceptPattern;
  private String acceptPatternMessage;
  private Pattern rejectPattern;
  private String rejectPatternMessage;
  
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
    if (acceptPattern!=null)
    {
      addRules
        (new PatternRule()
        { 
          { 
            this.setPattern(acceptPattern);
            setRejectMatch(false);
            setIgnoreNull(true);
            setMessage
              (acceptPatternMessage==null
              ?"Input must conform to pattern '"+acceptPattern+"'"
              :acceptPatternMessage
              );
          }
        }
        );
    }
    if (rejectPattern!=null)
    {
      addRules
        (new PatternRule()
        { 
          { 
            this.setPattern(rejectPattern);
            setRejectMatch(true);
            setIgnoreNull(true);
            setMessage
            (rejectPatternMessage==null
            ?"Input contains illegal pattern '"+rejectPattern+"'"
            :rejectPatternMessage
            );
          }
        }
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
  
  /**
   * A pattern which must be matched in order for the data to be accepted
   * 
   * @param pattern
   */
  public void setAcceptPattern(Pattern pattern)
  { this.acceptPattern=pattern;
  }
  
  /**
   * The message associated with the accept pattern
   * 
   * @param message
   */
  public void setAcceptPatternMessage(String message)
  { this.acceptPatternMessage=message;
  }

  /**
   * A pattern which, if matched, will cause the data to be rejected
   * 
   * @param pattern
   */
  public void setRejectPattern(Pattern pattern)
  { this.rejectPattern=pattern;
  }

  /**
   * The message associated with the reject pattern
   * 
   * @param message
   */
  public void setRejectPatternMessage(String message)
  { this.rejectPatternMessage=message;
  }
    
  class StringLengthRule
    extends AbstractRule<Type<String>,String>
  {

    @Override
    public Channel<Violation<String>> bindChannel
      (Channel<String> source
      ,Focus<?> focus
      ,Expression<?>[] args
      )
      throws BindException
    { return new StringLengthRuleChannel(source);
    }   
    
    class StringLengthRuleChannel
      extends RuleChannel<String>
    {

      private final Channel<String> source;
    
      public StringLengthRuleChannel(Channel<String> source)
      { this.source=source;
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
