//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.data.flatfile;

import java.io.IOException;
import java.io.Reader;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.util.DictionaryBinding;
import spiralcraft.log.ClassLog;
import spiralcraft.util.string.StringConverter;


/**
 * Mapping of an Expression to a Formatter for a field in a flat file record.
 * 
 * @author mike
 *
 */
public class FieldMapping<T>
  implements Contextual
{
  private static final ClassLog log
    =ClassLog.getInstance(FieldMapping.class);
  
  private DictionaryBinding<T> channel=new DictionaryBinding<T>();
  private FieldEncoder encoder=new FieldEncoder();
  private boolean optional;
  private boolean debug;
  
  public FieldMapping()
  {
  }
  
  public FieldMapping(Expression<T> x)
  { channel.setTarget(x);
  }

  
  public void setOptional(boolean optional)
  { this.optional=optional;
  }
  
  public boolean getOptional()
  { return optional;
  }  
  
  public void setEncoder(FieldEncoder encoder)
  { this.encoder=encoder;
  }
  
  public FieldEncoder getEncoder()
  { return this.encoder;
  }
  
  /**
   * The Expression bound to the file field. Data will be read from
   *   and written to this expression.
   * 
   * @param expression
   */
  public void setX(Expression<T> expression)
  { channel.setTarget(expression);
  }
  
  public Expression<T> getX()
  { return channel.getTarget();
  }
  
  public void setConverter(StringConverter<T> converter)
  { channel.setConverter(converter);
  }

  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  public void parse(Reader data)
    throws IOException
  { 
    String value=encoder.parse(data);
    if (debug)
    { log.fine("Parsed ["+value+"]");
    }
    channel.set(value);
  }
  
  public void format(Appendable out)
    throws IOException
  { encoder.format(out,channel.get());
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  { 
    channel.bind(focusChain);
    return focusChain;
  }
  
  

}
