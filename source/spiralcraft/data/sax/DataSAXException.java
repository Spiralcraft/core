//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.data.sax;

import org.xml.sax.SAXException;

/**
 * Thrown when a handler exception throws a SAX exception, to differentiate
 *   between natively generated SAX exceptions.
 * 
 * @author mike
 *
 */
public class DataSAXException
  extends SAXException
{

  private static final long serialVersionUID = -8902382215363040861L;

  public DataSAXException(String message)
  { super(message);
  }

  public DataSAXException(String message,Exception cause)
  {
    super(message,cause);
    initCause(cause);
  }

}
