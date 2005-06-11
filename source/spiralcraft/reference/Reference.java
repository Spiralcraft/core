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
package spiralcraft.reference;

public interface Reference
{

  /**
   * Obtain the target of this reference.
   *
   *@return The target, or null if the target does not exist
   */
  public Object getTarget();
  
  /**
   * Obtain the unique identifier for this reference. The identifier
   *   is only valid within the scope of the Exporter which created the
   *   reference.
   *
   *@return An opaque String that uniquely identifies the target object
   */
  public String getIdentifier();

  /**
   * Obtain the Exporter which created this reference.
   *
   *@return The Exporter
   */
  public Exporter getExporter();
}
