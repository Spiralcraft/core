//
// Copyright (c) 2010 Michael Toth
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

import java.net.URI;

import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;

public interface Snapshot
{
  public static final URI TYPE_URI
    =URI.create("class:/spiralcraft/data/access/snapshot");
  
  public static final Type<Snapshot> TYPE
    =TypeResolver.getTypeResolver().resolveSafeFromClass(Snapshot.class);
  
  public long getTransactionId();
  
  public Aggregate<Aggregate<Tuple>> getData();
}
