//
// Copyright (c) 1998,2009 Michael Toth
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
package spiralcraft.service;

import spiralcraft.app.Component;

/**
 * <p>Represents a software subsystem which cooperates with other such
 *   subsystems to implement an application.
 * </p>
 * 
 * <p>
 *   A Service instance has a life cycle longer than the components
 *   which use it, and normally has a state which involves
 *   external resources. The start() and stop() methods are used
 *   by a container to give the Service an opportunity to manage
 *   its state after it has been configured and before it is finalized.
 * </p>
 */
public interface Service
  extends Component
{


}
