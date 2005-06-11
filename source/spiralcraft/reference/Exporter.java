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

/**
 *  Provides access to a set of uniquely identifiable resources
 *  (eg. a database, file system, code repository, etc.) 
 *  by exporting References to objects of arbitrary type
 *  and by resolving unique identification strings associated
 *  with those references.
 *
 *  Note that the scope of the unique identification string is local to
 *  a specific exporter.
 */
public interface Exporter
{
  /**
   * Obtain a reference to a target object
   *@return A reference to the object, 
   */
  public Reference export(Object target)
    throws NotReferenceableException;

  /**
   * Obtain a reference to the object identified by the specified identifier. The Reference
   *   may or may not refer to an existing object. 
   * 
   *@return A Reference
   */
  public Reference resolve(String identifier)
    throws UnrecognizedIdentifierException;

}
