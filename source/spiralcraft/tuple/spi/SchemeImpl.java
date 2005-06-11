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
package spiralcraft.tuple.spi;

import spiralcraft.tuple.Scheme;
import spiralcraft.tuple.Field;
import spiralcraft.tuple.FieldList;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.net.URI;

/**
 * A basic, efficient implementation of a Scheme
 */
public class SchemeImpl
  implements Scheme
{
  private FieldList _fields;
  private URI _uri;
  
  public SchemeImpl()
  { }
  
  
  /**
   * Copy constructor
   */
  public SchemeImpl(Scheme scheme)
  {
    _uri=scheme.getURI();
    setFields(new FieldListImpl(scheme.getFields()));
  }
  
  public URI getURI()
  { return _uri;
  }
  
  public void setURI(URI uri)
  { _uri=uri;
  }

  public FieldList getFields()
  { return _fields;
  }

  /**
   * Specify the FieldList for this Scheme.
   *
   * Fields in the list will be indexed and bound to this Scheme.
   */
  protected void setFields(FieldListImpl<FieldImpl> fields)
  { 
    int i=0;
    for (FieldImpl field: fields)
    { 
      field.setIndex(i++);
      field.setScheme(this);
    }
    _fields=fields;
  }
}
