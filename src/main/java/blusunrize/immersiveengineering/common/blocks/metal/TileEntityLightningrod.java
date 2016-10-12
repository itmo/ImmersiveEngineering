package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockLightningrod;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.IEnergyProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntityLightningrod extends TileEntityMultiblockPart<TileEntityLightningrod> implements IFluxProvider,IEnergyProvider
{
	FluxStorage energyStorage = new FluxStorage(IEConfig.Machines.lightning_output);

	ArrayList<BlockPos> fenceNet = null;
	int height;

	@Override
	public void update()
	{
		if(!worldObj.isRemote && formed && pos==13)
		{
			if(energyStorage.getEnergyStored()>0)
			{
				TileEntity tileEntity;
				for(EnumFacing f : EnumFacing.HORIZONTALS)
				{
					tileEntity = worldObj.getTileEntity(getPos().offset(f,2));
					if(tileEntity instanceof IFluxReceiver)
					{
						IFluxReceiver ifr = (IFluxReceiver) tileEntity;
						int accepted = ifr.receiveEnergy(f.getOpposite(), energyStorage.getEnergyStored(), true);
						int extracted = energyStorage.extractEnergy(accepted, false);
						ifr.receiveEnergy(f.getOpposite(), extracted, false);
					}
				}
			}

			if(worldObj.getTotalWorldTime()%256==((getPos().getX()^getPos().getZ())&255))
				fenceNet = null;
			if(fenceNet==null)
				fenceNet = this.getFenceNet();
			if(fenceNet!=null && worldObj.getTotalWorldTime()%128==((getPos().getX()^getPos().getZ())&127) && ( worldObj.isThundering() || (worldObj.isRaining()&&worldObj.rand.nextInt(10)==0) ))
			{
				int i = this.height + this.fenceNet.size();
				if(worldObj.rand.nextInt(4096*worldObj.getHeight())<i*(getPos().getY()+i))
				{
					this.energyStorage.setEnergy(IEConfig.Machines.lightning_output);
					BlockPos pos = fenceNet.get(worldObj.rand.nextInt(fenceNet.size()));
					EntityLightningBolt entityLightningBolt = new EntityLightningBolt(worldObj, pos.getX(),pos.getY(),pos.getZ(), true);
					worldObj.addWeatherEffect(entityLightningBolt);
					worldObj.spawnEntityInWorld(entityLightningBolt);
				}
			}
		}
	}

	ArrayList<BlockPos> getFenceNet()
	{
		this.height = 0;
		boolean broken = false;
		for(int i=getPos().getY()+2; i<worldObj.getHeight()-1; i++)
		{
			BlockPos pos = getPos().add(0,i,0);
			if(!broken && isFence(pos))
				this.height++;
			else if(!worldObj.isAirBlock(pos))
				return null;
			else
			{
				if(!broken)
					broken=true;
			}
		}

		ArrayList<BlockPos> openList = new ArrayList();
		ArrayList<BlockPos> closedList = new ArrayList();
		openList.add(getPos().add(0,height,0));
		while(!openList.isEmpty() && closedList.size()<256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next) && isFence(next))
			{
				closedList.add(next);
				openList.add(next.offset(EnumFacing.WEST));
				openList.add(next.offset(EnumFacing.EAST));
				openList.add(next.offset(EnumFacing.NORTH));
				openList.add(next.offset(EnumFacing.SOUTH));
				openList.add(next.offset(EnumFacing.UP));
			}
			openList.remove(0);
		}
		return closedList;
	}
	boolean isFence(BlockPos pos)
	{
		return Utils.isBlockAt(worldObj, pos, IEContent.blockMetalDecoration1, BlockTypes_MetalDecoration1.STEEL_FENCE.getMeta());
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public float[] getBlockBounds()
	{
		if(pos==22)
			return new float[]{-.125f,0,-.125f,1.125f,1,1.125f};
		if(pos%9==4 || (pos<18&&pos%9%2==1))
			return new float[]{0,0,0,1,1,1};
		if(pos<9)
			return new float[]{0, 0, 0, 1, .5f, 1};
		float xMin = 0;
		float xMax = 1;
		float yMin = 0;
		float yMax = 1;
		float zMin = 0;
		float zMax = 1;
		if(pos%9==0||pos%9==2||pos%9==6||pos%9==8)
		{
			if(pos < 18)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = (facing.getAxis() == Axis.X ? (pos%9 > 2 ^ facing == EnumFacing.EAST) : (pos % 3 == 2 ^ facing == EnumFacing.SOUTH)) ? .8125f : .4375f;
				xMax = (facing.getAxis() == Axis.X ? (pos%9 < 3 ^ facing == EnumFacing.EAST) : (pos % 3 == 0 ^ facing == EnumFacing.SOUTH)) ? .1875f : .5625f;
				zMin = (facing.getAxis() == Axis.X ? (pos % 3 == 2 ^ facing == EnumFacing.EAST) : (pos%9 < 3 ^ facing == EnumFacing.SOUTH)) ? .8125f : .4375f;
				zMax = (facing.getAxis() == Axis.X ? (pos % 3 == 0 ^ facing == EnumFacing.EAST) : (pos%9 > 2 ^ facing == EnumFacing.SOUTH)) ? .1875f : .5625f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = (facing.getAxis() == Axis.X ? (pos%9 > 2 ^ facing == EnumFacing.EAST) : (pos % 3 == 2 ^ facing == EnumFacing.SOUTH)) ? 1 : .625f;
				xMax = (facing.getAxis() == Axis.X ? (pos%9 < 3 ^ facing == EnumFacing.EAST) : (pos % 3 == 0 ^ facing == EnumFacing.SOUTH)) ? 0 : .375f;
				zMin = (facing.getAxis() == Axis.X ? (pos % 3 == 2 ^ facing == EnumFacing.EAST) : (pos%9 < 3 ^ facing == EnumFacing.SOUTH)) ? 1 : .625f;
				zMax = (facing.getAxis() == Axis.X ? (pos % 3 == 0 ^ facing == EnumFacing.EAST) : (pos%9 > 2 ^ facing == EnumFacing.SOUTH)) ? 0 : .375f;
			}
		}
		else if(pos>17)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = offset[0]<0?.375f:0;
			xMax = offset[0]>0?.625f:1;
			zMin = offset[2]<0?.375f:0;
			zMax = offset[2]>0?.625f:1;
		}
		return new float[]{xMin, yMin, zMin, xMax, yMax, zMax};
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0)
			return null;
		ItemStack s = null;
		try{
			s = MultiblockLightningrod.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
		}catch(Exception e){e.printStackTrace();}
		return s!=null?s.copy():null;
	}

	@Override
	public void disassemble()
	{
		super.invalidate();
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getPos().add(-offset[0],-offset[1],-offset[2]);
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startPos) instanceof TileEntityLightningrod))
				return;

			for(int yy=0;yy<=3;yy++)
				for(int xx=-1;xx<=1;xx++)
					for(int zz=-1;zz<=1;zz++)
					{
						ItemStack s = null;
						TileEntity te = worldObj.getTileEntity(startPos.add(xx, yy, zz));
						if(te instanceof TileEntityLightningrod)
						{
							s = ((TileEntityLightningrod)te).getOriginalBlock();
							((TileEntityLightningrod)te).formed=false;
						}
						if(startPos.add(xx, yy, zz).equals(getPos()))
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startPos.add(xx, yy, zz).equals(getPos()))
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX()+.5,getPos().getY()+.5,getPos().getZ()+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==IEContent.blockMetalMultiblock)
									worldObj.setBlockToAir(startPos.add(xx, yy, zz));
								worldObj.setBlockState(startPos.add(xx, yy, zz), Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItemDamage()));
							}
						}
					}
		}
	}

	@Override
	protected FluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}
	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}
	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(pos==4)
				renderAABB = new AxisAlignedBB(getPos().add(-1,0,-1), getPos().add(2,5,2));
			else
				renderAABB = new AxisAlignedBB(getPos(),getPos());
		return renderAABB;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()* IEConfig.increasedTileRenderdistance;
	}

	@Override
	public int extractEnergy(@Nullable EnumFacing from, int energy, boolean simulate)
	{
		if(pos!=10&&pos!=12&&pos!=14&pos!=16)
			return 0;
		TileEntityLightningrod master = master();
		return master==null?0:master.energyStorage.extractEnergy(energy, simulate);
	}
	@Override
	public int getEnergyStored(@Nullable EnumFacing from)
	{
		TileEntityLightningrod master = master();
		return master==null?0:master.energyStorage.getEnergyStored();
	}
	@Override
	public int getMaxEnergyStored(@Nullable EnumFacing from)
	{
		TileEntityLightningrod master = master();
		return master==null?0:master.energyStorage.getMaxEnergyStored();
	}
	@Override
	public boolean canConnectEnergy(@Nullable EnumFacing from)
	{
		return pos==10||pos==12||pos==14||pos==16;
	}
}