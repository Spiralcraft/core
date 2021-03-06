//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.vfs.url;

import java.io.IOException;

public class URLAccessException
  extends IOException
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private URLMessage remoteMessage;
  
  public URLAccessException(IOException cause)
  { super(cause);
  }
  
  public URLAccessException(String message,IOException cause)
  { super(message,cause);
  }
  
  public URLAccessException(String message,IOException cause,URLMessage remoteMessage)
  { 
    super(message,cause);
    this.remoteMessage=remoteMessage;
  }
  
  public URLMessage getRemoteMessage()
  { return remoteMessage;
  }
  
  @Override
  public String toString()
  { return super.toString()+":Remote Message=["+remoteMessage+"]";
  }

}
