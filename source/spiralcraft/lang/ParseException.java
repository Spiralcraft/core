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
package spiralcraft.lang;

public class ParseException
  extends Exception
{
  private static final long serialVersionUID=1;
  
  private final String _progress;
  private final int _pos;
  private final String _text;

  public ParseException(String message,int pos,String progress,String text)
  {
    super(message);
    _pos=pos;
    _progress=progress;
    _text=text;
  }

  public ParseException(String message,Throwable cause,int pos,String progress,String text)
  {
    super(message,cause);
    _pos=pos;
    _progress=progress;
    _text=text;
  }

  @Override
  public String toString()
  {
    return super.toString()
      +" position "+_pos+" after | "+_progress+" | in | "+_text+" |";
  }
}
