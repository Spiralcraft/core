//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.text.markup;

import spiralcraft.text.ParseException;
import spiralcraft.text.ParsePosition;

/**
 * <P>Compiles a CharSequence containing markup into a tree of Units. The
 *   actual interpretation of the markup is left to the subclass.
 * </P>
 *   
 * <P>The MarkupCompiler is not thread-safe
 * </P>
 */
public abstract class MarkupCompiler<U extends Unit<U>>
  implements MarkupHandler
{
  
  private final MarkupParser _parser;
  // private final Trimmer _trimmer=new Trimmer("\r\n\t ");
  // private final CharSequence _startDelimiter;
  // private final CharSequence _endDelimiter;
  private U _unit;
  protected ParsePosition position;
  
  
  public MarkupCompiler
    (CharSequence startDelimiter
    ,CharSequence endDelimiter
    )
  { 
    // _startDelimiter=startDelimiter;
    // _endDelimiter=endDelimiter;
    _parser=new MarkupParser(startDelimiter,endDelimiter);
    _parser.setMarkupHandler(this);
  }

  /**
   * Compile a sequence of marked up text and add all the units read as
   *   children of the specified root Unit.
   * 
   * @param root
   * @param sequence
   * @throws ParseException
   */
  public synchronized void compile(U root,CharSequence sequence)
    throws ParseException,MarkupException
  { 
    // Give the root unit an origin position
    // - it is not actually in the document
    ParsePosition position=new ParsePosition();
    position.setIndex(0);
    root.setPosition(position);
    
    _unit=root;

    _parser.parse(sequence);
    
    if (!(_unit==root))
    { 
      throw new MarkupException
        ("Unexpected end of input. Unclosed unit "+_unit.getName(),position);
    }
  }

  public void setPosition(ParsePosition position)
  { this.position=position;
  }
  
  public ParsePosition getPosition()
  { return position;
  }
  
  /**
   * Closes the current containing unit
   */
  protected final void closeUnit()
    throws MarkupException
  { 
    _unit.close();
    _unit=_unit.getParent();
  }
  
  /**
   * Obtain the current containing unit
   */
  protected final U getUnit()
  { return _unit;
  }
  
  /**
   * Called by the superclass to open a new Unit node. If the newUnit.isOpen()
   *   it will be the current containing unit. Otherwise, the existing 
   *   containing unit will remain the current containing unit.
   *   
   * 
   * @param newUnit
   */
  protected final void pushUnit(U newUnit)
  {
    if (newUnit.isOpen())
    { _unit=newUnit;
    }
  }
  
  public abstract void handleMarkup(CharSequence code)
    throws ParseException;

  public abstract void handleContent(CharSequence text)
    throws ParseException;
  
  
}
