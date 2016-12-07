package factory;

import control.Block;
import java.util.stream.IntStream;

public class Container {

    protected final Block[] blocks;
    public final int length;

    public Container(int length) {
        this.length = length;
        blocks = new Block[length];
    }
    
    /**
     * Checks if there are blocks in the conveyor
     *
     * @return <i>true</i> if there is at least one block. <i>false</i>
     * otherwise
     */
    public boolean hasBlock() {
        for (Block b : blocks) {
            if (b != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns one block on the conveyor or null if no blocks are present
     *
     * @return
     */
    public Block getOneBlock() {
        for (Block b : blocks) {
            if (b != null) {
                return b;
            }
        }
        return null;
    }

    /**
     * Places a block object on the conveyor
     *
     * @param b the block to place
     * @param position the index (from 0 to length-1) of where to place
     * @return <i>true</i> if there was no other block on that position and the
     * block was placed. <i>false</i> otherwise
     */
    public boolean placeBlock(Block b, int position) {
        if (position >= length) {
            throw new IndexOutOfBoundsException("Invalid block position " + position);
        }
        if (blocks[position] == null) {
            blocks[position] = b;
            return true;
        }
        return false;
    }

    public Block getBlock(int i)
    {
        return blocks[i];
    }
    
    /**
     * Removes a block object from the conveyor
     *
     * @param position the position where to remove the block
     * @return the block that was removed, or null if no block was there
     */
    public Block removeBlock(int position) {
        Block ret = blocks[position];
        blocks[position] = null;
        return ret;
    }

    public void removeAllBlocks()
    {
        IntStream.range(0, blocks.length).forEach(i -> removeBlock(i));
    }
    
}
