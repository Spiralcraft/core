//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data;

/**
 * An EditableTuple is a Tuple which provides a method to modify its data.
 */
 
public interface EditableTuple
  extends Tuple
{
  
  /**
   * Modify the Object in the specified Field position. The index supplied
   *   corresponds to the Field's order of definition in the Scheme.
   */
  void set(int index,Object data)
    throws DataException;

  /**
   *@return the extent Tuple associated with an ancestor base Type of
   *  this Tuple's extent Type.
   */
  @Override
  EditableTuple widen(Type<?> type)
    throws DataException;

  /**
   * <p>Update a -raw- Tuple data value by field name. Intended for
   *   development / diagnostic use only.
   * </p>
   * 
   * <p><b>IMPORTANT: THIS METHOD CARRIES A PERFORMANCE PENALTY!</b>
   * 
   *  For repeated usage, use Field.set(Tuple,Object) or
   *   spiralcraft.lang.Channel bindings, as this method may be orders of 
   *   magnitude less efficient than the former methods due to the repeated
   *   lookup and comparison of field names.
   * </p> 
   * 
   *   
   *@throws DataException If the Type does not contain a field by the
   *  specified name, or an error occurs updating data.
   */
  void set(String fieldName,Object data)
    throws DataException;
  
  /**
   * Copy data from the source Tuple which is of the same Type or an archetype
   *   of this Tuple's Type. Non-null field values will be copied.
   *   
   * @param source The data source
   * @throws DataException If there is an error during copying
   */
  void copyFrom(Tuple source)
    throws DataException;
}