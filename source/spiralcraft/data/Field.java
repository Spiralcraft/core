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
import spiralcraft.lang.ChannelFactory;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.rules.RuleSet;


/**
 * <p>A description of a single data element of a Tuple.
 * </p>
 * 
 * <p>
 * </p>
 */
public interface Field<T>
  extends ChannelFactory<T,Tuple>
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
  Type<T> getType();
  
  
  /**
   * @return This field's URI, which is this field's name in the context of
   *   the Type that it belongs to. 
   */
  URI getURI();
  
  /**
   * @return The expression that evaluates to an initial value for the Field
   *   when the containing Tuple is created.
   */
  Expression<T> getNewExpression();

  /**
   * @return The expression that evaluates to a default value for the Field,
   *   applied if the Field value is null at the time updates are committed.
   */
  Expression<T> getDefaultExpression();

  /**
   * @return The expression that evaluates to the Field value,
   *   applied at the time updates are committed. 
   */
  Expression<T> getFixedExpression();
  
  /**
   * <p>Create a Channel that accesses the value of this Field in the Tuple
   *   provided by the source Focus. The results of this operation may 
   *   depend on the availability of other resources mapped through the Focus.
   * </p>
   * 
   * @param focus
   * @return A Binding bound to the focus
   */
   @Override
  Channel<T> bindChannel(Focus<Tuple> focus)
    throws BindException;
  
  /**
   * @return Indicates that values for this Field should not be persisted
   */
  boolean isTransient();
  
  /**
   * @return Indicates that the field, when persisted, cannot have a null value
   */
  boolean isRequired();
  
  /**
   * @return Indicates that the field, when persisted, must have a unique value
   *   within the set of Tuples managed by a given Store.
   */
  boolean isUniqueValue();
  
  /**
   * 
   * @return The spiralcraft.lang.Reflector that provides type metadata for 
   *   the field value.
   *     
   */
  Reflector<T> getContentReflector();
  
  /**
   * 
   * @return The RuleSet that validates data for this Field.
   */
  RuleSet<? extends Field<T>,T> getRuleSet();

  /**
   * Retrieve the value of this Field in the specified Tuple
   */
  T getValue(Tuple t)
    throws DataException;
  
  /**
   * Update the value of this Field in the specified Tuple
   */
  void setValue(EditableTuple t,T value)
    throws DataException;

  
}
