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
package spiralcraft.text.translator;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.text.ParseException;

/**
 * <p>Provides for pluggable implementations to 
 *   translate Strings between a foreign and a local representation.
 * </p>
 * 
 * <p>A foreign representation designates information stored in an external
 *   system or in a user interface. A local representation designates a 
 *   canonical form used for further processing.
 * </p>
 * 
 * <p>By convention, the "out" direction represents a "publishing" of the 
 *   String from a local representation to a foreign representation, and the
 *   "in" direction represents the consumption of input from a foreign 
 *   representation to a local representation.
 * </p>
 * 
 * <p>It is not necessary for the Translation to be completely symmetrical. 
 *   In some cases, data may be lost in the translation process. (eg.
 *   whitespace and extraneous characters may be removed in one direction).
 * </p>
 *   
 *   
 * 
 * @author mike
 *
 */
public interface Translator
  extends ChannelFactory<String,String>
{

  String translateOut(CharSequence local)
    throws ParseException;
  
  String translateIn(CharSequence foreign)
    throws ParseException;
}


class TranslatorChannel
  extends AbstractChannel<String>
{
  private final Translator translator;
  private final Channel<String> source;
  
  public TranslatorChannel
    (Translator translator
    ,Channel<String> source
    )
    throws BindException
  { 
    super(BeanReflector.<String>getInstance(String.class));
    this.translator=translator;
    this.source=source;
  }

  @Override
  protected String retrieve()
  { 
    String val=source.get();
    try
    { return translator.translateOut(val);
    }
    catch (ParseException x)
    { throw new AccessException("Error translating output :"+val,x);
    }
  }

  @Override
  protected boolean store(String val)
    throws AccessException
  { 
    try
    { return source.set(translator.translateIn(val));
    }
    catch (ParseException x)
    { throw new AccessException("Error translating input :"+val,x);
    }
  }
  
  @Override
  public boolean isWritable()
  { return source.isWritable();
  }
}
