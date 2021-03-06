/*
 * The MIT License
 *
 * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.entityreborn.chvirtualchests;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.annotations.shutdown;
import com.laytonsmith.annotations.startup;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class VirtualChests {
    private static String VERSION;

    private static Map<String, MCInventory> chests =
            new HashMap<String, MCInventory>();

    static {
        Package p = VirtualChests.class.getPackage();

        if (p == null) {
            p = Package.getPackage("me.entityreborn.chvirtualchests");
        }

        VERSION = "(unknown)";

        if (p != null) {
            String v = p.getImplementationVersion();

            if (v != null) {
                VERSION = v;
            }
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    @startup
    public static void startup() {
        System.out.println(getVersion() + " loaded.");
    }

    @shutdown
    public static void shutdown() {
        System.out.println(getVersion() + " unloaded.");
    }

    public static MCInventory get(String id) {
        return chests.get(id.toLowerCase().trim());
    }

    public static void set(String id, MCInventory inv) {
        chests.put(id.toLowerCase().trim(), inv);
    }

    public static Set<String> getAll() {
        return chests.keySet();
    }

    public static MCInventory create(String id) {
        return create(id, 54, "Virtual Chest");
    }

    public static MCInventory create(String id, int size, String title) {
        if (size <= 0) {
            size = 9;
        }

        int s = size / 9 * 9; // Assert that the size is multiple of 9.

        return Static.getServer().createInventory(
                new VirtualHolder(id.toLowerCase().trim()), s, title);
    }

    public static MCInventory del(String id) {
        String key = id.toLowerCase().trim();
        return chests.remove(key);
    }

    public static MCInventory setContents(MCInventory inv, CArray items, Target t) {
        for (String key : items.keySet()) {
            try {
                int index = Integer.parseInt(key);

                if (index >= 0 && index < inv.getSize()) {

                    MCItemStack is = ObjectGenerator.GetGenerator().item(items.get(index), t);

                    if (is.getTypeId() != 0) {
                        inv.setItem(index, is);
                    }
                } else {
                    ConfigRuntimeException.DoWarning("Out of range value (" + index + ") found in array passed to set_virtualchest(), so ignoring.");
                }
            } catch (NumberFormatException e) {
                ConfigRuntimeException.DoWarning("Expecting integer value for key in array passed to set_pinv(), but \"" + key + "\" was found. Ignoring.");
            }
        }

        return inv;
    }

    public static CArray toCArray(MCInventory inv) {
        CArray items = CArray.GetAssociativeArray(Target.UNKNOWN);

        for (int i = 0; i < inv.getSize(); i++) {
            Construct c = ObjectGenerator.GetGenerator().item(inv.getItem(i), Target.UNKNOWN);
            items.set(i, c, Target.UNKNOWN);
        }

        items.set("id", getID(inv));
        items.set("size", String.valueOf(inv.getSize()));
        items.set("title", inv.getTitle());

        return items;
    }

    public static MCInventory fromCArray(Target t, CArray array) {
        String id = "";
        String title = "Virtual Chest";
        int size = 54;

        if (array.containsKey("id")) {
            id = array.get("id").getValue();
        } else {
            throw new ConfigRuntimeException("Expecting item with key 'id' in arg 2 array", Exceptions.ExceptionType.FormatException, t);
        }

        if (array.containsKey("size") && array.get("size") instanceof CInt) {
            size = (int) ((CInt) array.get("size")).getInt();
        }

        if (array.containsKey("title")) {
            title = array.get("title").getValue();
        }

        MCInventory inv = VirtualChests.create(id, size, title);
        VirtualChests.setContents(inv, array, t);

        return inv;
    }

    public static String getID(MCInventory inv) {
        MCInventoryHolder ih = inv.getHolder();

        if (ih.getHandle() instanceof VirtualHolder.Holder) {
            return ((VirtualHolder.Holder) ih.getHandle()).id();
        }

        return null;
    }
}