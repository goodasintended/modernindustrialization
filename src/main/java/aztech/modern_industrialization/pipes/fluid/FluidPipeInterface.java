/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public interface FluidPipeInterface extends ConnectionTypeInterface, PriorityInterface {
    FluidKey getNetworkFluid();

    void setNetworkFluid(FluidKey fluid);

    boolean canUse(PlayerEntity player);

    static FluidPipeInterface ofBuf(PacketByteBuf buf) {
        FluidKey[] networkFluid = new FluidKey[] { FluidKey.fromPacket(buf) };
        int[] type = new int[] { buf.readInt() };
        int[] priority = new int[] { buf.readInt() };
        return new FluidPipeInterface() {
            @Override
            public FluidKey getNetworkFluid() {
                return networkFluid[0];
            }

            @Override
            public void setNetworkFluid(FluidKey fluid) {
                networkFluid[0] = fluid;
            }

            @Override
            public int getConnectionType() {
                return type[0];
            }

            @Override
            public void setConnectionType(int t) {
                type[0] = t;
            }

            @Override
            public int getPriority() {
                return priority[0];
            }

            @Override
            public void setPriority(int p) {
                priority[0] = p;
            }

            @Override
            public boolean canUse(PlayerEntity player) {
                return true;
            }
        };
    }

    default void toBuf(PacketByteBuf buf) {
        getNetworkFluid().toPacket(buf);
        buf.writeInt(getConnectionType());
        buf.writeInt(getPriority());
    }
}
