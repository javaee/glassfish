/*
 * Copyright (c) 2008, Your Corporation. All Rights Reserved.
 */

package com.sun.enterprise.universal;

/**
 * Base64 encoding using sun.misc generates warning messages.
 * It is centralized here to get rid of the warnings sprinkled around
 * the rest of Glassfish.
 * Also, we can replace the implementation whenever we like.
 * @author bnevins
 */
public class GFBase64Decoder extends sun.misc.BASE64Decoder{
}
