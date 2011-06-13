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
package spiralcraft.cli;

import spiralcraft.lang.util.Configurator;

/**
 * Generic 'command line' argument handling. Command line arguments
 *   fall into 2 categories, standard arguments and option flags. Option
 *   flags begin with an option indicator.
 */
public class BeanArguments<T>
  extends ReflectorArguments<T>
{
  public BeanArguments(T bean)
  { super(Configurator.forBean(bean));
  }

}
