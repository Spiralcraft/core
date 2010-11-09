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
package spiralcraft.vfs;

import java.net.URI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a streaming resource that can be accessed with a URI
 */
public interface Resource
{
  /**
   * Return the absolute, canonical URI corresponding to this resource. This 
   *   may be different than the URI used to resolve the resource, which may
   *   be relative to some application specific context.
   */
  public URI getURI();

  /**
   * Return the URI that was used to resolve this Resource
   */
  public URI getResolvedURI();

  /**
	 * Read the resource data.
   *
   *@return An InputStream, or null if the resource can't be read.
   *@throws IOException if there was an unexpected problem reading.
	 */	
	public InputStream getInputStream()
		throws IOException;


  /**
   * Indicate whether this type of resource supports read operations, regardless
   *   of the actual validity or status of the specific URI.
   *@return true if this type of resource supports reading
   */
  public boolean supportsRead();

	/**
	 * Write to the resource
   *
   *@return An OutputStream, or null if the resource can't be written to.
   *@throws IOException if there was an unexpected problem opening a stream for
   *        writing.
	 */	
  public OutputStream getOutputStream()
    throws IOException;

  /**
   * Indicate whether this type of resource supports write operations, regardless
   *   of the actual validity or status of the specific URI.
   *@return true if this type of resource supports writing
   */
  public boolean supportsWrite();

  /**
   * <p>Return the children of this resource, if any. A Resource may only
   *   be a child of a single parent, thus defining a Resource tree.
   * </p>
   * 
   * <p>This method must perform the equivalent of a call to
   *   asContainer().listChildren()
   * </p>
   * 
   * @return
   */
  public Resource[] getChildren()
    throws IOException;
  
  
  /**
   * Return the Container aspect of this Resource,
   *   if that aspect applies to the specific Resource (ie.
   *   the resource is a directory)
   */
  public Container asContainer();

  /**
   * Return the Container aspect of this Resource,
   *   if that aspect applies to the specific Resource (ie.
   *   the resource is a directory), and create that aspect
   *   if it does not exist.
   *
   *@throws IOException if for some reason the Container aspect
   *  is not applicable or incomatible with this specific resource
   */
  public Container ensureContainer()
    throws IOException;
  
  /**
   * @return the enclosing or containing Resource of the canonical
   *   underlying Resource.
   */
  public Resource getParent()
    throws IOException;
  
  /**
   * 
   * @return The name of this resource with respect to its parent
   */
  public String getLocalName();
  
  /**
   * Indicate whether the resource exists (ie. operations against
   *   it are likely to succeed barring any other problems)
   */
  public boolean exists()
    throws IOException;
  
  /**
   * @return The long integer representation of the time this resource was
   *   last modified.
   */
  public long getLastModified()
    throws IOException;

  /**
   * Attempt to change the lastModified time on the resource.
   * 
   * @param lastModified
   * @return true of the operation succeeded, false otherwise
   * @throws IOException
   */
  public boolean setLastModified(long lastModified)
    throws IOException;
  
  /**
   * @return The size in bytes of this resource, or 0 if not known.
   */
  public long getSize()
    throws IOException;
  
  /**
   * Copy this resource from the source resource in the
   *   most efficient way possible.
   */
  public void copyFrom(Resource source)
    throws IOException;
  
  /**
   * Renames the resource if appropriate and efficient.
   */
  public void renameTo(URI uri)
    throws IOException;

  /**
   * <p>Copy the content of the resource to the targetResource, overwriting
   *   the targetResource if it exists. If the targetResource specifies a
   *   container (ie. a directory) the resource will be copied into to it,
   *   overwriting a resource of the same local name.
   * </p>
   * 
   * @param targetResource
   */
  public void copyTo(Resource targetResource)
    throws IOException;  
  
  /**
   * <p>Move the content of the resource to the targetResource, overwriting
   *   the targetResource if it exists. If the targetResource specifies a
   *   container (ie. a directory) the resource will be moved into to it,
   *   overwriting a resource of the same local name.
   * </p>
   * 
   * @param targetResource
   */
  public void moveTo(Resource targetResource)
    throws IOException;
  
  /**
   * Delete the resource
   *
   * @throws IOException
   */
  public void delete()
    throws IOException;
  
  /**
   * Write the InputStream to the resource
   */
  public long write(InputStream in)
    throws IOException;
  
  /**
   * Obtain an indirectly referenced resource
   * 
   * @param <T>
   * @param clazz
   * @return
   */
  public <T extends Resource> T unwrap(Class<T> clazz);
}
