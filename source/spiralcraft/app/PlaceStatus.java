//
//Copyright (c) 2012 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app;

import spiralcraft.meta.Version;

/**
 * <p>Keeps a record of the status of instance data for a Place. Used to
 *   maintain synchronization between the code and the data for purposes of
 *   initialization or upgrade.
 * </p> 
 * 
 * @author mike
 *
 */
public class PlaceStatus
{
  private Version version;
  private String id;
  
  public void setId(String id)
  { this.id=id;
  }
  
  public String getId()
  { return id;
  }
  
  public void setVersion(Version version)
  { this.version=version;
  }
  
  public Version getVersion()
  { return version;
  }
}
