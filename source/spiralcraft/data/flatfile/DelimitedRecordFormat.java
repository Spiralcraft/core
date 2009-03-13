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
import java.io.StringReader;
import java.io.StringWriter;

import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.spi.SimpleChannel;

public class DelimitedRecordFormat
  implements RecordFormat
{

  
  private String fieldSeparator;
  private FieldMapping<?>[] fields;
  private Channel<Tuple> channel;
  private Type<?> type;
  
  public void setFields(FieldMapping<?>[] fields)
  { this.fields=fields;
  }
  
  public void setFieldSeparator(String separator)
  { this.fieldSeparator=separator;
  }
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  public Type<?> getType()
  { return type;
  }
  
  @Override
  public byte[] format(Tuple data)
    throws IOException
  {
    channel.set(data);
    StringWriter writer=new StringWriter();
    boolean first=true;
    for (FieldMapping<?> mapping : fields)
    { 
      if (first)
      { first=false;
      }
      else
      { writer.write(fieldSeparator);
      }
      mapping.format(writer);
    }
    return writer.toString().getBytes();
    
  }

  @Override
  public void parse(
    byte[] record,
    Tuple target)
    throws ParseException,IOException
  {
    channel.set(target);
    StringReader reader=new StringReader(new String(record));
    for (FieldMapping<?> mapping : fields)
    { mapping.parse(reader);
    }
    String rest=readRest(reader);
    if (rest!=null)
    { throw new ParseException("Unread data in record: "+rest);
    }
    
    
    
  }

  private String readRest(Reader reader)
    throws IOException
  {
    int read;
    StringBuffer buf=new StringBuffer();
    while ( (read=reader.read())>-1)
    { buf.append((char) read);
    }
    if (buf.length()>0)
    { return buf.toString();
    }
    else
    { return null;
    }
  }
  
  @Override
  public Focus<?> bind(Focus<?> focusChain)
    throws BindException
  {
    channel=new SimpleChannel<Tuple>(DataReflector.<Tuple>getInstance(type));
    focusChain=focusChain.chain(channel);
    for (FieldMapping<?> mapping : fields)
    {
      mapping.getEncoder().setFieldSeparator(fieldSeparator);
      mapping.bind(focusChain);
    }
    return focusChain;
  }
  
}
