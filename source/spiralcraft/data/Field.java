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
package spiralcraft.data;

import java.net.URI;

import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;


/**
 * <P>A description of a single element of a Tuple.
 */
public interface Field
{
  /**
   * The FieldSet to which this Field belongs. All Fields belong to a FieldSet.
   */
  FieldSet getFieldSet();
  
  /**
   * The index of the Field within the Scheme, which corresponds to the
   *   ordinal position of the associated value within the Tuple
   */
  int getIndex();
  
  /**
   * The name of the Field, to be used for the programmatic binding of
   *   data aware components to Tuples of this Scheme. Conforms to
   *   the rules for language identifiers.
   */
  String getName();
  
  /**
   * A short descriptive name for the field, for user consumption. Does not
   *   have syntax constraints.
   */
  // XXX Consider a FieldUI component which provides descriptive information
  String getTitle();
  
  /**
   * The Type of the Field
   */
  Type<?> getType();
  
  
  /**
   * @return This field's URI, which is this field's name in the context of
   *   the Type that it belongs to. 
   */
  URI getURI();
  
  /**
   * Create a Channel that accesses the value of this Field in the Tuple
   *   provided by the source Channel
   * 
   * @param source
   * @param focus
   * @return A Binding bound to the source and focus
   */
   Channel<?> bind(Channel<Tuple> source,Focus<?> focus)
    throws BindException;
  
  /**
   * @return Whether the value for this Field is stored or recomputed every
   *   time it is accessed. 
   */
  boolean isStored();

  /**
   * Retrieve the value of this Field in the specified Tuple
   */
  Object getValue(Tuple t)
    throws DataException;
  
  /**
   * Update the value of this Field in the specified Tuple
   */
  void setValue(EditableTuple t,Object value)
    throws DataException;

  
}
