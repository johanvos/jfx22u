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

package com.sun.javafx.sg.prism;

import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import com.sun.javafx.sg.PGCamera;
import com.sun.prism.Graphics;
import com.sun.prism.camera.PrismCameraImpl;

/**
 * TODO: 3D - Need documentation
 */
abstract public class NGCamera extends NGNode implements PGCamera {

    private PrismCameraImpl cameraImpl;

    @Override NodeType getNodeType() {
        return NodeType.NODE_NONE;
    }

    @Override protected void doRender(Graphics g) {
    }

    @Override protected void renderContent(Graphics g) {
    }

    @Override protected boolean hasOverlappingContents() {
        return false;
    }

    public void setNearClip(float nearClip) {
        getCameraImpl().setNearClip(nearClip);
    }

    public void setFarClip(float farClip) {
        getCameraImpl().setFarClip(farClip);
    }

    @Override
    public void setWorldTransform(Affine3D localToWorldTx) {
        getCameraImpl().setWorldTransform(localToWorldTx);
    }
    
    
    public void setCameraImpl(PrismCameraImpl cameraImpl) {
        this.cameraImpl = cameraImpl;
    }
    
    public PrismCameraImpl getCameraImpl() {
        return cameraImpl;
    }

    @Override
    public void release() {
        // TODO: 3D - Need to release native resources
//        System.err.println("NGCamera: Need to release native resources");
    }

    @Override
    public GeneralTransform3D getScreenProjViewTx(GeneralTransform3D tx, double w, double h) {
        return getCameraImpl().getScreenProjViewTx(tx, w, h);
    }

    @Override
    public Rectangle getViewport(Rectangle vp) {
        return getCameraImpl().getViewport(vp);
    }
}