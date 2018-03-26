/*
 *  Itmo
 *  Copyright (c) 2018
 *  Do what you will. No guarantee. 
 */
package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

//TBD this needs an entry in ClientProxy, commonproxy
/*
    -what shoudl blockwoodendevice0 - kind of  an object do?
        -it apparently constructs tileentities?
        -handles the life of the table as a block?
        -it is referred to in IEContent.java
            -which forbids it from being in crates
        -
*/
/**
 *  this class does what? implements the block? or what? 
 *  modeled after TileEntityModWorkbench
 */
public class TileEntityTradingTable 
extends TileEntityIEBase
implements IIEInventory, //manages inventory of the container?
            IDirectionalTile, //and this apparently manages direction of placement?
//            IHasDummyBlocks, //wtf is this
            IGuiTile
{

}
