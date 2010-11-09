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
package spiralcraft.data.access;

import spiralcraft.app.Component;
import spiralcraft.data.Type;
import spiralcraft.data.query.Query;

/**
 * <p>Provide data-related services- e.g. text indexing, query optimization
 * </p>
 */
public interface StoreService
  extends Component
{

  Query handleQuery(Query q,Type<?>[] types);
  
  void onReload(Type<?>[] types);
  
}