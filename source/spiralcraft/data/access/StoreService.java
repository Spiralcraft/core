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
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.query.BoundQuery;
import spiralcraft.data.query.Query;
import spiralcraft.lang.Focus;

/**
 * <p>Provide data-related services- e.g. text indexing, query optimization
 * </p>
 */
public interface StoreService
  extends Component
{

  /**
   * Bind a Query if this StoreService will provide a more desirable execution
   *   path than the default binding.
   * 
   * @param q
   * @param types
   * @param paramFocus
   * @return
   */
  BoundQuery<?,Tuple> handleQuery(Query q,Type<?>[] types,Focus<?> paramFocus)
    throws DataException;
  
  /**
   * Called when data for the specified type is replaced/reloaded in bulk
   * 
   * @param types
   */
  void onReload(Type<?>[] types);
  
}
