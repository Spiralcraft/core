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

import java.net.URI;

import spiralcraft.common.Lifecycle;
import spiralcraft.data.query.Queryable;

import spiralcraft.data.DataConsumer;
import spiralcraft.data.DataException;
import spiralcraft.data.Sequence;
import spiralcraft.data.Type;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;


/**
 * <p>A physical data container. Provides access to a set of data that is
 *   reachable through a single access mechanism, such as a database login,
 *   an XML file, the subtree of a filesystem directory, etc.
 * </p>
 *   
 * <p>A Store is always participates in a single Space.
 * </p>
 */
public interface Store
  extends Queryable<Tuple>,Lifecycle,Contextual
{
  /**
   * A name for the store within the containing Space (or containing store,
   *   in some circumstances). Used by applications to distinguish between
   *   multiple data sources.
   * 
   * @return
   */
  String getName();
  
  /**
   * A name for the store within the containing Space (or containing store,
   *   in some circumstances). Used by applications to distinguish between
   *   multiple data sources.
   * 
   * @return
   */
  void setName(String name);
  
  /**
   * The Schema of this Store, which contains the data model metadata
   * 
   * @return
   */
  Schema getSchema();
  
  /**
   * Retrieve an update 'channel'. The DataConsumer can be used once to update
   *   a batch of Tuples of the same Type.
   * 
   * @return A DataConsumer which is used to push one or more updates into
   *   this Store
   */
  DataConsumer<DeltaTuple> getUpdater(Type<?> type,Focus<?> focus)
    throws DataException;

  /**
   * <p>Return a Sequence for generating primary key data, or null if
   *   sequential ids are not provided for the specified URI. The URI is
   *   usually that of a specific Field (ie. Type.getURI()+"#"+field.getName())
   *   that denotes a primary key.  
   * </p>
   * 
   * @param type
   * @return
   * @throws DataException
   */
  Sequence getSequence(URI uri)
    throws DataException;

  /**
   * <p>Indicate whether this Store contains authoritative data for the 
   *   specified Type. If so, the store can generate IDs and process data
   *   updates for the Type.
   * </p>
   * 
   * @param type
   * @return
   */
  boolean isAuthoritative(Type<?> type);
  
  /**
   * <p>Return the URI of the local directory tree that holds persistent
   *   data and/or configuration info.
   * </p>
   * 
   * @return
   */
  URI getLocalResourceURI();
  
  /**
   * <p>Specify the URI of the local writable directory tree that holds
   *   persistent data and/or configuration info
   * </p>
   * @param localResourceURI
   */
  void setLocalResourceURI(URI localResourceURI);
  
  /**
   * Take a snapshot of the changes to the datastore since transactionId
   * 
   * @param transactionId
   * @return ASsnapshot newer than the transactionId, or null
   */
  // XXX Change to export(fromId)
  Snapshot snapshot(long transactionId)
    throws DataException;
  
  /**
   * Update the dataStore with the changes in the snapshot
   * 
   * @param snapshot
   * @throws DataException
   */
  
  // XXX Change to import(snapshot)
  void update(Snapshot snapshot)
    throws DataException;

  long getLastTransactionId();
  
  
  public void setPublic(boolean pub);
  
  public boolean isPublic();
  
}
