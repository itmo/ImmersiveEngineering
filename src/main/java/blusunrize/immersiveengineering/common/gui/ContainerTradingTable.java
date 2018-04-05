/*
 *  Itmo
 *  Copyright (c) 2018
 *  Do what you will. No guarantee. 
 */
package blusunrize.immersiveengineering.common.gui;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;

/*
    -state stored in tileentity?
        -in customNBT?
*/
/**
 *  this is the "server" side of the trading table, which 
 *  implements the ui components and logic of the table? 
 *  modeled after ContainerModWorkbench
 */
public class ContainerTradingTable 
extends ContainerIEBase<TileEntityTradingTable>
{
    public InventoryPlayer inventoryPlayer; //why public? why not protected?
                                            //also why not accessed via superclass?
    protected IItemHandler inv;
    public ContainerTradingTable(
                    InventoryPlayer inventoryPlayer,
                    TileEntityTradingTable tile)
    {
        super(inventoryPlayer,tile);
		this.tile = tile; //why do we have to set this
        this.inventoryPlayer = inventoryPlayer;

        inv = new ItemStackHandler(tile.getInventory()); //why this.
		if (inv instanceof IEItemStackHandler)
			((IEItemStackHandler) inv).setTile(tile);

        rebindSlots();//only need to rebind if layout needs to change?
        ImmersiveEngineering.proxy.reInitGui();
    }
    /*
     *  this was public but apparently it  doesnt need to be
     */
    protected void rebindSlots()
    {
        
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
        slotCount=0;
        
        bindTradingslots();        
        
        bindPlayerInventory(inventoryPlayer);
    }
    /**
     *  bind slots used for trading
     */
    protected void bindTradingSlots()
    {
        /*
            ingoing stuff.
        */
        int xoffset=18;
        int yoffset=18;
        for(int x=0;x<5;x++)
        {
            int yc=y*yoffset+6;
            for(int y=0;y<5;y++)
            {
                xc=x*xoffset+12;
                this.addSlotToContainer(
                        new IESlot.ContainerCallback(
                                        new IngoingSlots(), 
                                        inv, 
                                        slotCount++, 
                                        xc, 
                                        yc));                
            }        
        }
        /*
            outgoing stuff.
        */
        for(int x=0;x<5;x++)
        {
            int yc=y*yoffset+6;
            for(int y=0;y<5;y++)
            {
                xc=x*xoffset+12;
                this.addSlotToContainer(
                        new IESlot.ContainerCallback(
                                        new OutgoingSlots(), 
                                        inv, 
                                        slotCount++, 
                                        xc, 
                                        yc));                
            }        
        }
    }
    /**
     *  bind player inventory and action bar so it is accessible during
     *  container operation
     */
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{

        /*  3x9 lines of inventory*/
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(
                    new Slot(
                            inventoryPlayer, 
                            j+i*9+9,  //slots 9-36 are inventory?
                            8+j*18,   //graphical x
                            87+i*18));//graphical y

        /*  1x9 line of action bar*/
		for (int i = 0; i < 9; i++)
			addSlotToContainer(
                    new Slot(
                            inventoryPlayer, 
                            i,     //i assume slots 0-8 are action bar
                            8+i*18,//x coordinate in graphics
                            145)); //apparently the y coordinate 
	}    
    /**
     *  listener to handle ingoing container slots
     */
    protected class IngoingSlots
    implements ICallbackContainer
    {
        @Override
        public boolean canInsert(
                            ItemStack stack, 
                            int slotNumer, 
                            Slot slotObject)
        {
            //check that trade has not been made
            if(stack.isEmpty())
                return false;
            //do we need to check current slot contents?
            return false;//return true 
        }
        @Override
        public boolean canTake(
                            ItemStack stack, 
                            int slotNumer, 
                            Slot slotObject)
        {
            //check that trade has not yet been made
            return false;
        }    
    }
    /**
     *  listener to handle outgoing container slots
     */
    protected class OutgoingSlots
    implements ICallbackContainer
    {
        @Override
        public boolean canInsert(
                            ItemStack stack, 
                            int slotNumer, 
                            Slot slotObject)
        {
            //never.
            return false;
        }
        @Override
        public boolean canTake(
                            ItemStack stack, 
                            int slotNumer, 
                            Slot slotObject)
        {
            //check that trade has been made,or will be made with
            //this click
            return false;
        }    
    }

	@Override
	public void onContainerClosed(EntityPlayer playerIn)
	{
		if (inv instanceof IEItemStackHandler)
			((IEItemStackHandler) inv).setTile(null);
        super.onContainerClosed(playerIn);
	}    
}