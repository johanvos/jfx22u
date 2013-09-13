/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.font.coretext;

import com.sun.javafx.font.CompositeFontResource;
import com.sun.javafx.font.CompositeStrike;
import com.sun.javafx.font.FontStrike;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.font.PrismFontFactory;
import com.sun.javafx.text.GlyphLayout;
import com.sun.javafx.text.TextRun;

class CTGlyphLayout extends GlyphLayout {

    private long createCTLine(long fontRef, char[] chars,
                              int start, int length) {
        /* Use CoreText to analize the run */
        long alloc = OS.kCFAllocatorDefault();
        long textRef = OS.CFStringCreateWithCharacters(alloc, chars, start, length);
        long attributes = OS.CFDictionaryCreateMutable(alloc, 4,
                              OS.kCFTypeDictionaryKeyCallBacks(),
                              OS.kCFTypeDictionaryValueCallBacks());
        OS.CFDictionaryAddValue(attributes, OS.kCTFontAttributeName(), fontRef);
        /* Note that by default CoreText will apply kerning depending on the font*/
        long attString = OS.CFAttributedStringCreate(alloc, textRef, attributes);
        long lineRef = OS.CTLineCreateWithAttributedString(attString);
        OS.CFRelease(attributes);
        OS.CFRelease(attString);
        OS.CFRelease(textRef);
        return lineRef;
    }

    private int getFontSlot(long runRef, CompositeFontResource fr, String name) {
        long runAttrs = OS.CTRunGetAttributes(runRef);
        long actualFont = OS.CFDictionaryGetValue(runAttrs, OS.kCTFontAttributeName());

        /* Use the display name from the kCTFontDisplayNameAttribute attribute
         * instead of CTFontCopyDisplayName() to avoid localized names*/
        String fontName = OS.CTFontCopyAttributeDisplayName(actualFont);
        int slot = 0;
        if (!fontName.equalsIgnoreCase(name)) {
            if (fr == null) return -1;
            slot = fr.getSlotForFont(fontName);
            if (PrismFontFactory.debugFonts) {
                System.err.println("\tFallback font= "+ fontName + " slot=" + slot);
            }
        }
        return slot;
    }

    public void layout(TextRun run, PGFont font, FontStrike strike, char[] text) {

        CompositeFontResource composite = null;
        if (strike instanceof CompositeStrike) {
            composite = (CompositeFontResource)strike.getFontResource();
            strike = ((CompositeStrike)strike).getStrikeSlot(0);
        }
        float size = strike.getSize();
        String fontName = strike.getFontResource().getFullName();
        long fontRef = ((CTFontStrike)strike).getFontRef();
        long lineRef = createCTLine(fontRef, text, run.getStart(), run.getLength());
        long runs = OS.CTLineGetGlyphRuns(lineRef);
        int glyphCount = (int)OS.CTLineGetGlyphCount(lineRef);
        int[] glyphs = new int[glyphCount];
        float[] positions = new float[glyphCount * 2 + 2];
        int[] indices = new int[glyphCount];
        long runCount = OS.CFArrayGetCount(runs);
        int glyphStart = 0, posStart = 0, indicesStart = 0;
        for (int i = 0; i < runCount; i++) {
            long runRef = OS.CFArrayGetValueAtIndex(runs, i);
            int slot = getFontSlot(runRef, composite, fontName) ;
            if (slot != -1) {
                glyphStart += OS.CTRunGetGlyphs(runRef, slot << 24, glyphStart, glyphs);
            } else {
                glyphStart += OS.CTRunGetGlyphCount(runRef);
            }
            if (size > 0) {
                posStart += OS.CTRunGetPositions(runRef, posStart, positions);
            }
            indicesStart += OS.CTRunGetStringIndices(runRef, indicesStart, indices);

        }
        if (size > 0) {
            positions[posStart] = (float)OS.CTLineGetTypographicBounds(lineRef);
        }
        run.shape(glyphCount, glyphs, positions, indices);
        OS.CFRelease(lineRef);
    }
}