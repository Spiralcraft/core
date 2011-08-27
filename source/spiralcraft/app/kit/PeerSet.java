//
//Copyright (c) 2011 Michael Toth
//Spiralcraft Inc., All Rights Reserved
//
//This package is part of the Spiralcraft project and is licensed under
//a multiple-license framework.
//
//You may not use this file except in compliance with the terms found in the
//SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
//at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
//Unless otherwise agreed to in writing, this software is distributed on an
//"AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.app.kit;

import java.util.HashMap;
import java.util.Iterator;

import spiralcraft.app.Component;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;
import spiralcraft.util.ArrayUtil;

/**
 * Manages a set of Peers for a component
 * @author mike
 *
 */
public class PeerSet
  implements Iterable<Component>,Contextual
{

  private Component[] peers=new Component[0];
  private HashMap<Component,String> peerSet=new HashMap<Component,String>();
  private HashMap<String,Component> peerMap=new HashMap<String,Component>();
  private HashMap<Component,Peering> peerings
    =new HashMap<Component,Peering>();
  
  public void add(Component comp)
  {
    this.peers=ArrayUtil.append(this.peers,comp);
    
    this.peerSet.put(comp,null);
  }
  
  public void add(Peering peering)
  {
    Component comp=peering.component;
    String key=peering.key;
    this.peers=ArrayUtil.append(this.peers,comp);
    this.peerSet.put(comp,key);
    this.peerMap.put(key,comp);
    this.peerings.put(comp,peering);
  }
  
  public void remove(Component comp)
  {
    this.peers=ArrayUtil.remove(this.peers,comp);
    this.peerMap.remove(peerSet.get(comp));
    this.peerSet.remove(comp);
    this.peerings.remove(comp);
  }
  
  public String getKey(Component comp)
  { return peerSet.get(comp);
  }

  public Peering getPeering(Component comp)
  { return peerings.get(comp);
  }

  public boolean contains(Component comp)
  { return this.peerSet.containsKey(comp);
  }
  
  public int size()
  { return peers.length;
  }

  @Override
  public Iterator<Component> iterator()
  { return ArrayUtil.iterator(peers);
  }

  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  {
    for (Peering peering: peerings.values())
    { peering.bind(focusChain);
    }
    return focusChain;
  }
    
}
