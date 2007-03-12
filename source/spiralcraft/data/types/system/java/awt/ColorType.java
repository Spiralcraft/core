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
package spiralcraft.data.types.system.java.awt;

import spiralcraft.data.TypeResolver;

import spiralcraft.data.reflect.ReflectionType;

import java.net.URI;

import java.awt.Color;


public class ColorType
  extends ReflectionType<Color>
{
  
  public ColorType(TypeResolver resolver,URI uri)
  { 
    super(resolver,uri,Color.class);

    // TODO: More robust externalization to support ColorSpace.
    //   Unfortunately "components" is not a bean property. Perhaps
    //   we need to use presence of a ColorSpace as a discriminator between
    //   this and a method call to set a persistent property- ExpressionField?
    //
    //   The getComponents(float[]) is a bizarre pattern- passing an empty array
    //     for GC efficiency traversing large images? Flyweight pattern?
    
    setPreferredConstructor("red","green","blue","alpha");
    
  }
}