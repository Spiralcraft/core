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
package spiralcraft.common.callable;

/**
 * <p>A one-argument functor pattern.
 * </p>
 * 
 * @author mike
 *
 * @param <Ia>
 * @param <Ib>
 * @param <R>
 * @param <X>
 */
public interface BinaryFunction<Ia,Ib,R,X extends Exception>
{
  public R evaluate(Ia inputA,Ib inputB) 
    throws X;
}
