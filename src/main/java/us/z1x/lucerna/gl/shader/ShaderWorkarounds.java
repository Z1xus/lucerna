// Copyright 2020 Grondag
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package us.z1x.lucerna.gl.shader;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

// from canvas https://github.com/grondag/canvas/commit/820bf754092ccaf8d0c169620c2ff575722d7d96
public class ShaderWorkarounds {
    // passes a null length so the driver reads up to the terminator, some amd drivers misread the length and crash
    public static void safeShaderSource(int glId, CharSequence source) {
        final MemoryStack stack = MemoryStack.stackGet();
        final int stackPointer = stack.getPointer();

        try {
            final ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
            final PointerBuffer pointers = stack.mallocPointer(1);
            pointers.put(sourceBuffer);

            GL20C.nglShaderSource(glId, 1, pointers.address0(), 0);
            APIUtil.apiArrayFree(pointers.address0(), 1);
        } finally {
            stack.setPointer(stackPointer);
        }
    }
}
