/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.deploy;

public final class Test {

    public static void main(String args[]) {

        String list[] = null;

        System.out.println("Creating new collection");
        SecurityCollection collection = new SecurityCollection();

        System.out.println("Adding GET and POST methods");
        collection.addMethod("GET");
        collection.addMethod("POST");

        System.out.println("Currently defined methods:");
        list = collection.findMethods();
        for (int i = 0; i < list.length; i++)
            System.out.println(" " + list[i]);
        System.out.println("Is DELETE included? " +
                           collection.findMethod("DELETE"));
        System.out.println("Is POST included? " +
                           collection.findMethod("POST"));

        System.out.println("Removing POST method");
        collection.removeMethod("POST");

        System.out.println("Currently defined methods:");
        list = collection.findMethods();
        for (int i = 0; i < list.length; i++)
            System.out.println(" " + list[i]);
        System.out.println("Is DELETE included? " +
                           collection.findMethod("DELETE"));
        System.out.println("Is POST included? " +
                           collection.findMethod("POST"));

    }

}
